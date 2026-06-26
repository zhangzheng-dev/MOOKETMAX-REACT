#!/usr/bin/env python3
"""
Parse pg_dump --schema-only output and split into clean init files.
Organizes: extensions/preamble, functions, tables (with their indexes/constraints/sequences/comments).
"""
import re
import os
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent.parent
SRC = ROOT / ".tmp" / "mooket_schema.sql"
OUT_DIR = ROOT / "sql" / "init"
OUT_DIR.mkdir(parents=True, exist_ok=True)
(OUT_DIR / "tables").mkdir(exist_ok=True)
(OUT_DIR / "functions").mkdir(exist_ok=True)

# Order tables so FK dependencies are satisfied if/when added later
TABLE_ORDER = [
    "dict_user",
    "dict_product",
    "dict_product_source_map",
    "dict_factory",
    "dict_brand",
    "dict_merchant",
    "factory_tier",
    "rel_user_merchant",
    "biz_offer",
    "biz_search_history",
    "stat_country",
    "stat_factory",
    "stat_product",
    "stat_merchant",
    "stat_brand",
    "stat_country_product",
    "stat_factory_product",
    "stat_brand_product",
    "stat_price_trend",
]

raw = SRC.read_text(encoding="utf-8")

# pg_dump 16 outputs `\restrict TOKEN` and `\unrestrict TOKEN` which are session-local
# directives. Drop them — they aren't portable.
raw = re.sub(r"^\\(?:un)?restrict.*$", "", raw, flags=re.MULTILINE)

# Split into top-level statements separated by ';' on lines, while preserving
# DO/FUNCTION bodies (they end with `$$;`).
def split_statements(text):
    stmts = []
    buf = []
    in_dollar = False
    for line in text.splitlines(keepends=True):
        buf.append(line)
        if "$$" in line:
            # Toggle for each $$ occurrence on this line
            count = line.count("$$")
            if count % 2 == 1:
                in_dollar = not in_dollar
        if not in_dollar and line.rstrip().endswith(";"):
            stmts.append("".join(buf).strip())
            buf = []
    if buf and "".join(buf).strip():
        stmts.append("".join(buf).strip())
    return stmts

statements = split_statements(raw)
print(f"Total statements: {len(statements)}")

# First pass: build sequence_name -> table_name map from ALTER SEQUENCE OWNED BY statements
seq_to_table = {}
for s in statements:
    m = re.search(r"ALTER\s+SEQUENCE\s+public\.([\w_]+)\s+OWNED\s+BY\s+public\.([\w_]+)\.", s, re.IGNORECASE)
    if m:
        seq_to_table[m.group(1)] = m.group(2)

# Classify statements
preamble_set = []
functions = {}              # name -> [stmts]
comments_on_funcs = {}
tables = {}                 # name -> dict(create=, alters=[], sequences=[], indexes=[], constraints=[], comments=[])
extra = []                  # anything else

current_func_name = None

def init_table(name):
    if name not in tables:
        tables[name] = {
            "create": None,
            "create_sequences": [],
            "alter_sequences": [],
            "alters": [],
            "indexes": [],
            "constraints": [],
            "comments": [],
        }
    return tables[name]

for s in statements:
    s_stripped = s.strip()
    if not s_stripped:
        continue
    # Strip leading comment-only lines, then look at the first real statement line.
    body_lines = [ln for ln in s_stripped.splitlines() if not ln.lstrip().startswith("--") and ln.strip()]
    if not body_lines:
        # pure comment block, skip
        continue
    first_body_line = body_lines[0].strip()

    # Pure SET ... statements (pg_dump preamble)
    if re.match(r"^SET\s+\w", first_body_line, re.IGNORECASE):
        preamble_set.append(s_stripped)
        continue
    if re.match(r"^SELECT\s+pg_catalog\.set_config", first_body_line, re.IGNORECASE):
        preamble_set.append(s_stripped)
        continue

    # CREATE FUNCTION
    m = re.match(r"^CREATE\s+(?:OR\s+REPLACE\s+)?FUNCTION\s+public\.([\w_]+)\s*\(", first_body_line, re.IGNORECASE)
    if m:
        name = m.group(1)
        functions.setdefault(name, []).append(s_stripped)
        continue

    # COMMENT ON FUNCTION
    m = re.match(r"^COMMENT\s+ON\s+FUNCTION\s+public\.([\w_]+)", first_body_line, re.IGNORECASE)
    if m:
        name = m.group(1)
        functions.setdefault(name, []).append(s_stripped)
        continue

    # CREATE TABLE
    m = re.match(r"^CREATE\s+TABLE\s+public\.([\w_]+)\s*\(", first_body_line, re.IGNORECASE)
    if m:
        name = m.group(1)
        t = init_table(name)
        t["create"] = s_stripped
        continue

    # CREATE SEQUENCE / ALTER SEQUENCE
    m = re.match(r"^(?:CREATE|ALTER)\s+SEQUENCE\s+public\.([\w_]+_seq)", first_body_line, re.IGNORECASE)
    if m:
        seq_name = m.group(1)
        # Prefer mapping built from ALTER SEQUENCE OWNED BY statements
        if seq_name in seq_to_table:
            table_name = seq_to_table[seq_name]
        else:
            # Fallback: try matching against known table names by longest prefix
            candidates = [t for t in TABLE_ORDER if seq_name.startswith(t + "_")]
            table_name = max(candidates, key=len) if candidates else seq_name
        t = init_table(table_name)
        # Distinguish CREATE SEQUENCE (independent, must run before CREATE TABLE)
        # from ALTER SEQUENCE ... OWNED BY (depends on table, must run after)
        is_alter = first_body_line.upper().startswith("ALTER")
        if is_alter:
            t["alter_sequences"].append(s_stripped)
        else:
            t["create_sequences"].append(s_stripped)
        continue

    # ALTER TABLE ... (default nextval, primary key, etc.)
    m = re.match(r"^ALTER\s+TABLE\s+(?:ONLY\s+)?public\.([\w_]+)", first_body_line, re.IGNORECASE)
    if m:
        name = m.group(1)
        t = init_table(name)
        if "ADD CONSTRAINT" in s_stripped.upper():
            t["constraints"].append(s_stripped)
        else:
            t["alters"].append(s_stripped)
        continue

    # CREATE [UNIQUE] INDEX
    m = re.match(r"^CREATE\s+(?:UNIQUE\s+)?INDEX\s+\w+\s+ON\s+(?:ONLY\s+)?public\.([\w_]+)", first_body_line, re.IGNORECASE)
    if m:
        name = m.group(1)
        t = init_table(name)
        t["indexes"].append(s_stripped)
        continue

    # COMMENT ON TABLE/COLUMN
    m = re.match(r"^COMMENT\s+ON\s+(?:TABLE|COLUMN)\s+public\.([\w_]+)", first_body_line, re.IGNORECASE)
    if m:
        name = m.group(1)
        t = init_table(name)
        t["comments"].append(s_stripped)
        continue

    extra.append(s_stripped)

