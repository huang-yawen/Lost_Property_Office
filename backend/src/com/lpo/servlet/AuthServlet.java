package com.lpo.servlet;

import com.lpo.util.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.*;
import java.util.*;

@WebServlet("/api/auth/*")
public class AuthServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();
        if (pathInfo == null) pathInfo = "";
        System.out.println("[AuthServlet] GET request: " + pathInfo);

        try {
            if (pathInfo.equals("/profile")) {
                getProfile(req, resp);
            } else if (pathInfo.equals("/users")) {
                getUsers(req, resp);
            } else if (pathInfo.equals("/certification/list")) {
                getCertificationList(req, resp);
            } else {
                ResponseUtil.error(resp, 404, "接口不存在: " + pathInfo);
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
        System.out.println("[AuthServlet] POST request: " + pathInfo);

        try {
            if (pathInfo.equals("/login")) {
                login(req, resp);
            } else if (pathInfo.equals("/register")) {
                register(req, resp);
            } else if (pathInfo.equals("/certification")) {
                submitCertification(req, resp);
            } else if (pathInfo.equals("/certification/review")) {
                reviewCertification(req, resp);
            } else if (pathInfo.equals("/reputation")) {
                updateReputation(req, resp);
            } else {
                ResponseUtil.error(resp, 404, "接口不存在: " + pathInfo);
            }
        } catch (Exception e) {
            e.printStackTrace();
            ResponseUtil.error(resp, 500, "服务器内部错误");
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();
        if (pathInfo == null) pathInfo = "";
        System.out.println("[AuthServlet] PUT request: " + pathInfo);

        try {
            if (pathInfo.equals("/profile")) {
                updateProfile(req, resp);
            } else {
                ResponseUtil.error(resp, 404, "接口不存在: " + pathInfo);
            }
        } catch (Exception e) {
            e.printStackTrace();
            ResponseUtil.error(resp, 500, "服务器内部错误");
        }
    }

    // ==================== 用户登录 ====================
    private void login(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Map<String, Object> body = JsonUtil.toMap(readBody(req));
        String username = (String) body.get("username");
        String password = (String) body.get("password");
        System.out.println("[AuthServlet] 登录请求 - username: " + username);

        if (username == null || password == null) {
            ResponseUtil.error(resp, 400, "用户名和密码不能为空");
            return;
        }

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DbUtil.getConnection();
            System.out.println("[AuthServlet] 数据库连接成功，查询用户...");
            String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
            ps = conn.prepareStatement(sql);
            ps.setString(1, username);
            ps.setString(2, password);
            rs = ps.executeQuery();

            if (rs.next()) {
                int userId = rs.getInt("id");
                boolean isAdmin = rs.getInt("is_admin") == 1;
                String token = JwtUtil.generateToken(userId, username, isAdmin);
                System.out.println("[AuthServlet] 登录成功 - userId: " + userId + ", isAdmin: " + isAdmin);

                Map<String, Object> user = new HashMap<>();
                user.put("id", userId);
                user.put("username", rs.getString("username"));
                user.put("nickname", rs.getString("nickname"));
                user.put("avatar", rs.getString("avatar"));
                user.put("email", rs.getString("email"));
                user.put("phone", rs.getString("phone"));
                user.put("campus", rs.getString("campus"));
                user.put("reputation", rs.getInt("reputation"));
                user.put("is_certified", rs.getInt("is_certified"));
                user.put("is_admin", rs.getInt("is_admin"));

                Map<String, Object> data = new HashMap<>();
                data.put("token", token);
                data.put("user", user);
                ResponseUtil.success(resp, data);
            } else {
                System.out.println("[AuthServlet] 登录失败 - 用户名或密码错误");
                ResponseUtil.error(resp, 401, "用户名或密码错误");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("[AuthServlet] 数据库异常: " + e.getMessage());
            ResponseUtil.error(resp, 500, "登录失败");
        } finally {
            DbUtil.close(conn, ps, rs);
        }
    }

    // ==================== 用户注册 ====================
    private void register(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Map<String, Object> body = JsonUtil.toMap(readBody(req));
        String username = (String) body.get("username");
        String password = (String) body.get("password");
        String nickname = (String) body.get("nickname");
        String email = (String) body.get("email");
        String phone = (String) body.get("phone");
        System.out.println("[AuthServlet] 注册请求 - username: " + username + ", nickname: " + nickname);

        if (username == null || password == null) {
            ResponseUtil.error(resp, 400, "用户名和密码不能为空");
            return;
        }

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DbUtil.getConnection();
            // 检查用户名是否已存在
            String checkSql = "SELECT id FROM users WHERE username = ?";
            ps = conn.prepareStatement(checkSql);
            ps.setString(1, username);
            rs = ps.executeQuery();
            if (rs.next()) {
                ResponseUtil.error(resp, 400, "用户名已存在");
                return;
            }
            DbUtil.close(null, ps, rs);

            String sql = "INSERT INTO users (username, password, nickname, email, phone, reputation, is_certified, is_admin, created_at) VALUES (?, ?, ?, ?, ?, 100, 0, 0, NOW())";
            ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, username);
            ps.setString(2, password);
            ps.setString(3, nickname != null ? nickname : username);
            ps.setString(4, email);
            ps.setString(5, phone);
            ps.executeUpdate();

            rs = ps.getGeneratedKeys();
            if (rs.next()) {
                int userId = rs.getInt(1);
                String token = JwtUtil.generateToken(userId, username, false);
                System.out.println("[AuthServlet] 注册成功 - userId: " + userId);

                Map<String, Object> user = new HashMap<>();
                user.put("id", userId);
                user.put("username", username);
                user.put("nickname", nickname != null ? nickname : username);
                user.put("email", email);
                user.put("phone", phone);
                user.put("reputation", 100);
                user.put("is_certified", 0);
                user.put("is_admin", 0);

                Map<String, Object> data = new HashMap<>();
                data.put("token", token);
                data.put("user", user);
                ResponseUtil.success(resp, data);
            } else {
                ResponseUtil.error(resp, 500, "注册失败");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            ResponseUtil.error(resp, 500, "注册失败");
        } finally {
            DbUtil.close(conn, ps, rs);
        }
    }

    // ==================== 获取用户信息 ====================
    private void getProfile(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String authHeader = req.getHeader("Authorization");
        System.out.println("[AuthServlet] 获取用户信息 - Authorization: " + (authHeader != null ? "存在" : "不存在"));
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            ResponseUtil.unauthorized(resp);
            return;
        }
        String token = authHeader.substring(7);
        Integer userId = JwtUtil.getUserId(token);
        System.out.println("[AuthServlet] 获取用户信息 - userId: " + userId);
        if (userId == null) {
            ResponseUtil.unauthorized(resp);
            return;
        }

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DbUtil.getConnection();
            String sql = "SELECT * FROM users WHERE id = ?";
            ps = conn.prepareStatement(sql);
            ps.setInt(1, userId);
            rs = ps.executeQuery();

            if (rs.next()) {
                Map<String, Object> user = new HashMap<>();
                user.put("id", rs.getInt("id"));
                user.put("username", rs.getString("username"));
                user.put("nickname", rs.getString("nickname"));
                user.put("avatar", rs.getString("avatar"));
                user.put("email", rs.getString("email"));
                user.put("phone", rs.getString("phone"));
                user.put("campus", rs.getString("campus"));
                user.put("reputation", rs.getInt("reputation"));
                user.put("is_certified", rs.getInt("is_certified"));
                user.put("is_admin", rs.getInt("is_admin"));
                user.put("created_at", rs.getString("created_at"));
                System.out.println("[AuthServlet] 获取用户信息成功 - username: " + rs.getString("username"));
                ResponseUtil.success(resp, user);
            } else {
                ResponseUtil.error(resp, 404, "用户不存在");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            ResponseUtil.error(resp, 500, "获取用户信息失败");
        } finally {
            DbUtil.close(conn, ps, rs);
        }
    }

    // ==================== 更新用户信息 ====================
    private void updateProfile(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String authHeader = req.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            ResponseUtil.unauthorized(resp);
            return;
        }
        String token = authHeader.substring(7);
        Integer userId = JwtUtil.getUserId(token);
        if (userId == null) {
            ResponseUtil.unauthorized(resp);
            return;
        }

        Map<String, Object> body = JsonUtil.toMap(readBody(req));
        System.out.println("[AuthServlet] 更新用户信息 - userId: " + userId + ", body: " + body);

        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DbUtil.getConnection();
            StringBuilder sql = new StringBuilder("UPDATE users SET ");
            List<Object> params = new ArrayList<>();

            if (body.containsKey("nickname")) {
                sql.append("nickname = ?, ");
                params.add(body.get("nickname"));
            }
            if (body.containsKey("email")) {
                sql.append("email = ?, ");
                params.add(body.get("email"));
            }
            if (body.containsKey("phone")) {
                sql.append("phone = ?, ");
                params.add(body.get("phone"));
            }
            if (body.containsKey("campus")) {
                sql.append("campus = ?, ");
                params.add(body.get("campus"));
            }
            if (body.containsKey("avatar")) {
                sql.append("avatar = ?, ");
                params.add(body.get("avatar"));
            }

            if (params.isEmpty()) {
                ResponseUtil.error(resp, 400, "没有需要更新的字段");
                return;
            }

            sql.delete(sql.length() - 2, sql.length());
            sql.append(" WHERE id = ?");
            params.add(userId);

            ps = conn.prepareStatement(sql.toString());
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            ps.executeUpdate();
            System.out.println("[AuthServlet] 更新用户信息成功 - userId: " + userId);
            ResponseUtil.success(resp);
        } catch (SQLException e) {
            e.printStackTrace();
            ResponseUtil.error(resp, 500, "更新用户信息失败");
        } finally {
            DbUtil.close(conn, ps);
        }
    }

    // ==================== 提交校园认证 ====================
    private void submitCertification(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String authHeader = req.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            ResponseUtil.unauthorized(resp);
            return;
        }
        String token = authHeader.substring(7);
        Integer userId = JwtUtil.getUserId(token);
        if (userId == null) {
            ResponseUtil.unauthorized(resp);
            return;
        }

        Map<String, Object> body = JsonUtil.toMap(readBody(req));
        String realName = (String) body.get("real_name");
        String studentId = (String) body.get("student_id");
        String campus = (String) body.get("campus");
        String idCard = (String) body.get("id_card");
        System.out.println("[AuthServlet] 提交认证 - userId: " + userId + ", realName: " + realName);

        if (realName == null || studentId == null) {
            ResponseUtil.error(resp, 400, "真实姓名和学号不能为空");
            return;
        }

        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DbUtil.getConnection();
            String sql = "INSERT INTO certifications (user_id, real_name, student_id, campus, id_card, status, created_at) VALUES (?, ?, ?, ?, ?, 'pending', NOW())";
            ps = conn.prepareStatement(sql);
            ps.setInt(1, userId);
            ps.setString(2, realName);
            ps.setString(3, studentId);
            ps.setString(4, campus);
            ps.setString(5, idCard);
            ps.executeUpdate();
            System.out.println("[AuthServlet] 提交认证成功 - userId: " + userId);
            ResponseUtil.success(resp);
        } catch (SQLException e) {
            e.printStackTrace();
            ResponseUtil.error(resp, 500, "提交认证失败");
        } finally {
            DbUtil.close(conn, ps);
        }
    }

    // ==================== 获取认证列表（管理员） ====================
    private void getCertificationList(HttpServletRequest req, HttpServletResponse resp) throws IOException {
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
        System.out.println("[AuthServlet] 获取认证列表 - status: " + status);

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
            System.out.println("[AuthServlet] 获取认证列表成功 - 数量: " + list.size());
            ResponseUtil.success(resp, list);
        } catch (SQLException e) {
            e.printStackTrace();
            ResponseUtil.error(resp, 500, "获取认证列表失败");
        } finally {
            DbUtil.close(conn, ps, rs);
        }
    }

    // ==================== 审核认证（管理员） ====================
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

        Map<String, Object> body = JsonUtil.toMap(readBody(req));
        Integer userId = ((Double) body.get("user_id")).intValue();
        String status = (String) body.get("status");
        System.out.println("[AuthServlet] 审核认证 - userId: " + userId + ", status: " + status);

        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DbUtil.getConnection();
            String sql = "UPDATE certifications SET status = ? WHERE user_id = ?";
            ps = conn.prepareStatement(sql);
            ps.setString(1, status);
            ps.setInt(2, userId);
            ps.executeUpdate();
            DbUtil.close(null, ps, null);

            if ("approved".equals(status)) {
                sql = "UPDATE users SET is_certified = 1 WHERE id = ?";
                ps = conn.prepareStatement(sql);
                ps.setInt(1, userId);
                ps.executeUpdate();
            }
            System.out.println("[AuthServlet] 审核认证成功 - userId: " + userId);
            ResponseUtil.success(resp);
        } catch (SQLException e) {
            e.printStackTrace();
            ResponseUtil.error(resp, 500, "审核认证失败");
        } finally {
            DbUtil.close(conn, ps);
        }
    }

    // ==================== 更新信誉积分 ====================
    private void updateReputation(HttpServletRequest req, HttpServletResponse resp) throws IOException {
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

        Map<String, Object> body = JsonUtil.toMap(readBody(req));
        Integer userId = ((Double) body.get("user_id")).intValue();
        Integer reputation = ((Double) body.get("reputation")).intValue();
        System.out.println("[AuthServlet] 更新信誉积分 - userId: " + userId + ", reputation: " + reputation);

        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DbUtil.getConnection();
            String sql = "UPDATE users SET reputation = ? WHERE id = ?";
            ps = conn.prepareStatement(sql);
            ps.setInt(1, reputation);
            ps.setInt(2, userId);
            ps.executeUpdate();
            System.out.println("[AuthServlet] 更新信誉积分成功");
            ResponseUtil.success(resp);
        } catch (SQLException e) {
            e.printStackTrace();
            ResponseUtil.error(resp, 500, "更新信誉积分失败");
        } finally {
            DbUtil.close(conn, ps);
        }
    }

    // ==================== 获取用户列表（公开信息） ====================
    private void getUsers(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        System.out.println("[AuthServlet] 获取用户列表");
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DbUtil.getConnection();
            String sql = "SELECT id, username, nickname, avatar, reputation, campus, is_certified, created_at FROM users ORDER BY created_at DESC";
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();

            List<Map<String, Object>> list = new ArrayList<>();
            while (rs.next()) {
                Map<String, Object> user = new HashMap<>();
                user.put("id", rs.getInt("id"));
                user.put("username", rs.getString("username"));
                user.put("nickname", rs.getString("nickname"));
                user.put("avatar", rs.getString("avatar"));
                user.put("reputation", rs.getInt("reputation"));
                user.put("campus", rs.getString("campus"));
                user.put("is_certified", rs.getInt("is_certified"));
                user.put("created_at", rs.getString("created_at"));
                list.add(user);
            }
            System.out.println("[AuthServlet] 获取用户列表成功 - 数量: " + list.size());
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