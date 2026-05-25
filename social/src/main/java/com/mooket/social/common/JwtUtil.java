package com.mooket.social.common;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * JWT utilities.
 */
public class JwtUtil {

    private static final String SECRET = "MooketMaxSecretKeyForJWTTokenGeneration2024!";
    private static final long EXPIRATION_MS = 14L * 24 * 60 * 60 * 1000;
    private static final SecretKey KEY = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
    private static final String SESSION_ID_CLAIM = "sessionId";
    private static final Map<Long, String> ACTIVE_SESSION_BY_USER = new ConcurrentHashMap<>();

    public static final String FORCED_LOGOUT_MESSAGE =
            "您的账号已在另一台设备登录，当前登录状态已失效。为保障账号安全，请重新登录。如非本人操作，请及时修改密码。";
    public static final String TOKEN_INVALID_MESSAGE = "登录状态已失效，请重新登录";

    private JwtUtil() {
    }

    public static String generateToken(Long userId, String phone) {
        String sessionId = UUID.randomUUID().toString();
        ACTIVE_SESSION_BY_USER.put(userId, sessionId);

        Date now = new Date();
        Date expiry = new Date(now.getTime() + EXPIRATION_MS);

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("phone", phone)
                .claim(SESSION_ID_CLAIM, sessionId)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(KEY)
                .compact();
    }

    public static Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(KEY)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public static Long getUserId(String token) {
        Claims claims = parseToken(token);
        return Long.parseLong(claims.getSubject());
    }

    public static String getPhone(String token) {
        Claims claims = parseToken(token);
        return claims.get("phone", String.class);
    }

    public static boolean validateToken(String token) {
        return validateTokenDetailed(token).isValid();
    }

    public static ValidationResult validateTokenDetailed(String token) {
        try {
            Claims claims = parseToken(token);
            if (claims.getExpiration().before(new Date())) {
                return ValidationResult.invalid(TOKEN_INVALID_MESSAGE);
            }

            Long userId = Long.parseLong(claims.getSubject());
            String tokenSessionId = claims.get(SESSION_ID_CLAIM, String.class);
            String activeSessionId = ACTIVE_SESSION_BY_USER.get(userId);

            if (activeSessionId == null) {
                if (tokenSessionId != null && !tokenSessionId.isBlank()) {
                    ACTIVE_SESSION_BY_USER.putIfAbsent(userId, tokenSessionId);
                }
                return ValidationResult.valid();
            }

            if (tokenSessionId == null || tokenSessionId.isBlank() || !activeSessionId.equals(tokenSessionId)) {
                return ValidationResult.invalid(FORCED_LOGOUT_MESSAGE);
            }

            return ValidationResult.valid();
        } catch (Exception e) {
            return ValidationResult.invalid(TOKEN_INVALID_MESSAGE);
        }
    }

    public static void invalidateSession(Long userId) {
        if (userId != null) {
            ACTIVE_SESSION_BY_USER.remove(userId);
        }
    }

    public static final class ValidationResult {
        private final boolean valid;
        private final String message;

        private ValidationResult(boolean valid, String message) {
            this.valid = valid;
            this.message = message;
        }

        public static ValidationResult valid() {
            return new ValidationResult(true, null);
        }

        public static ValidationResult invalid(String message) {
            return new ValidationResult(false, message);
        }

        public boolean isValid() {
            return valid;
        }

        public String getMessage() {
            return message;
        }
    }
}
