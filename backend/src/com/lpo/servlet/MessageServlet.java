package com.lpo.servlet;

import com.lpo.util.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.*;
import java.util.*;

@WebServlet("/api/messages/*")
public class MessageServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();
        if (pathInfo == null) pathInfo = "";
        System.out.println("[MessageServlet] GET request: " + pathInfo);

        try {
            if (pathInfo.matches("/\\d+")) {
                // GET /api/messages/{itemId} - 获取物品的留言列表
                getItemMessages(req, resp);
            } else if (pathInfo.equals("/user/received")) {
                // GET /api/messages/user/received - 获取用户收到的消息
                getMyMessages(req, resp);
            } else if (pathInfo.equals("/user/sent")) {
                // GET /api/messages/user/sent - 获取用户发送的消息
                getSentMessages(req, resp);
            } else if (pathInfo.equals("/unread/count")) {
                // GET /api/messages/unread/count - 获取未读消息数
                getUnreadCount(req, resp);
            } else if (pathInfo.equals("") || pathInfo.equals("/")) {
                getMessages(req, resp);
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
        System.out.println("[MessageServlet] POST request: " + pathInfo);

        try {
            if (pathInfo.equals("") || pathInfo.equals("/")) {
                sendMessage(req, resp);
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
        System.out.println("[MessageServlet] PUT request: " + pathInfo);

        try {
            if (pathInfo.matches("/\\d+/read")) {
                // PUT /api/messages/{id}/read - 标记消息已读
                markRead(req, resp);
            } else if (pathInfo.equals("/read")) {
                markAsRead(req, resp);
            } else {
                ResponseUtil.error(resp, 404, "接口不存在: " + pathInfo);
            }
        } catch (Exception e) {
            e.printStackTrace();
            ResponseUtil.error(resp, 500, "服务器内部错误");
        }
    }

    // ==================== 获取物品的留言列表 ====================
    private void getItemMessages(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();
        int itemId = Integer.parseInt(pathInfo.substring(1));
        System.out.println("[MessageServlet] getItemMessages - itemId=" + itemId);

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DbUtil.getConnection();
            String sql = "SELECT m.*, u_sender.username as sender_username, u_sender.nickname as sender_nickname, u_sender.avatar as sender_avatar, " +
                    "u_receiver.username as receiver_username, u_receiver.nickname as receiver_nickname, u_receiver.avatar as receiver_avatar, " +
                    "i.title as item_title " +
                    "FROM messages m " +
                    "JOIN users u_sender ON m.sender_id = u_sender.id " +
                    "JOIN users u_receiver ON m.receiver_id = u_receiver.id " +
                    "LEFT JOIN items i ON m.item_id = i.id " +
                    "WHERE m.item_id = ? " +
                    "ORDER BY m.created_at ASC";
            ps = conn.prepareStatement(sql);
            ps.setInt(1, itemId);
            rs = ps.executeQuery();

            List<Map<String, Object>> list = new ArrayList<>();
            while (rs.next()) {
                list.add(buildMessageMap(rs));
            }
            System.out.println("[MessageServlet] getItemMessages 返回: " + list.size() + "条");
            ResponseUtil.success(resp, list);
        } catch (SQLException e) {
            e.printStackTrace();
            ResponseUtil.error(resp, 500, "获取留言列表失败");
        } finally {
            DbUtil.close(conn, ps, rs);
        }
    }

    // ==================== 获取用户收到的消息 ====================
    private void getMyMessages(HttpServletRequest req, HttpServletResponse resp) throws IOException {
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
        System.out.println("[MessageServlet] getMyMessages - userId=" + userId);

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DbUtil.getConnection();
            String sql = "SELECT m.*, u_sender.username as sender_username, u_sender.nickname as sender_nickname, u_sender.avatar as sender_avatar, " +
                    "u_receiver.username as receiver_username, u_receiver.nickname as receiver_nickname, u_receiver.avatar as receiver_avatar, " +
                    "i.title as item_title " +
                    "FROM messages m " +
                    "JOIN users u_sender ON m.sender_id = u_sender.id " +
                    "JOIN users u_receiver ON m.receiver_id = u_receiver.id " +
                    "LEFT JOIN items i ON m.item_id = i.id " +
                    "WHERE m.receiver_id = ? " +
                    "ORDER BY m.created_at DESC LIMIT 100";
            ps = conn.prepareStatement(sql);
            ps.setInt(1, userId);
            rs = ps.executeQuery();

            List<Map<String, Object>> list = new ArrayList<>();
            while (rs.next()) {
                list.add(buildMessageMap(rs));
            }
            System.out.println("[MessageServlet] getMyMessages 返回: " + list.size() + "条");
            ResponseUtil.success(resp, list);
        } catch (SQLException e) {
            e.printStackTrace();
            ResponseUtil.error(resp, 500, "获取消息列表失败");
        } finally {
            DbUtil.close(conn, ps, rs);
        }
    }

    // ==================== 获取用户发送的消息 ====================
    private void getSentMessages(HttpServletRequest req, HttpServletResponse resp) throws IOException {
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
        System.out.println("[MessageServlet] getSentMessages - userId=" + userId);

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DbUtil.getConnection();
            String sql = "SELECT m.*, u_sender.username as sender_username, u_sender.nickname as sender_nickname, u_sender.avatar as sender_avatar, " +
                    "u_receiver.username as receiver_username, u_receiver.nickname as receiver_nickname, u_receiver.avatar as receiver_avatar, " +
                    "i.title as item_title " +
                    "FROM messages m " +
                    "JOIN users u_sender ON m.sender_id = u_sender.id " +
                    "JOIN users u_receiver ON m.receiver_id = u_receiver.id " +
                    "LEFT JOIN items i ON m.item_id = i.id " +
                    "WHERE m.sender_id = ? " +
                    "ORDER BY m.created_at DESC LIMIT 100";
            ps = conn.prepareStatement(sql);
            ps.setInt(1, userId);
            rs = ps.executeQuery();

            List<Map<String, Object>> list = new ArrayList<>();
            while (rs.next()) {
                list.add(buildMessageMap(rs));
            }
            System.out.println("[MessageServlet] getSentMessages 返回: " + list.size() + "条");
            ResponseUtil.success(resp, list);
        } catch (SQLException e) {
            e.printStackTrace();
            ResponseUtil.error(resp, 500, "获取发送消息列表失败");
        } finally {
            DbUtil.close(conn, ps, rs);
        }
    }

    // ==================== 获取消息列表（通用） ====================
    private void getMessages(HttpServletRequest req, HttpServletResponse resp) throws IOException {
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
        System.out.println("[MessageServlet] getMessages - userId=" + userId);

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DbUtil.getConnection();
            String sql = "SELECT m.*, u_sender.username as sender_username, u_sender.nickname as sender_nickname, u_sender.avatar as sender_avatar, " +
                    "u_receiver.username as receiver_username, u_receiver.nickname as receiver_nickname, u_receiver.avatar as receiver_avatar, " +
                    "i.title as item_title " +
                    "FROM messages m " +
                    "JOIN users u_sender ON m.sender_id = u_sender.id " +
                    "JOIN users u_receiver ON m.receiver_id = u_receiver.id " +
                    "LEFT JOIN items i ON m.item_id = i.id " +
                    "WHERE m.sender_id = ? OR m.receiver_id = ? " +
                    "ORDER BY m.created_at DESC LIMIT 100";
            ps = conn.prepareStatement(sql);
            ps.setInt(1, userId);
            ps.setInt(2, userId);
            rs = ps.executeQuery();

            List<Map<String, Object>> list = new ArrayList<>();
            while (rs.next()) {
                list.add(buildMessageMap(rs));
            }
            System.out.println("[MessageServlet] getMessages 返回: " + list.size() + "条");
            ResponseUtil.success(resp, list);
        } catch (SQLException e) {
            e.printStackTrace();
            ResponseUtil.error(resp, 500, "获取消息失败");
        } finally {
            DbUtil.close(conn, ps, rs);
        }
    }

    // ==================== 发送消息 ====================
    private void sendMessage(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String authHeader = req.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            ResponseUtil.unauthorized(resp);
            return;
        }
        String token = authHeader.substring(7);
        Integer senderId = JwtUtil.getUserId(token);
        if (senderId == null) {
            ResponseUtil.unauthorized(resp);
            return;
        }

        Map<String, Object> body = JsonUtil.toMap(readBody(req));
        // 兼容驼峰和下划线命名
        Object receiverIdObj = body.get("receiverId");
        if (receiverIdObj == null) receiverIdObj = body.get("receiver_id");
        Integer receiverId = receiverIdObj != null ? ((Number) receiverIdObj).intValue() : null;
        
        Object itemIdObj = body.get("itemId");
        if (itemIdObj == null) itemIdObj = body.get("item_id");
        Integer itemId = itemIdObj != null ? ((Number) itemIdObj).intValue() : null;
        
        String content = (String) body.get("content");
        System.out.println("[MessageServlet] sendMessage - senderId=" + senderId + ", receiverId=" + receiverId + ", itemId=" + itemId);

        if (receiverId == null || content == null || content.trim().isEmpty()) {
            ResponseUtil.error(resp, 400, "接收者和内容不能为空");
            return;
        }

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DbUtil.getConnection();
            String sql = "INSERT INTO messages (sender_id, receiver_id, item_id, content, is_read, created_at) VALUES (?, ?, ?, ?, 0, NOW())";
            ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, senderId);
            ps.setInt(2, receiverId);
            if (itemId != null) {
                ps.setInt(3, itemId);
            } else {
                ps.setNull(3, Types.INTEGER);
            }
            ps.setString(4, content.trim());
            ps.executeUpdate();

            // 获取自增ID
            rs = ps.getGeneratedKeys();
            int newId = 0;
            if (rs.next()) {
                newId = rs.getInt(1);
            }
            DbUtil.close(null, ps, rs);

            // 查询新建的消息详情返回给前端
            String querySql = "SELECT m.*, " +
                    "u_sender.username as sender_username, u_sender.nickname as sender_nickname, u_sender.avatar as sender_avatar, " +
                    "u_receiver.username as receiver_username, u_receiver.nickname as receiver_nickname, u_receiver.avatar as receiver_avatar, " +
                    "i.title as item_title " +
                    "FROM messages m " +
                    "JOIN users u_sender ON m.sender_id = u_sender.id " +
                    "JOIN users u_receiver ON m.receiver_id = u_receiver.id " +
                    "LEFT JOIN items i ON m.item_id = i.id " +
                    "WHERE m.id = ?";
            ps = conn.prepareStatement(querySql);
            ps.setInt(1, newId);
            rs = ps.executeQuery();

            if (rs.next()) {
                Map<String, Object> msg = buildMessageMap(rs);
                System.out.println("[MessageServlet] sendMessage 成功, 返回消息 id=" + newId);
                ResponseUtil.success(resp, msg);
            } else {
                System.out.println("[MessageServlet] sendMessage 成功但查询失败");
                ResponseUtil.success(resp);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            ResponseUtil.error(resp, 500, "发送消息失败");
        } finally {
            DbUtil.close(conn, ps, rs);
        }
    }

    // ==================== 标记已读（批量） ====================
    private void markAsRead(HttpServletRequest req, HttpServletResponse resp) throws IOException {
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
        Integer messageId = body.get("message_id") != null ? ((Double) body.get("message_id")).intValue() : null;
        System.out.println("[MessageServlet] markAsRead - userId=" + userId + ", messageId=" + messageId);

        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DbUtil.getConnection();
            String sql;
            if (messageId != null) {
                sql = "UPDATE messages SET is_read = 1 WHERE id = ? AND receiver_id = ?";
                ps = conn.prepareStatement(sql);
                ps.setInt(1, messageId);
                ps.setInt(2, userId);
            } else {
                sql = "UPDATE messages SET is_read = 1 WHERE receiver_id = ?";
                ps = conn.prepareStatement(sql);
                ps.setInt(1, userId);
            }
            ps.executeUpdate();

            System.out.println("[MessageServlet] markAsRead 成功");
            ResponseUtil.success(resp);
        } catch (SQLException e) {
            e.printStackTrace();
            ResponseUtil.error(resp, 500, "标记已读失败");
        } finally {
            DbUtil.close(conn, ps);
        }
    }

    // ==================== 标记单条消息已读 ====================
    private void markRead(HttpServletRequest req, HttpServletResponse resp) throws IOException {
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

        String pathInfo = req.getPathInfo();
        int messageId = Integer.parseInt(pathInfo.substring(1, pathInfo.indexOf("/read")));
        System.out.println("[MessageServlet] markRead - messageId=" + messageId + ", userId=" + userId);

        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DbUtil.getConnection();
            String sql = "UPDATE messages SET is_read = 1 WHERE id = ? AND receiver_id = ?";
            ps = conn.prepareStatement(sql);
            ps.setInt(1, messageId);
            ps.setInt(2, userId);
            ps.executeUpdate();

            System.out.println("[MessageServlet] markRead 成功");
            ResponseUtil.success(resp);
        } catch (SQLException e) {
            e.printStackTrace();
            ResponseUtil.error(resp, 500, "标记已读失败");
        } finally {
            DbUtil.close(conn, ps);
        }
    }

    // ==================== 获取未读数量 ====================
    private void getUnreadCount(HttpServletRequest req, HttpServletResponse resp) throws IOException {
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
        System.out.println("[MessageServlet] getUnreadCount - userId=" + userId);

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DbUtil.getConnection();
            String sql = "SELECT COUNT(*) as count FROM messages WHERE receiver_id = ? AND is_read = 0";
            ps = conn.prepareStatement(sql);
            ps.setInt(1, userId);
            rs = ps.executeQuery();

            Map<String, Object> data = new HashMap<>();
            if (rs.next()) {
                data.put("count", rs.getInt("count"));
            } else {
                data.put("count", 0);
            }
            System.out.println("[MessageServlet] getUnreadCount 返回: " + data.get("count"));
            ResponseUtil.success(resp, data);
        } catch (SQLException e) {
            e.printStackTrace();
            ResponseUtil.error(resp, 500, "获取未读数量失败");
        } finally {
            DbUtil.close(conn, ps, rs);
        }
    }

    // ==================== 构建驼峰命名的消息Map ====================
    private Map<String, Object> buildMessageMap(ResultSet rs) throws SQLException {
        Map<String, Object> msg = new HashMap<>();
        msg.put("id", rs.getInt("id"));
        msg.put("senderId", rs.getInt("sender_id"));
        msg.put("receiverId", rs.getInt("receiver_id"));
        msg.put("itemId", rs.getInt("item_id"));
        msg.put("content", rs.getString("content"));
        msg.put("isRead", rs.getInt("is_read"));
        msg.put("createdAt", rs.getString("created_at"));
        
        try { msg.put("senderName", rs.getString("sender_nickname") != null ? rs.getString("sender_nickname") : rs.getString("sender_username")); } catch (Exception e) { msg.put("senderName", rs.getString("sender_username")); }
        try { msg.put("senderUsername", rs.getString("sender_username")); } catch (Exception e) {}
        try { msg.put("senderAvatar", rs.getString("sender_avatar")); } catch (Exception e) {}
        try { msg.put("receiverName", rs.getString("receiver_nickname") != null ? rs.getString("receiver_nickname") : rs.getString("receiver_username")); } catch (Exception e) { msg.put("receiverName", rs.getString("receiver_username")); }
        try { msg.put("receiverUsername", rs.getString("receiver_username")); } catch (Exception e) {}
        try { msg.put("receiverAvatar", rs.getString("receiver_avatar")); } catch (Exception e) {}
        try { msg.put("itemTitle", rs.getString("item_title")); } catch (Exception e) {}
        
        return msg;
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