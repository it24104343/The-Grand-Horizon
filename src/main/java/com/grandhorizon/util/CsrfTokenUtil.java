package com.grandhorizon.util;

import jakarta.servlet.http.HttpSession;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.UUID;

// CsrfTokenUtil protects form submissions by creating and checking a session-based token.
public final class CsrfTokenUtil {
    private static final String SESSION_ATTRIBUTE = CsrfTokenUtil.class.getName() + ".token";

    private CsrfTokenUtil() {
    }

    public static String getOrCreate(HttpSession session) {
        String token = (String) session.getAttribute(SESSION_ATTRIBUTE);
        if (token == null) {
            token = UUID.randomUUID().toString();
            session.setAttribute(SESSION_ATTRIBUTE, token);
        }
        return token;
    }

    public static boolean isValid(HttpSession session, String submittedToken) {
        if (session == null || submittedToken == null) {
            return false;
        }
        String token = (String) session.getAttribute(SESSION_ATTRIBUTE);
        if (token == null) {
            return false;
        }
        return MessageDigest.isEqual(token.getBytes(StandardCharsets.UTF_8),
                submittedToken.getBytes(StandardCharsets.UTF_8));
    }
}
