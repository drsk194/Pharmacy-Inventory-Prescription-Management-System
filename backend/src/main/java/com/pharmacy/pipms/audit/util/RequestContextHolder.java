package com.pharmacy.pipms.audit.util;

public class RequestContextHolder {

    private static final ThreadLocal<RequestContext> CONTEXT = new ThreadLocal<>();

    public static void set(RequestContext context) { CONTEXT.set(context); }
    public static RequestContext get() { return CONTEXT.get(); }
    public static void clear() { CONTEXT.remove(); }

    public static class RequestContext {
        private final String ipAddress;
        private final String requestId;

        public RequestContext(String ipAddress, String requestId) {
            this.ipAddress = ipAddress;
            this.requestId = requestId;
        }

        public String getIpAddress() { return ipAddress; }
        public String getRequestId() { return requestId; }
    }
}