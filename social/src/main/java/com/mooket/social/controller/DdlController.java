package com.mooket.social.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 临时 DDL/DML 执行控制器（建表后删除）
 */
@RestController
@RequestMapping("/internal")
public class DdlController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @PostMapping("/ddl/execute")
    public Map<String, Object> executeDdl(@RequestBody String sql) {
        Map<String, Object> result = new HashMap<>();
        try {
            if (sql.trim().toUpperCase().startsWith("SELECT")) {
                var list = jdbcTemplate.queryForList(sql);
                result.put("code", 200);
                result.put("message", "查询成功");
                result.put("data", list);
            } else {
                int rows = jdbcTemplate.update(sql);
                result.put("code", 200);
                result.put("message", "执行成功");
                result.put("affectedRows", rows);
            }
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "执行失败: " + e.getMessage());
        }
        return result;
    }
}
