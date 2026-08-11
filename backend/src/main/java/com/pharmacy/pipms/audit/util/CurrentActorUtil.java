package com.pharmacy.pipms.audit.util;

import org.springframework.security.core.context.SecurityContextHolder;

// Resolves "who is calling right now" from Spring Security's context,
// avoiding the need to thread Authentication through every service method
// and controller signature just for audit logging.
public class CurrentActorUtil {
    public static String getCurrentUserEmail() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return null;
        }
        return auth.getName();
    }
}