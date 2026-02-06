package com.dthvinh.order_service.type;

public class Response {
    public boolean success;
    public String message;
    public Object data;

    public static Response ok(Object data) {
        Response response = new Response();
        response.success = true;
        response.message = "Operation successful";
        response.data = data;
        return response;
    }

    public static Response error(String message) {
        Response response = new Response();
        response.success = false;
        response.message = message;
        response.data = null;
        return response;
    }
}