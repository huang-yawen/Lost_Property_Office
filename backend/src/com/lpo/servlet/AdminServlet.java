package com.lpo.servlet;

import com.lpo.util.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.*;
import java.util.*;

@WebServlet("/api/admin/*")
public class AdminServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();
        if (pathInfo == null) pathInfo = "";
        System.out.println("[AdminServlet] GET request: " + pathInfo);

        try {
            if (pathInfo.equals("/certifications")) {
                getCertifications(req, resp);
            } else if (pathInfo.equals("/users")) {
                getUsers(req, resp);
            } else {
                ResponseUtil.error(resp, 404, "接口不存在");
            }
        } catch (Exception e) {
            e.printStackTrace();
            ResponseUtil.error(resp, 500, "服务器内部错误");
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();
        if (pathInfo == null) pathInfo = "";
        System.out.println("[AdminServlet] DELETE request: " + pathInfo);

        try {
            if (pathInfo.matches("/users/\\d+")) {
                deleteUser(req, resp);
            } else {
                ResponseUtil.error(resp, 404, "接口不存在");
            }
        } catch (Exception e) {
            e.printStackTrace();
            ResponseUtil.error(resp, 500, "服务器内部错误");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();
        if (pathInfo == null) pathInfo = "";
        System.out.println("[AdminServlet] POST request: " + pathInfo);

        try {
            if (pathInfo.equals("/login")) {
                login(req, resp);
            } else {
                ResponseUtil.error(resp, 404, "接口不存在");
            }
        } catch (Exception e) {
            e.printStackTrace();
            ResponseUtil.error(resp, 500, "服务器内部错误");
        }
    }

    // ==================== 删除用户（踢出） ====================
    private void deleteUser(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String authHeader = req.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            ResponseUtil.unauthorized(resp);
            return;
        }
        String token = authHeader.substring(7);
        if (!JwtUtil.isAdmin(token)) {
            ResponseUtil.forbidden(resp);
            return;
        }

        String pathInfo = req.getPathInfo();
        int userId = Integer.parseInt(pathInfo.substring(pathInfo.lastIndexOf("/") + 1));
        System.out.println("[AdminServlet] 删除用户 - userId: " + userId);

        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DbUtil.getConnection();
            // 关闭自动提交，开启事务保证数据一致性
            conn.setAutoCommit(false);

            // 1. 将关联到该用户物品的消息的 item_id 置空（避免外键约束阻止删除物品）
            String nullifyMsgItemsSql = "UPDATE messages m JOIN items i ON m.item_id = i.id SET m.item_id = NULL WHERE i.user_id = ?";
            ps = conn.prepareStatement(nullifyMsgItemsSql);
            ps.setInt(1, userId);
            ps.executeUpdate();
            DbUtil.close(null, ps, null);

            // 2. 删除该用户的所有消息（作为发送者或接收者）
            String deleteMsgSql = "DELETE FROM messages WHERE sender_id = ? OR receiver_id = ?";
            ps = conn.prepareStatement(deleteMsgSql);
            ps.setInt(1, userId);
            ps.setInt(2, userId);
            ps.executeUpdate();
            DbUtil.close(null, ps, null);

            // 3. 删除该用户的认证记录
            String deleteCertSql = "DELETE FROM certifications WHERE user_id = ?";
            ps = conn.prepareStatement(deleteCertSql);
            ps.setInt(1, userId);
            ps.executeUpdate();
            DbUtil.close(null, ps, null);

            // 4. 硬删除该用户的所有物品（解除 items.user_id → users.id 的外键约束）
            String deleteItemsSql = "DELETE FROM items WHERE user_id = ?";
            ps = conn.prepareStatement(deleteItemsSql);
            ps.setInt(1, userId);
            ps.executeUpdate();
            DbUtil.close(null, ps, null);

            // 5. 最后删除用户
            String deleteUserSql = "DELETE FROM users WHERE id = ? AND is_admin = 0";
            ps = conn.prepareStatement(deleteUserSql);
            ps.setInt(1, userId);
            int rows = ps.executeUpdate();

            if (rows > 0) {
                conn.commit();
                System.out.println("[AdminServlet] 删除用户成功 - userId: " + userId);
                ResponseUtil.success(resp);
            } else {
                conn.rollback();
                System.out.println("[AdminServlet] 删除用户失败 - 用户不存在或为管理员");
                ResponseUtil.error(resp, 400, "用户不存在或不能删除管理员账号");
            }
        } catch (Exception e) {
            try { if (conn != null) conn.rollback(); } catch (SQLException ignored) {}
            e.printStackTrace();
            ResponseUtil.error(resp, 500, "删除用户失败：" + e.getMessage());
        } finally {
            DbUtil.close(conn, ps);
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();
        if (pathInfo == null) pathInfo = "";
        System.out.println("[AdminServlet] PUT request: " + pathInfo);

        try {
            if (pathInfo.matches("/certifications/\\d+")) {
                reviewCertification(req, resp);
            } else {
                ResponseUtil.error(resp, 404, "接口不存在");
            }
        } catch (Exception e) {
            e.printStackTrace();
            ResponseUtil.error(resp, 500, "服务器内部错误");
        }
    }

    // ==================== 管理员登录 ====================
    private void login(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Map<String, Object> body = JsonUtil.toMap(readBody(req));
        String username = (String) body.get("username");
        String password = (String) body.get("password");
        System.out.println("[AdminServlet] 管理员登录 - username: " + username);

        if (username == null || password == null) {
            ResponseUtil.error(resp, 400, "用户名和密码不能为空");
            return;
        }

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DbUtil.getConnection();
            String sql = "SELECT * FROM users WHERE username = ? AND password = ? AND is_admin = 1";
            ps = conn.prepareStatement(sql);
            ps.setString(1, username);
            ps.setString(2, password);
            rs = ps.executeQuery();

            if (rs.next()) {
                int userId = rs.getInt("id");
                String token = JwtUtil.generateToken(userId, username, true);
                Map<String, Object> user = new HashMap<>();
                user.put("id", userId);
                user.put("username", rs.getString("username"));
                user.put("nickname", rs.getString("nickname"));
                user.put("avatar", rs.getString("avatar"));
                user.put("is_admin", rs.getInt("is_admin"));

                Map<String, Object> data = new HashMap<>();
                data.put("token", token);
                data.put("user", user);
                System.out.println("[AdminServlet] 管理员登录成功 - userId: " + userId);
                ResponseUtil.success(resp, data);
            } else {
                System.out.println("[AdminServlet] 管理员登录失败 - 用户名或密码错误");
                ResponseUtil.error(resp, 401, "管理员用户名或密码错误");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            ResponseUtil.error(resp, 500, "管理员登录失败");
        } finally {
            DbUtil.close(conn, ps, rs);
        }
    }

    // ==================== 审核认证 ====================
    private void reviewCertification(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String authHeader = req.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            ResponseUtil.unauthorized(resp);
            return;
        }
        String token = authHeader.substring(7);
        if (!JwtUtil.isAdmin(token)) {
            ResponseUtil.forbidden(resp);
            return;
        }

        String pathInfo = req.getPathInfo();
        int userId = Integer.parseInt(pathInfo.substring(pathInfo.lastIndexOf("/") + 1));

        Map<String, Object> body = JsonUtil.toMap(readBody(req));
        String status = (String) body.get("status");

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DbUtil.getConnection();
            // 查找该用户的认证记录
            String getSql = "SELECT * FROM certifications WHERE user_id = ? ORDER BY created_at DESC LIMIT 1";
            ps = conn.prepareStatement(getSql);
            ps.setInt(1, userId);
            rs = ps.executeQuery();
            if (!rs.next()) {
                ResponseUtil.error(resp, 404, "认证记录不存在");
                return;
            }
            int certId = rs.getInt("id");
            DbUtil.close(null, ps, rs);

            // 更新认证状态
            String updateSql = "UPDATE certifications SET status = ? WHERE id = ?";
            ps = conn.prepareStatement(updateSql);
            ps.setString(1, status);
            ps.setInt(2, certId);
            ps.executeUpdate();
            DbUtil.close(null, ps, null);

            // 如果通过，更新用户认证状态
            if ("approved".equals(status)) {
                String userSql = "UPDATE users SET is_certified = 1 WHERE id = ?";
                ps = conn.prepareStatement(userSql);
                ps.setInt(1, userId);
                ps.executeUpdate();
            } else if ("rejected".equals(status)) {
                String userSql = "UPDATE users SET is_certified = 0 WHERE id = ?";
                ps = conn.prepareStatement(userSql);
                ps.setInt(1, userId);
                ps.executeUpdate();
            }

            ResponseUtil.success(resp);
        } catch (SQLException e) {
            e.printStackTrace();
            ResponseUtil.error(resp, 500, "审核失败");
        } finally {
            DbUtil.close(conn, ps, rs);
        }
    }

    // ==================== 获取认证列表 ====================
    private void getCertifications(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String authHeader = req.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            ResponseUtil.unauthorized(resp);
            return;
        }
        String token = authHeader.substring(7);
        if (!JwtUtil.isAdmin(token)) {
            ResponseUtil.forbidden(resp);
            return;
        }

        String status = req.getParameter("status");
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DbUtil.getConnection();
            String sql;
            if (status != null && !status.isEmpty()) {
                sql = "SELECT c.*, u.username, u.nickname FROM certifications c JOIN users u ON c.user_id = u.id WHERE c.status = ? ORDER BY c.created_at DESC";
                ps = conn.prepareStatement(sql);
                ps.setString(1, status);
            } else {
                sql = "SELECT c.*, u.username, u.nickname FROM certifications c JOIN users u ON c.user_id = u.id ORDER BY c.created_at DESC";
                ps = conn.prepareStatement(sql);
            }
            rs = ps.executeQuery();

            List<Map<String, Object>> list = new ArrayList<>();
            while (rs.next()) {
                Map<String, Object> item = new HashMap<>();
                item.put("id", rs.getInt("id"));
                item.put("user_id", rs.getInt("user_id"));
                item.put("username", rs.getString("username"));
                item.put("nickname", rs.getString("nickname"));
                item.put("real_name", rs.getString("real_name"));
                item.put("student_id", rs.getString("student_id"));
                item.put("campus", rs.getString("campus"));
                item.put("id_card", rs.getString("id_card"));
                item.put("status", rs.getString("status"));
                item.put("created_at", rs.getString("created_at"));
                list.add(item);
            }
            ResponseUtil.success(resp, list);
        } catch (SQLException e) {
            e.printStackTrace();
            ResponseUtil.error(resp, 500, "获取认证列表失败");
        } finally {
            DbUtil.close(conn, ps, rs);
        }
    }

    // ==================== 获取用户列表 ====================
    private void getUsers(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String authHeader = req.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            ResponseUtil.unauthorized(resp);
            return;
        }
        String token = authHeader.substring(7);
        if (!JwtUtil.isAdmin(token)) {
            ResponseUtil.forbidden(resp);
            return;
        }

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DbUtil.getConnection();
            String sql = "SELECT * FROM users ORDER BY created_at DESC";
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();

            List<Map<String, Object>> list = new ArrayList<>();
            while (rs.next()) {
                Map<String, Object> user = new HashMap<>();
                user.put("id", rs.getInt("id"));
                user.put("username", rs.getString("username"));
                user.put("nickname", rs.getString("nickname"));
                user.put("email", rs.getString("email"));
                user.put("phone", rs.getString("phone"));
                user.put("avatar", rs.getString("avatar"));
                user.put("reputation", rs.getInt("reputation"));
                user.put("campus", rs.getString("campus"));
                user.put("is_certified", rs.getInt("is_certified"));
                user.put("is_admin", rs.getInt("is_admin"));
                user.put("created_at", rs.getString("created_at"));
                list.add(user);
            }
            ResponseUtil.success(resp, list);
        } catch (SQLException e) {
            e.printStackTrace();
            ResponseUtil.error(resp, 500, "获取用户列表失败");
        } finally {
            DbUtil.close(conn, ps, rs);
        }
    }

    private String readBody(HttpServletRequest req) throws IOException {
        StringBuilder sb = new StringBuilder();
        String line;
        try (java.io.BufferedReader reader = req.getReader()) {
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }
        return sb.toString();
    }
}