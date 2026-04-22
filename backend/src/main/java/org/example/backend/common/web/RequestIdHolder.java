package org.example.backend.common.web;

public final class RequestIdHolder {

    private static final ThreadLocal<String> REQUEST_ID_HOLDER = new ThreadLocal<>();

    private RequestIdHolder() {
    }

    public static void setRequestId(String requestId) {
        REQUEST_ID_HOLDER.set(requestId);
    }

    public static String getRequestId() {
        String requestId = REQUEST_ID_HOLDER.get();
        return requestId == null || requestId.isBlank() ? "unknown" : requestId;
    }

    public static void clear() {
        REQUEST_ID_HOLDER.remove();
    }
}
