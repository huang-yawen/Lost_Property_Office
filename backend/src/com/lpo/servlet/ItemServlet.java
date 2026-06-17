package com.lpo.servlet;

import com.lpo.util.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.*;
import java.util.*;

@WebServlet("/api/items/*")
public class ItemServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();
        if (pathInfo == null) pathInfo = "";

        System.out.println("[ItemServlet] GET 请求: " + pathInfo + ", 参数: " + req.getQueryString());

        try {
            if (pathInfo.equals("/user")) {
                getUserItems(req, resp);
            } else if (pathInfo.equals("/user/stats")) {
                getUserStats(req, resp);
            } else if (pathInfo.equals("/search")) {
                searchItems(req, resp);
            } else if (pathInfo.matches("/match/\\d+")) {
                matchItem(req, resp);
            } else if (pathInfo.matches("/\\d+")) {
                getItemDetail(req, resp);
            } else if (pathInfo.equals("") || pathInfo.equals("/")) {
                getItems(req, resp);
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

        System.out.println("[ItemServlet] POST 请求: " + pathInfo);

        try {
            if (pathInfo.equals("") || pathInfo.equals("/")) {
                createItem(req, resp);
            } else {
                ResponseUtil.error(resp, 404, "接口不存在");
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

        System.out.println("[ItemServlet] PUT 请求: " + pathInfo);

        try {
            if (pathInfo.matches("/\\d+/status")) {
                updateItemStatus(req, resp);
            } else if (pathInfo.matches("/\\d+")) {
                updateItem(req, resp);
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

        System.out.println("[ItemServlet] DELETE 请求: " + pathInfo);

        try {
            if (pathInfo.matches("/\\d+")) {
                deleteItem(req, resp);
            } else {
                ResponseUtil.error(resp, 404, "接口不存在");
            }
        } catch (Exception e) {
            e.printStackTrace();
            ResponseUtil.error(resp, 500, "服务器内部错误");
        }
    }

    // ==================== 获取物品列表 ====================
    private void getItems(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        // 兼容前端传参：categoryId 和 category 都支持
        String type = req.getParameter("type");
        String category = req.getParameter("category");
        if (category == null || category.isEmpty()) {
            category = req.getParameter("categoryId");
        }
        String status = req.getParameter("status");
        String pageStr = req.getParameter("page");
        String pageSizeStr = req.getParameter("pageSize");
        String keyword = req.getParameter("keyword");
        String sort = req.getParameter("sort");
        String startTime = req.getParameter("startTime");
        String endTime = req.getParameter("endTime");

        System.out.println("[ItemServlet] getItems - type=" + type + ", category=" + category + ", page=" + pageStr + ", pageSize=" + pageSizeStr + ", keyword=" + keyword + ", sort=" + sort + ", startTime=" + startTime + ", endTime=" + endTime);

        int page = 1;
        int pageSize = 20;
        if (pageStr != null) page = Integer.parseInt(pageStr);
        if (pageSizeStr != null) pageSize = Integer.parseInt(pageSizeStr);
        int offset = (page - 1) * pageSize;

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DbUtil.getConnection();

            // 先查总数
            StringBuilder countSql = new StringBuilder("SELECT COUNT(*) as total FROM items i JOIN users u ON i.user_id = u.id WHERE 1=1");
            List<Object> params = new ArrayList<>();

            if (type != null && !type.isEmpty()) {
                countSql.append(" AND i.type = ?");
                params.add(type);
            }
            if (category != null && !category.isEmpty()) {
                countSql.append(" AND i.category = ?");
                params.add(category);
            }
            if (status != null && !status.isEmpty()) {
                countSql.append(" AND i.status = ?");
                params.add(status);
            }
            if (keyword != null && !keyword.isEmpty()) {
                countSql.append(" AND (i.title LIKE ? OR i.description LIKE ?)");
                String like = "%" + keyword + "%";
                params.add(like);
                params.add(like);
            }
            if (startTime != null && !startTime.isEmpty()) {
                countSql.append(" AND i.created_at >= ?");
                params.add(startTime);
            }
            if (endTime != null && !endTime.isEmpty()) {
                countSql.append(" AND i.created_at <= ?");
                params.add(endTime);
            }
            countSql.append(" AND i.status != 'deleted'");

            ps = conn.prepareStatement(countSql.toString());
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            rs = ps.executeQuery();
            int total = 0;
            if (rs.next()) total = rs.getInt("total");
            DbUtil.close(null, ps, rs);

            // 查列表
            StringBuilder sql = new StringBuilder("SELECT i.*, u.username, u.nickname, u.avatar FROM items i JOIN users u ON i.user_id = u.id WHERE 1=1");
            params.clear();

            if (type != null && !type.isEmpty()) {
                sql.append(" AND i.type = ?");
                params.add(type);
            }
            if (category != null && !category.isEmpty()) {
                sql.append(" AND i.category = ?");
                params.add(category);
            }
            if (status != null && !status.isEmpty()) {
                sql.append(" AND i.status = ?");
                params.add(status);
            }
            if (keyword != null && !keyword.isEmpty()) {
                sql.append(" AND (i.title LIKE ? OR i.description LIKE ?)");
                String like = "%" + keyword + "%";
                params.add(like);
                params.add(like);
            }
            if (startTime != null && !startTime.isEmpty()) {
                sql.append(" AND i.created_at >= ?");
                params.add(startTime);
            }
            if (endTime != null && !endTime.isEmpty()) {
                sql.append(" AND i.created_at <= ?");
                params.add(endTime);
            }
            sql.append(" AND i.status != 'deleted'");

            // 排序
            if ("asc".equals(sort)) {
                sql.append(" ORDER BY i.created_at ASC");
            } else {
                sql.append(" ORDER BY i.created_at DESC");
            }
            sql.append(" LIMIT ? OFFSET ?");
            params.add(pageSize);
            params.add(offset);

            ps = conn.prepareStatement(sql.toString());
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            rs = ps.executeQuery();

            List<Map<String, Object>> list = new ArrayList<>();
            while (rs.next()) {
                list.add(itemToMap(rs));
            }

            System.out.println("[ItemServlet] getItems 返回: total=" + total + ", 当前页数据=" + list.size() + "条");

            Map<String, Object> result = new HashMap<>();
            result.put("list", list);
            result.put("total", total);
            result.put("page", page);
            result.put("pageSize", pageSize);
            ResponseUtil.success(resp, result);
        } catch (SQLException e) {
            e.printStackTrace();
            ResponseUtil.error(resp, 500, "获取物品列表失败");
        } finally {
            DbUtil.close(conn, ps, rs);
        }
    }

    // ==================== 获取物品详情 ====================
    private void getItemDetail(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();
        int itemId = Integer.parseInt(pathInfo.substring(1));

        System.out.println("[ItemServlet] getItemDetail - itemId=" + itemId);

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DbUtil.getConnection();
            String sql = "SELECT i.*, u.username, u.nickname, u.avatar, u.phone, u.email, u.is_certified FROM items i JOIN users u ON i.user_id = u.id WHERE i.id = ?";
            ps = conn.prepareStatement(sql);
            ps.setInt(1, itemId);
            rs = ps.executeQuery();

            if (rs.next()) {
                Map<String, Object> item = itemToMap(rs);
                item.put("phone", rs.getString("phone"));
                item.put("email", rs.getString("email"));
                item.put("certified", rs.getInt("is_certified") == 1);
                System.out.println("[ItemServlet] getItemDetail 返回: " + item.get("title"));
                ResponseUtil.success(resp, item);
            } else {
                ResponseUtil.error(resp, 404, "物品不存在");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            ResponseUtil.error(resp, 500, "获取物品详情失败");
        } finally {
            DbUtil.close(conn, ps, rs);
        }
    }

    // ==================== 创建物品 ====================
    private void createItem(HttpServletRequest req, HttpServletResponse resp) throws IOException {
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
        String title = (String) body.get("title");
        String description = (String) body.get("description");
        Object typeObj = body.get("type");
        Object categoryObj = body.get("category");
        String location = (String) body.get("location");
        String date = (String) body.get("date");
        String images = (String) body.get("images");
        String contact = (String) body.get("contact");

        // 处理 type：前端可能传数字 1/2 或字符串 "lost"/"found"
        String type;
        if (typeObj instanceof Number) {
            type = ((Number) typeObj).intValue() == 1 ? "found" : "lost";
        } else if (typeObj instanceof String) {
            String ts = (String) typeObj;
            if ("1".equals(ts) || "2".equals(ts)) {
                type = "1".equals(ts) ? "found" : "lost";
            } else {
                type = ts;
            }
        } else {
            type = null;
        }

        // 处理 category：前端可能传数字 categoryId
        String category;
        if (categoryObj instanceof Number) {
            category = String.valueOf(((Number) categoryObj).intValue());
        } else if (categoryObj instanceof String) {
            category = (String) categoryObj;
        } else {
            category = null;
        }

        System.out.println("[ItemServlet] createItem - userId=" + userId + ", title=" + title + ", type=" + type + ", category=" + category);

        if (title == null || type == null) {
            ResponseUtil.error(resp, 400, "标题和类型不能为空");
            return;
        }

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DbUtil.getConnection();
            String sql = "INSERT INTO items (user_id, title, description, type, category, location, date, images, contact, status, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, 'active', NOW())";
            ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, userId);
            ps.setString(2, title);
            ps.setString(3, description);
            ps.setString(4, type);
            ps.setString(5, category);
            ps.setString(6, location);
            ps.setString(7, date);
            ps.setString(8, images);
            ps.setString(9, contact);
            ps.executeUpdate();

            rs = ps.getGeneratedKeys();
            if (rs.next()) {
                int itemId = rs.getInt(1);
                Map<String, Object> data = new HashMap<>();
                data.put("id", itemId);
                System.out.println("[ItemServlet] createItem 成功: itemId=" + itemId);
                ResponseUtil.success(resp, data);
            } else {
                ResponseUtil.error(resp, 500, "创建失败");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            ResponseUtil.error(resp, 500, "创建物品失败");
        } finally {
            DbUtil.close(conn, ps, rs);
        }
    }

    // ==================== 更新物品 ====================
    private void updateItem(HttpServletRequest req, HttpServletResponse resp) throws IOException {
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
        int itemId = Integer.parseInt(pathInfo.substring(1));

        System.out.println("[ItemServlet] updateItem - itemId=" + itemId + ", userId=" + userId);

        Map<String, Object> body = JsonUtil.toMap(readBody(req));
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DbUtil.getConnection();
            // 检查是否是自己的物品
            String checkSql = "SELECT user_id FROM items WHERE id = ?";
            ps = conn.prepareStatement(checkSql);
            ps.setInt(1, itemId);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) {
                ResponseUtil.error(resp, 404, "物品不存在");
                return;
            }
            if (rs.getInt("user_id") != userId) {
                ResponseUtil.forbidden(resp);
                return;
            }
            DbUtil.close(null, ps, rs);

            StringBuilder sql = new StringBuilder("UPDATE items SET ");
            List<Object> params = new ArrayList<>();

            if (body.containsKey("title")) { sql.append("title = ?, "); params.add(body.get("title")); }
            if (body.containsKey("description")) { sql.append("description = ?, "); params.add(body.get("description")); }
            if (body.containsKey("category")) { sql.append("category = ?, "); params.add(body.get("category")); }
            if (body.containsKey("location")) { sql.append("location = ?, "); params.add(body.get("location")); }
            if (body.containsKey("date")) { sql.append("date = ?, "); params.add(body.get("date")); }
            if (body.containsKey("images")) { sql.append("images = ?, "); params.add(body.get("images")); }
            if (body.containsKey("contact")) { sql.append("contact = ?, "); params.add(body.get("contact")); }

            if (params.isEmpty()) {
                ResponseUtil.error(resp, 400, "没有需要更新的字段");
                return;
            }

            sql.setLength(sql.length() - 2);
            sql.append(" WHERE id = ?");
            params.add(itemId);

            ps = conn.prepareStatement(sql.toString());
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            ps.executeUpdate();

            System.out.println("[ItemServlet] updateItem 成功");
            ResponseUtil.success(resp);
        } catch (SQLException e) {
            e.printStackTrace();
            ResponseUtil.error(resp, 500, "更新物品失败");
        } finally {
            DbUtil.close(conn, ps);
        }
    }

    // ==================== 删除物品 ====================
    private void deleteItem(HttpServletRequest req, HttpServletResponse resp) throws IOException {
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
        int itemId = Integer.parseInt(pathInfo.substring(1));

        System.out.println("[ItemServlet] deleteItem - itemId=" + itemId + ", userId=" + userId);

        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DbUtil.getConnection();
            String checkSql = "SELECT user_id FROM items WHERE id = ?";
            ps = conn.prepareStatement(checkSql);
            ps.setInt(1, itemId);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) {
                ResponseUtil.error(resp, 404, "物品不存在");
                return;
            }
            if (rs.getInt("user_id") != userId && !JwtUtil.isAdmin(token)) {
                ResponseUtil.forbidden(resp);
                return;
            }
            DbUtil.close(null, ps, rs);

            String sql = "UPDATE items SET status = 'deleted' WHERE id = ?";
            ps = conn.prepareStatement(sql);
            ps.setInt(1, itemId);
            ps.executeUpdate();

            System.out.println("[ItemServlet] deleteItem 成功");
            ResponseUtil.success(resp);
        } catch (SQLException e) {
            e.printStackTrace();
            ResponseUtil.error(resp, 500, "删除物品失败");
        } finally {
            DbUtil.close(conn, ps);
        }
    }

    // ==================== 获取用户自己的物品 ====================
    private void getUserItems(HttpServletRequest req, HttpServletResponse resp) throws IOException {
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

        String type = req.getParameter("type");
        System.out.println("[ItemServlet] getUserItems - userId=" + userId + ", type=" + type);

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DbUtil.getConnection();
            String sql;
            if (type != null && !type.isEmpty()) {
                sql = "SELECT i.*, u.username, u.nickname, u.avatar FROM items i JOIN users u ON i.user_id = u.id WHERE i.user_id = ? AND i.type = ? AND i.status != 'deleted' ORDER BY i.created_at DESC";
                ps = conn.prepareStatement(sql);
                ps.setInt(1, userId);
                ps.setString(2, type);
            } else {
                sql = "SELECT i.*, u.username, u.nickname, u.avatar FROM items i JOIN users u ON i.user_id = u.id WHERE i.user_id = ? AND i.status != 'deleted' ORDER BY i.created_at DESC";
                ps = conn.prepareStatement(sql);
                ps.setInt(1, userId);
            }
            rs = ps.executeQuery();

            List<Map<String, Object>> list = new ArrayList<>();
            while (rs.next()) {
                list.add(itemToMap(rs));
            }

            System.out.println("[ItemServlet] getUserItems 返回: " + list.size() + "条");
            ResponseUtil.success(resp, list);
        } catch (SQLException e) {
            e.printStackTrace();
            ResponseUtil.error(resp, 500, "获取用户物品失败");
        } finally {
            DbUtil.close(conn, ps, rs);
        }
    }

    // ==================== 获取用户统计 ====================
    private void getUserStats(HttpServletRequest req, HttpServletResponse resp) throws IOException {
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

        System.out.println("[ItemServlet] getUserStats - userId=" + userId);

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DbUtil.getConnection();
            Map<String, Object> stats = new HashMap<>();

            // 总发布数
            String totalSql = "SELECT COUNT(*) as total FROM items WHERE user_id = ? AND status != 'deleted'";
            ps = conn.prepareStatement(totalSql);
            ps.setInt(1, userId);
            rs = ps.executeQuery();
            if (rs.next()) stats.put("total", rs.getInt("total"));
            DbUtil.close(null, ps, rs);

            // 失物数
            String lostSql = "SELECT COUNT(*) as total FROM items WHERE user_id = ? AND type = 'lost' AND status != 'deleted'";
            ps = conn.prepareStatement(lostSql);
            ps.setInt(1, userId);
            rs = ps.executeQuery();
            if (rs.next()) stats.put("lost", rs.getInt("total"));
            DbUtil.close(null, ps, rs);

            // 招领数
            String foundSql = "SELECT COUNT(*) as total FROM items WHERE user_id = ? AND type = 'found' AND status != 'deleted'";
            ps = conn.prepareStatement(foundSql);
            ps.setInt(1, userId);
            rs = ps.executeQuery();
            if (rs.next()) stats.put("found", rs.getInt("total"));
            DbUtil.close(null, ps, rs);

            // 已完成数
            String completedSql = "SELECT COUNT(*) as total FROM items WHERE user_id = ? AND status = 'completed'";
            ps = conn.prepareStatement(completedSql);
            ps.setInt(1, userId);
            rs = ps.executeQuery();
            if (rs.next()) stats.put("completed", rs.getInt("total"));

            System.out.println("[ItemServlet] getUserStats 返回: " + stats);
            ResponseUtil.success(resp, stats);
        } catch (SQLException e) {
            e.printStackTrace();
            ResponseUtil.error(resp, 500, "获取用户统计失败");
        } finally {
            DbUtil.close(conn, ps, rs);
        }
    }

    // ==================== 搜索物品 ====================
    private void searchItems(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String keyword = req.getParameter("keyword");
        String type = req.getParameter("type");
        String category = req.getParameter("category");

        System.out.println("[ItemServlet] searchItems - keyword=" + keyword + ", type=" + type + ", category=" + category);

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DbUtil.getConnection();
            StringBuilder sql = new StringBuilder("SELECT i.*, u.username, u.nickname, u.avatar FROM items i JOIN users u ON i.user_id = u.id WHERE i.status != 'deleted'");
            List<Object> params = new ArrayList<>();

            if (keyword != null && !keyword.isEmpty()) {
                sql.append(" AND (i.title LIKE ? OR i.description LIKE ?)");
                String like = "%" + keyword + "%";
                params.add(like);
                params.add(like);
            }
            if (type != null && !type.isEmpty()) {
                sql.append(" AND i.type = ?");
                params.add(type);
            }
            if (category != null && !category.isEmpty()) {
                sql.append(" AND i.category = ?");
                params.add(category);
            }
            sql.append(" ORDER BY i.created_at DESC LIMIT 50");

            ps = conn.prepareStatement(sql.toString());
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            rs = ps.executeQuery();

            List<Map<String, Object>> list = new ArrayList<>();
            while (rs.next()) {
                list.add(itemToMap(rs));
            }

            System.out.println("[ItemServlet] searchItems 返回: " + list.size() + "条");
            ResponseUtil.success(resp, list);
        } catch (SQLException e) {
            e.printStackTrace();
            ResponseUtil.error(resp, 500, "搜索失败");
        } finally {
            DbUtil.close(conn, ps, rs);
        }
    }

    // ==================== 智能匹配 ====================
    private void matchItem(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();
        int itemId = Integer.parseInt(pathInfo.substring(7));

        System.out.println("[ItemServlet] matchItem - itemId=" + itemId);

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DbUtil.getConnection();
            // 获取当前物品信息
            String getSql = "SELECT * FROM items WHERE id = ?";
            ps = conn.prepareStatement(getSql);
            ps.setInt(1, itemId);
            rs = ps.executeQuery();
            if (!rs.next()) {
                ResponseUtil.error(resp, 404, "物品不存在");
                return;
            }

            String type = rs.getString("type");
            String category = rs.getString("category");
            String oppositeType = "lost".equals(type) ? "found" : "lost";

            System.out.println("[ItemServlet] matchItem - 当前物品 type=" + type + ", category=" + category + ", 匹配类型=" + oppositeType);

            DbUtil.close(null, ps, rs);

            // 查找匹配的物品
            String matchSql = "SELECT i.*, u.username, u.nickname, u.avatar FROM items i JOIN users u ON i.user_id = u.id WHERE i.type = ? AND i.category = ? AND i.status = 'active' AND i.id != ? ORDER BY i.created_at DESC LIMIT 10";
            ps = conn.prepareStatement(matchSql);
            ps.setString(1, oppositeType);
            ps.setString(2, category);
            ps.setInt(3, itemId);
            rs = ps.executeQuery();

            List<Map<String, Object>> list = new ArrayList<>();
            while (rs.next()) {
                list.add(itemToMap(rs));
            }

            System.out.println("[ItemServlet] matchItem 返回: " + list.size() + "条匹配");
            ResponseUtil.success(resp, list);
        } catch (SQLException e) {
            e.printStackTrace();
            ResponseUtil.error(resp, 500, "匹配失败");
        } finally {
            DbUtil.close(conn, ps, rs);
        }
    }

    // ==================== 更新物品状态 ====================
    private void updateItemStatus(HttpServletRequest req, HttpServletResponse resp) throws IOException {
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
        int itemId = Integer.parseInt(pathInfo.substring(1, pathInfo.indexOf("/status")));

        Map<String, Object> body = JsonUtil.toMap(readBody(req));
        Object statusObj = body.get("status");
        String status;
        if (statusObj instanceof Number) {
            status = ((Number) statusObj).intValue() == 2 ? "completed" : "active";
        } else {
            status = (String) statusObj;
        }

        System.out.println("[ItemServlet] updateItemStatus - itemId=" + itemId + ", userId=" + userId + ", status=" + status);

        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DbUtil.getConnection();
            String checkSql = "SELECT user_id FROM items WHERE id = ?";
            ps = conn.prepareStatement(checkSql);
            ps.setInt(1, itemId);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) {
                ResponseUtil.error(resp, 404, "物品不存在");
                return;
            }
            if (rs.getInt("user_id") != userId) {
                ResponseUtil.forbidden(resp);
                return;
            }
            DbUtil.close(null, ps, rs);

            String sql = "UPDATE items SET status = ? WHERE id = ?";
            ps = conn.prepareStatement(sql);
            ps.setString(1, status);
            ps.setInt(2, itemId);
            ps.executeUpdate();

            System.out.println("[ItemServlet] updateItemStatus 成功");
            ResponseUtil.success(resp);
        } catch (SQLException e) {
            e.printStackTrace();
            ResponseUtil.error(resp, 500, "更新状态失败");
        } finally {
            DbUtil.close(conn, ps);
        }
    }

    // ==================== 工具方法：将 ResultSet 转为前端期望的 Map 格式 ====================
    private Map<String, Object> itemToMap(ResultSet rs) throws SQLException {
        Map<String, Object> item = new HashMap<>();
        item.put("id", rs.getInt("id"));
        item.put("userId", rs.getInt("user_id"));
        item.put("title", rs.getString("title"));
        item.put("description", rs.getString("description"));

        // type: 数据库存 "lost"/"found"，前端期望 1(招领)/2(寻物)
        String dbType = rs.getString("type");
        if ("found".equals(dbType)) {
            item.put("type", 1);
        } else if ("lost".equals(dbType)) {
            item.put("type", 2);
        } else {
            item.put("type", dbType);
        }

        // category: 数据库存字符串，前端期望 categoryId（数字）
        String dbCategory = rs.getString("category");
        try {
            item.put("categoryId", Integer.parseInt(dbCategory));
        } catch (NumberFormatException e) {
            item.put("categoryId", dbCategory);
        }
        item.put("category", dbCategory);

        item.put("location", rs.getString("location"));
        item.put("date", rs.getString("date"));
        item.put("images", rs.getString("images"));
        item.put("contact", rs.getString("contact"));

        // status: 数据库存 "active"/"completed"/"deleted"，前端期望 1(进行中)/2(已解决)
        String dbStatus = rs.getString("status");
        if ("active".equals(dbStatus)) {
            item.put("status", 1);
        } else if ("completed".equals(dbStatus)) {
            item.put("status", 2);
        } else if ("deleted".equals(dbStatus)) {
            item.put("status", 0);
        } else {
            item.put("status", dbStatus);
        }

        // createTime: 前端期望驼峰命名
        item.put("createTime", rs.getString("created_at"));
        item.put("created_at", rs.getString("created_at"));

        try {
            item.put("username", rs.getString("username"));
            item.put("nickname", rs.getString("nickname"));
            item.put("avatar", rs.getString("avatar"));
        } catch (SQLException ignored) {}
        return item;
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