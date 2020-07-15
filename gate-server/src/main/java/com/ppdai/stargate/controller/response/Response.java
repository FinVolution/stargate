package com.ppdai.stargate.controller.response;

public class Response<T> {

    private Integer code;
    private String message;
    private T detail;
    private String stackTrace;

    public static Response mark(MessageType type) {
        return mark(type, null);
    }

    public static <T> Response<T> mark(MessageType type, T detail) {
        Response<T> response = new Response<>();
        response.code = type.getCode();
        response.message = type.getMsg();
        response.detail = detail;
        return response;
    }

    public static <T> Response<T> emark(MessageType type, T detail, String stackTrace) {
        Response<T> response = new Response<>();
        response.code = type.getCode();
        response.message = type.getMsg();
        response.detail = detail;
        response.stackTrace = stackTrace;
        return response;
    }

    public static Response<String> mark(MessageType type, String message, Object... params){
        Response response;

        if (params != null && params.length > 0) {
            String formatMessage = String.format(message, params);
            response = Response.mark(type, formatMessage);
        } else {
            response = Response.mark(type, message);
        }

        return response;
    }

    public static <T> Response<T> success(T detail) {
        Response<T> response = new Response<>();
        response.code = MessageType.SUCCESS.getCode();
        response.message = MessageType.SUCCESS.getMsg();
        response.detail = detail;
        return response;
    }

    public static <T> Response<T> error(String message, T detail) {
        Response<T> response = new Response<>();
        response.code = MessageType.ERROR.getCode();
        response.message = message;
        response.detail = detail;
        return response;
    }

    public static <T> Response<T> error(T detail) {
        Response<T> response = new Response<>();
        response.code = MessageType.ERROR.getCode();
        response.message = MessageType.ERROR.getMsg();
        response.detail = detail;
        return response;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getDetails() {
        return detail;
    }

    public void setDetails(T result) {
        this.detail = result;
    }

    public String getStackTrace() {
        return stackTrace;
    }

    public void setStackTrace(String stackTrace) {
        this.stackTrace = stackTrace;
    }
}
