package com.byoskill.speedrester.model;

import org.apache.http.HttpStatus;

public enum HttpStatusCode {
    CONTINUE(100, "Continue"),
    SWITCHING_PROTOCOLS(101, "Switching Protocols"),
    PROCESSING(102, "Processing"),
    OK(200, "OK"),
    CREATED(201, "Created"),
    ACCEPTED(202, "Accepted"),
    NON_AUTHORITATIVE_INFORMATION(203, "Non Authoritative Information"),
    NO_CONTENT(204, "No Content"),
    RESET_CONTENT(205, "Reset Content"),
    PARTIAL_CONTENT(206, "Partial Content"),
    MULTI_STATUS(207, "Multi-Status"),
    MULTIPLE_CHOICES(300, "Multiple Choices"),
    MOVED_PERMANENTLY(301, "Moved Permanently"),
    MOVED_TEMPORARILY(302, "Moved Temporarily"),
    FOUND(302, "Found"),
    SEE_OTHER(303, "See Other"),
    NOT_MODIFIED(304, "Not Modified"),
    USE_PROXY(305, "Use Proxy"),
    TEMPORARY_REDIRECT(307, "Temporary Redirect"),
    PERMANET_REDIRECT(308, "Permanent Redirect"),
    PERMANENT_REDIRECT(308, "Permanent Redirect"),
    BAD_REQUEST(400, "Bad Request"),
    UNAUTHORIZED(401, "Unauthorized"),
    PAYMENT_REQUIRED(402, "Payment Required"),
    FORBIDDEN(403, "Forbidden"),
    NOT_FOUND(404, "Not Found"),
    METHOD_NOT_ALLOWED(405, "Method Not Allowed"),
    NOT_ACCEPTABLE(406, "Not Acceptable"),
    PROXY_AUTHENTICATION_REQUIRED(407, "Proxy Authentication Required"),
    REQUEST_TIMEOUT(408, "Request Timeout"),
    CONFLICT(409, "Conflict"),
    GONE(410, "Gone"),
    LENGTH_REQUIRED(411, "Length Required"),
    PRECONDITION_FAILED(412, "Precondition Failed"),
    PAYLOAD_TOO_LARGE(413, "Payload Too Large"),
    URI_TOO_LONG(414, "URI Too Long"),
    UNSUPPORTED_MEDIA_TYPE(415, "Unsupported Media Type"),
    RANGE_NOT_SATISFIABLE(416, "Range Not Satisfiable"),
    EXPECTATION_FAILED(417, "Expectation Failed"),
    IM_A_TEAPOT(418, "I'm a Teapot"),
    ENHANCE_YOUR_CALM(420, "Enhance your Calm"),
    MISDIRECTED_REQUEST(421, "Misdirected Request"),
    UNPROCESSABLE_ENTITY(422, "Unprocessable Entity"),
    LOCKED(423, "Locked"),
    FAILED_DEPENDENCY(424, "Failed Dependency"),
    UPGRADE_REQUIRED(426, "Upgrade Required"),
    PRECONDITION_REQUIRED(428, "Precondition Required"),
    TOO_MANY_REQUESTS(429, "Too Many Requests"),
    REQUEST_HEADER_FIELDS_TOO_LARGE(431, "Request Header Fields Too Large"),
    UNAVAILABLE_FOR_LEGAL_REASONS(451, "Unavailable for Legal Reason"),
    INTERNAL_SERVER_ERROR(500, "Server Error"),
    NOT_IMPLEMENTED(501, "Not Implemented"),
    BAD_GATEWAY(502, "Bad Gateway"),
    SERVICE_UNAVAILABLE(503, "Service Unavailable"),
    GATEWAY_TIMEOUT(504, "Gateway Timeout"),
    HTTP_VERSION_NOT_SUPPORTED(505, "HTTP Version Not Supported"),
    INSUFFICIENT_STORAGE(507, "Insufficient Storage"),
    LOOP_DETECTED(508, "Loop Detected"),
    NOT_EXTENDED(510, "Not Extended"),
    NETWORK_AUTHENTICATION_REQUIRED(511, "Network Authentication Required");

    private static final HttpStatusCode[] codeMap = new HttpStatusCode[512];

    static {
        final HttpStatusCode[] var0 = values();
        final int              var1 = var0.length;

        for (int var2 = 0; var2 < var1; ++var2) {
            final HttpStatusCode code = var0[var2];
            codeMap[code._code] = code;
        }

    }

    private final int    _code;
    private final String _message;

    HttpStatusCode(final int code, final String message) {
        this._code    = code;
        this._message = message;
    }

    public static String getMessage(final int code) {
        final HttpStatusCode codeEnum = getCode(code);
        return codeEnum != null ? codeEnum.getMessage() : Integer.toString(code);
    }

    public static HttpStatusCode getCode(final int code) {
        return code <= 511 ? codeMap[code] : null;
    }

    public String getMessage() {
        return this._message;
    }

    public int getCode() {
        return this._code;
    }

    public boolean equals(final int code) {
        return this._code == code;
    }

    public String toString() {
        return String.format("[%03d %s]", this._code, this.getMessage());
    }

    public boolean isInformational() {
        return isInformational(this._code);
    }

    public static boolean isInformational(final int code) {
        return 100 <= code && code <= 199;
    }

    public boolean isSuccess() {
        return isSuccess(this._code);
    }

    public static boolean isSuccess(final int code) {
        return 200 <= code && code <= 299;
    }

    public boolean isRedirection() {
        return isRedirection(this._code);
    }

    public static boolean isRedirection(final int code) {
        return 300 <= code && code <= 399;
    }

    public boolean isClientError() {
        return isClientError(this._code);
    }

    public static boolean isClientError(final int code) {
        return 400 <= code && code <= 499;
    }

    public boolean isServerError() {
        return isServerError(this._code);
    }

    public static boolean isServerError(final int code) {
        return 500 <= code && code <= 599;
    }

}