print(f"Tables: {len(tables)}, Functions: {len(functions)}, Extra: {len(extra)}")

# Write per-table files
for idx, name in enumerate(TABLE_ORDER, start=1):
    if name not in tables:
        print(f"WARN: table '{name}' not found in dump, skipping")
        continue
    t = tables[name]
    out = []
    out.append(f"-- ===== {name} =====")
    out.append(f"-- Generated from pg_dump --schema-only of mooket_db (production)")
    out.append("")
    if t["create_sequences"]:
        out.append("-- Sequences (must exist before CREATE TABLE references them)")
        for s in t["create_sequences"]:
            out.append(s)
            out.append("")
    if t["create"]:
        out.append("-- Table")
        out.append(t["create"])
        out.append("")
    if t["alter_sequences"]:
        out.append("-- Link sequences to columns (must run after CREATE TABLE)")
        for s in t["alter_sequences"]:
            out.append(s)
            out.append("")
    if t["alters"]:
        out.append("-- ALTER TABLE (column defaults, etc.)")
        for s in t["alters"]:
            out.append(s)
        out.append("")
    if t["constraints"]:
        out.append("-- Constraints (PK / UK / FK)")
        for s in t["constraints"]:
            out.append(s)
        out.append("")
    if t["indexes"]:
        out.append("-- Indexes")
        for s in t["indexes"]:
            out.append(s)
        out.append("")
    if t["comments"]:
        out.append("-- Comments")
        for s in t["comments"]:
            out.append(s)
        out.append("")
    fname = f"tables/{idx:02d}_{name}.sql"
    (OUT_DIR / fname).write_text("\n".join(out), encoding="utf-8")
    print(f"  wrote {fname}")

# Write function files
for name in sorted(functions.keys()):
    out = [f"-- ===== Function: {name} =====", ""]
    out.extend(functions[name])
    out.append("")
    (OUT_DIR / f"functions/{name}.sql").write_text("\n".join(out), encoding="utf-8")
    print(f"  wrote functions/{name}.sql")

# Master file: includes everything in dependency order
master = []
master.append("-- ===========================================================")
master.append("-- mooket_db schema initialization (regenerated from production)")
master.append("-- ")
master.append("-- Usage:")
master.append("--   psql -U <user> -d <db> -f schema.sql")
master.append("-- ")
master.append("-- Generated by .tmp/parse_schema.py from pg_dump --schema-only")
master.append("-- ===========================================================")
master.append("")
master.append("-- Preamble: server settings (safe defaults, won't affect existing sessions)")
master.append("SET statement_timeout = 0;")
master.append("SET lock_timeout = 0;")
master.append("SET idle_in_transaction_session_timeout = 0;")
master.append("SET client_encoding = 'UTF8';")
master.append("SET standard_conforming_strings = on;")
master.append("SET check_function_bodies = false;")
master.append("SET xmloption = content;")
master.append("SET client_min_messages = warning;")
master.append("SET row_security = off;")
master.append("")

master.append("-- ===== FUNCTIONS =====")
for name in sorted(functions.keys()):
    master.append(f"-- ----- {name} -----")
    master.extend(functions[name])
    master.append("")

master.append("-- ===== TABLES (in dependency order) =====")
for idx, name in enumerate(TABLE_ORDER, start=1):
    if name not in tables:
        continue
    t = tables[name]
    master.append(f"-- ----- {idx:02d}. {name} -----")
    for s in t["create_sequences"]:
        master.append(s)
        master.append("")
    if t["create"]:
        master.append(t["create"])
        master.append("")
    for s in t["alter_sequences"]:
        master.append(s)
        master.append("")
    for s in t["alters"]:
        master.append(s)
    if t["alters"]:
        master.append("")
    for s in t["constraints"]:
        master.append(s)
    if t["constraints"]:
        master.append("")
    for s in t["indexes"]:
        master.append(s)
    if t["indexes"]:
        master.append("")
    for s in t["comments"]:
        master.append(s)
    master.append("")

(OUT_DIR / "schema.sql").write_text("\n".join(master), encoding="utf-8")
print(f"  wrote schema.sql ({len(master)} lines)")

if extra:
    print(f"\nWARN: {len(extra)} unclassified statements:")
    for s in extra[:5]:
        print(f"  -> {s[:80]}")
