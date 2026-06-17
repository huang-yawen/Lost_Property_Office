package com.lpo.util;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

public class ResponseUtil {

    public static void write(HttpServletResponse resp, int code, String message, Object data) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");
        resp.setCharacterEncoding("UTF-8");
        Map<String, Object> result = new HashMap<>();
        result.put("code", code);
        result.put("message", message);
        if (data != null) {
            result.put("data", data);
        }
        PrintWriter out = resp.getWriter();
        out.write(JsonUtil.toJson(result));
        out.flush();
    }

    public static void success(HttpServletResponse resp, Object data) throws IOException {
        write(resp, 200, "success", data);
    }

    public static void success(HttpServletResponse resp) throws IOException {
        write(resp, 200, "success", null);
    }

    public static void error(HttpServletResponse resp, int code, String message) throws IOException {
        write(resp, code, message, null);
    }

    public static void unauthorized(HttpServletResponse resp) throws IOException {
        write(resp, 401, "未登录或token已过期", null);
    }

    public static void forbidden(HttpServletResponse resp) throws IOException {
        write(resp, 403, "无权限访问", null);
    }
}