package com.lpo.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

public class CorsFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        System.out.println("[CorsFilter] 初始化 CORS 过滤器");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletResponse resp = (HttpServletResponse) response;
        HttpServletRequest req = (HttpServletRequest) request;

        // 获取请求来源
        String origin = req.getHeader("Origin");
        System.out.println("[CorsFilter] 收到请求: " + req.getMethod() + " " + req.getRequestURI() + " 来源: " + origin);

        // 允许所有来源
        if (origin != null && !origin.isEmpty()) {
            resp.setHeader("Access-Control-Allow-Origin", origin);
        } else {
            resp.setHeader("Access-Control-Allow-Origin", "*");
        }
        resp.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        resp.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization, X-Requested-With");
        resp.setHeader("Access-Control-Allow-Credentials", "true");
        resp.setHeader("Access-Control-Max-Age", "3600");
        resp.setContentType("application/json;charset=UTF-8");
        resp.setCharacterEncoding("UTF-8");
        req.setCharacterEncoding("UTF-8");

        // 处理预检请求
        if ("OPTIONS".equalsIgnoreCase(req.getMethod())) {
            System.out.println("[CorsFilter] 处理 OPTIONS 预检请求");
            resp.setStatus(HttpServletResponse.SC_OK);
            return;
        }               

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        System.out.println("[CorsFilter] 销毁 CORS 过滤器");
    }
}