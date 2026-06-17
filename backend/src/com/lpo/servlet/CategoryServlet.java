package com.lpo.servlet;

import com.lpo.util.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.*;
import java.util.*;

@WebServlet("/api/categories/*")
public class CategoryServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();
        if (pathInfo == null) pathInfo = "";
        System.out.println("[CategoryServlet] GET request: " + pathInfo);

        try {
            if (pathInfo.equals("") || pathInfo.equals("/")) {
                getCategories(req, resp);
            } else {
                ResponseUtil.error(resp, 404, "接口不存在");
            }
        } catch (Exception e) {
            e.printStackTrace();
            ResponseUtil.error(resp, 500, "服务器内部错误");
        }
    }

    private void getCategories(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        System.out.println("[CategoryServlet] 获取分类列表");
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DbUtil.getConnection();
            String sql = "SELECT * FROM categories ORDER BY sort_order ASC";
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();

            List<Map<String, Object>> list = new ArrayList<>();
            while (rs.next()) {
                Map<String, Object> cat = new HashMap<>();
                cat.put("id", rs.getInt("id"));
                cat.put("name", rs.getString("name"));
                cat.put("icon", rs.getString("icon"));
                cat.put("sort_order", rs.getInt("sort_order"));
                list.add(cat);
            }
            System.out.println("[CategoryServlet] 返回分类: " + list.size() + "条");
            ResponseUtil.success(resp, list);
        } catch (SQLException e) {
            e.printStackTrace();
            ResponseUtil.error(resp, 500, "获取分类失败");
        } finally {
            DbUtil.close(conn, ps, rs);
        }
    }
}