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
        System.out.println("[ItemServlet] GET request: " + pathInfo + ", 参数: " + req.getQueryString());

        try {
            if (pathInfo.matches("/\\d+")) {
                getItemDetail(req, resp);
    } else if (pathInfo.equals("/dashboard/stats")) {
                getDashboardStats(req, resp);
            } else if (pathInfo.equals("/user/stats")) {
                getUserStats(req, resp);
            } else if (pathInfo.equals("/user")) {
                getUserItems(req, resp);
            } else if (pathInfo.equals("/search")) {
                searchItems(req, resp);
            } else if (pathInfo.matches("/match/\\d+")) {
                smartMatch(req, resp);
            } else if (pathInfo.equals("") || pathInfo.equals("/")) {
                getItems(req, resp);
            } else {
                System.out.println("[ItemServlet] 404 - 接口不存在: " + pathInfo);
                ResponseUtil.error(resp, 404, "接口不存在: " + pathInfo);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("[ItemServlet] 500错误: " + e.getMessage());
            ResponseUtil.error(resp, 500, "服务器内部错误: " + e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();
        if (pathInfo == null) pathInfo = "";
        System.out.println("[ItemServlet] POST request: " + pathInfo);

        try {
            if (pathInfo.equals("") || pathInfo.equals("/")) {
                createItem(req, resp);
            } else {
                System.out.println("[ItemServlet] 404 - 接口不存在: " + pathInfo);
                ResponseUtil.error(resp, 404, "接口不存在: " + pathInfo);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("[ItemServlet] 500错误: " + e.getMessage());
            ResponseUtil.error(resp, 500, "服务器内部错误: " + e.getMessage());
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();
        if (pathInfo == null) pathInfo = "";
        System.out.println("[ItemServlet] PUT request: " + pathInfo);

        try {
            if (pathInfo.matches("/\\d+")) {
                updateItem(req, resp);
            } else if (pathInfo.matches("/\\d+/status")) {
                updateItemStatus(req, resp);
            } else {
                System.out.println("[ItemServlet] 404 - 接口不存在: " + pathInfo);
                ResponseUtil.error(resp, 404, "接口不存在: " + pathInfo);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("[ItemServlet] 500错误: " + e.getMessage());
            ResponseUtil.error(resp, 500, "服务器内部错误: " + e.getMessage());
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();
        if (pathInfo == null) pathInfo = "";
        System.out.println("[ItemServlet] DELETE request: " + pathInfo);

        try {
            if (pathInfo.matches("/\\d+")) {
                deleteItem(req, resp);
            } else {
                System.out.println("[ItemServlet] 404 - 接口不存在: " + pathInfo);
                ResponseUtil.error(resp, 404, "接口不存在: " + pathInfo);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("[ItemServlet] 500错误: " + e.getMessage());
            ResponseUtil.error(resp, 500, "服务器内部错误: " + e.getMessage());
        }
    }

    // ==================== 获取物品列表（分页） ====================
    private void getItems(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        System.out.println("[ItemServlet] ===== 开始获取物品列表 =====");
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = DbUtil.getConnection();
            System.out.println("[ItemServlet] 数据库连接成功");

            StringBuilder where = new StringBuilder(" WHERE i.status != 'deleted'");
            List<Object> params = new ArrayList<>();

            String typeStr = req.getParameter("type");
            if (typeStr != null && !typeStr.isEmpty()) {
                int type = Integer.parseInt(typeStr);
                System.out.println("[ItemServlet] 类型筛选: " + type + " (1=招领, 2=寻物)");
                if (type == 1) {
                    where.append(" AND i.type = 'found'");
                } else if (type == 2) {
                    where.append(" AND i.type = 'lost'");
                }
            }

            String categoryIdStr = req.getParameter("categoryId");
            if (categoryIdStr != null && !categoryIdStr.isEmpty()) {
                where.append(" AND i.category = ?");
                params.add(categoryIdStr);
                System.out.println("[ItemServlet] 分类筛选: categoryId=" + categoryIdStr);
            }

            String keyword = req.getParameter("keyword");
            if (keyword != null && !keyword.trim().isEmpty()) {
                where.append(" AND (i.title LIKE ? OR i.description LIKE ?)");
                String likePattern = "%" + keyword.trim() + "%";
                params.add(likePattern);
                params.add(likePattern);
                System.out.println("[ItemServlet] 关键词搜索: " + keyword);
            }

            String startTime = req.getParameter("startTime");
            String endTime = req.getParameter("endTime");
            if (startTime != null && !startTime.isEmpty()) {
                where.append(" AND i.created_at >= ?");
                params.add(startTime);
                System.out.println("[ItemServlet] 时间筛选: startTime=" + startTime);
            }
            if (endTime != null && !endTime.isEmpty()) {
                where.append(" AND i.created_at <= ?");
                params.add(endTime);
                System.out.println("[ItemServlet] 时间筛选: endTime=" + endTime);
            }

            String sort = req.getParameter("sort");
            String orderBy = " ORDER BY i.created_at DESC";
            if ("asc".equals(sort)) {
                orderBy = " ORDER BY i.created_at ASC";
            }
            System.out.println("[ItemServlet] 排序方式: " + (sort != null ? sort : "desc"));

            String countSql = "SELECT COUNT(*) as total FROM items i" + where.toString();
            System.out.println("[ItemServlet] 总数SQL: " + countSql);
            ps = conn.prepareStatement(countSql);
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            rs = ps.executeQuery();
            int total = 0;
            if (rs.next()) {
                total = rs.getInt("total");
            }
            System.out.println("[ItemServlet] 查询到总数: " + total);
            DbUtil.close(null, ps, rs);

            int page = 1;
            int pageSize = 12;
            String pageStr = req.getParameter("page");
            String pageSizeStr = req.getParameter("pageSize");
            if (pageStr != null && !pageStr.isEmpty()) page = Integer.parseInt(pageStr);
            if (pageSizeStr != null && !pageSizeStr.isEmpty()) pageSize = Integer.parseInt(pageSizeStr);
            int offset = (page - 1) * pageSize;
            System.out.println("[ItemServlet] 分页: page=" + page + ", pageSize=" + pageSize + ", offset=" + offset);

            String sql = "SELECT i.*, u.username, u.nickname, u.avatar, u.is_certified as user_certified " +
                    "FROM items i LEFT JOIN users u ON i.user_id = u.id" +
                    where.toString() + orderBy + " LIMIT ? OFFSET ?";
            System.out.println("[ItemServlet] 查询SQL: " + sql);
            ps = conn.prepareStatement(sql);
            int paramIndex = 1;
            for (Object param : params) {
                ps.setObject(paramIndex++, param);
            }
            ps.setInt(paramIndex++, pageSize);
            ps.setInt(paramIndex, offset);
            rs = ps.executeQuery();

            List<Map<String, Object>> list = new ArrayList<>();
            while (rs.next()) {
                list.add(itemToMap(rs));
            }
            System.out.println("[ItemServlet] 查询到 " + list.size() + " 条数据");

            Map<String, Object> data = new HashMap<>();
            data.put("list", list);
            data.put("total", total);
            data.put("page", page);
            data.put("pageSize", pageSize);

            System.out.println("[ItemServlet] ===== 返回数据: total=" + total + ", list.size=" + list.size() + " =====");
            ResponseUtil.success(resp, data);
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("[ItemServlet] 数据库异常: " + e.getMessage());
            ResponseUtil.error(resp, 500, "获取物品列表失败: " + e.getMessage());
        } finally {
            DbUtil.close(conn, ps, rs);
        }
    }

    // ==================== 获取物品详情 ====================
    private void getItemDetail(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();
        int itemId = Integer.parseInt(pathInfo.substring(1));
        System.out.println("[ItemServlet] 获取物品详情: id=" + itemId);

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DbUtil.getConnection();
            String sql = "SELECT i.*, u.username, u.nickname, u.avatar, u.is_certified as user_certified " +
                    "FROM items i LEFT JOIN users u ON i.user_id = u.id WHERE i.id = ?";
            ps = conn.prepareStatement(sql);
            ps.setInt(1, itemId);
            rs = ps.executeQuery();

            if (rs.next()) {
                Map<String, Object> item = itemToMap(rs);
                System.out.println("[ItemServlet] 获取详情成功: id=" + itemId + ", title=" + item.get("title"));
                ResponseUtil.success(resp, item);
            } else {
                System.out.println("[ItemServlet] 物品不存在: id=" + itemId);
                ResponseUtil.error(resp, 404, "物品不存在");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("[ItemServlet] 获取详情异常: " + e.getMessage());
            ResponseUtil.error(resp, 500, "获取物品详情失败");
        } finally {
            DbUtil.close(conn, ps, rs);
        }
    }

    // ==================== 创建物品 ====================
    private void createItem(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String authHeader = req.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            System.out.println("[ItemServlet] 创建物品失败: 未授权");
            ResponseUtil.unauthorized(resp);
            return;
        }
        String token = authHeader.substring(7);
        Integer userId = JwtUtil.getUserId(token);
        if (userId == null) {
            System.out.println("[ItemServlet] 创建物品失败: token无效");
            ResponseUtil.unauthorized(resp);
            return;
        }

        Map<String, Object> body = JsonUtil.toMap(readBody(req));
        System.out.println("[ItemServlet] 创建物品 - userId=" + userId + ", title=" + body.get("title"));

        String title = (String) body.get("title");
        String description = (String) body.get("description");
        Object typeObj = body.get("type");
        String type;
        if (typeObj instanceof Integer) {
            type = ((Integer) typeObj) == 1 ? "found" : "lost";
        } else {
            type = (String) typeObj;
        }
        String category = body.get("categoryId") != null ? String.valueOf(body.get("categoryId")) : null;
        String location = (String) body.get("location");
        String date = (String) body.get("date");
        // images 已经是 JSON 字符串（前端已 JSON.stringify），直接使用
        String images = (String) body.get("images");
        String contact = (String) body.get("contact");

        if (title == null || title.trim().isEmpty()) {
            System.out.println("[ItemServlet] 创建物品失败: 标题为空");
            ResponseUtil.error(resp, 400, "标题不能为空");
            return;
        }

        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DbUtil.getConnection();
            String sql = "INSERT INTO items (user_id, title, description, type, category, location, date, images, contact, status, created_at) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, 'active', NOW())";
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

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                int newId = rs.getInt(1);
                System.out.println("[ItemServlet] 创建物品成功: id=" + newId);
                Map<String, Object> data = new HashMap<>();
                data.put("id", newId);
                ResponseUtil.success(resp, data);
            } else {
                System.out.println("[ItemServlet] 创建物品失败: 无法获取自增ID");
                ResponseUtil.error(resp, 500, "创建失败");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("[ItemServlet] 创建物品异常: " + e.getMessage());
            ResponseUtil.error(resp, 500, "创建物品失败");
        } finally {
            DbUtil.close(conn, ps);
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
        Map<String, Object> body = JsonUtil.toMap(readBody(req));
        System.out.println("[ItemServlet] 更新物品 - id=" + itemId + ", userId=" + userId + ", body=" + body);

        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DbUtil.getConnection();

            String checkSql = "SELECT user_id FROM items WHERE id = ?";
            ps = conn.prepareStatement(checkSql);
            ps.setInt(1, itemId);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) {
                System.out.println("[ItemServlet] 更新失败: 物品不存在 id=" + itemId);
                ResponseUtil.error(resp, 404, "物品不存在");
                return;
            }
            int ownerId = rs.getInt("user_id");
            if (ownerId != userId) {
                System.out.println("[ItemServlet] 更新失败: 无权操作 userId=" + userId + ", ownerId=" + ownerId);
                ResponseUtil.forbidden(resp);
                return;
            }
            DbUtil.close(null, ps, rs);

            StringBuilder sql = new StringBuilder("UPDATE items SET ");
            List<Object> params = new ArrayList<>();

            if (body.containsKey("title")) {
                sql.append("title = ?, ");
                params.add(body.get("title"));
            }
            if (body.containsKey("description")) {
                sql.append("description = ?, ");
                params.add(body.get("description"));
            }
            if (body.containsKey("type")) {
                Object typeObj = body.get("type");
                String type;
                if (typeObj instanceof Integer) {
                    type = ((Integer) typeObj) == 1 ? "found" : "lost";
                } else {
                    type = (String) typeObj;
                }
                sql.append("type = ?, ");
                params.add(type);
            }
            if (body.containsKey("categoryId")) {
                sql.append("category = ?, ");
                params.add(String.valueOf(body.get("categoryId")));
            }
            if (body.containsKey("location")) {
                sql.append("location = ?, ");
                params.add(body.get("location"));
            }
            if (body.containsKey("status")) {
                Object statusObj = body.get("status");
                String status;
                if (statusObj instanceof Integer) {
                    int s = (Integer) statusObj;
                    status = s == 2 ? "completed" : "active";
                } else {
                    status = (String) statusObj;
                }
                sql.append("status = ?, ");
                params.add(status);
            }

            if (params.isEmpty()) {
                System.out.println("[ItemServlet] 更新失败: 没有需要更新的字段");
                ResponseUtil.error(resp, 400, "没有需要更新的字段");
                return;
            }

            sql.delete(sql.length() - 2, sql.length());
            sql.append(" WHERE id = ?");
            params.add(itemId);

            ps = conn.prepareStatement(sql.toString());
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            ps.executeUpdate();
            System.out.println("[ItemServlet] 更新物品成功: id=" + itemId);
            ResponseUtil.success(resp);
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("[ItemServlet] 更新物品异常: " + e.getMessage());
            ResponseUtil.error(resp, 500, "更新物品失败");
        } finally {
            DbUtil.close(conn, ps);
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
        String status = (String) body.get("status");
        System.out.println("[ItemServlet] 更新物品状态 - id=" + itemId + ", status=" + status);

        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DbUtil.getConnection();
            String sql = "UPDATE items SET status = ? WHERE id = ?";
            ps = conn.prepareStatement(sql);
            ps.setString(1, status);
            ps.setInt(2, itemId);
            ps.executeUpdate();
            System.out.println("[ItemServlet] 更新状态成功");
            ResponseUtil.success(resp);
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("[ItemServlet] 更新状态异常: " + e.getMessage());
            ResponseUtil.error(resp, 500, "更新状态失败");
        } finally {
            DbUtil.close(conn, ps);
        }
    }

    // ==================== 删除/下架物品 ====================
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
        System.out.println("[ItemServlet] 删除物品 - id=" + itemId + ", userId=" + userId);

        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DbUtil.getConnection();
            String sql = "UPDATE items SET status = 'deleted' WHERE id = ? AND user_id = ?";
            ps = conn.prepareStatement(sql);
            ps.setInt(1, itemId);
            ps.setInt(2, userId);
            int rows = ps.executeUpdate();
            if (rows > 0) {
                System.out.println("[ItemServlet] 删除成功");
                ResponseUtil.success(resp);
            } else {
                System.out.println("[ItemServlet] 删除失败: 物品不存在或无权操作");
                ResponseUtil.error(resp, 404, "物品不存在或无权操作");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("[ItemServlet] 删除异常: " + e.getMessage());
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
        System.out.println("[ItemServlet] 获取用户物品 - userId=" + userId);

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DbUtil.getConnection();

            int page = 1;
            int pageSize = 20;
            String pageStr = req.getParameter("page");
            String pageSizeStr = req.getParameter("pageSize");
            if (pageStr != null && !pageStr.isEmpty()) page = Integer.parseInt(pageStr);
            if (pageSizeStr != null && !pageSizeStr.isEmpty()) pageSize = Integer.parseInt(pageSizeStr);
            int offset = (page - 1) * pageSize;

            String countSql = "SELECT COUNT(*) as total FROM items WHERE user_id = ? AND status != 'deleted'";
            ps = conn.prepareStatement(countSql);
            ps.setInt(1, userId);
            rs = ps.executeQuery();
            int total = 0;
            if (rs.next()) total = rs.getInt("total");
            DbUtil.close(null, ps, rs);

            String sql = "SELECT i.*, u.username, u.nickname, u.avatar, u.is_certified as user_certified " +
                    "FROM items i LEFT JOIN users u ON i.user_id = u.id " +
                    "WHERE i.user_id = ? AND i.status != 'deleted' ORDER BY i.created_at DESC LIMIT ? OFFSET ?";
            ps = conn.prepareStatement(sql);
            ps.setInt(1, userId);
            ps.setInt(2, pageSize);
            ps.setInt(3, offset);
            rs = ps.executeQuery();

            List<Map<String, Object>> list = new ArrayList<>();
            while (rs.next()) {
                list.add(itemToMap(rs));
            }

            Map<String, Object> data = new HashMap<>();
            data.put("list", list);
            data.put("total", total);
            data.put("page", page);
            data.put("pageSize", pageSize);
            System.out.println("[ItemServlet] 用户物品: total=" + total + ", list=" + list.size());
            ResponseUtil.success(resp, data);
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("[ItemServlet] 获取用户物品异常: " + e.getMessage());
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
        System.out.println("[ItemServlet] 获取用户统计 - userId=" + userId);

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DbUtil.getConnection();

            String sql = "SELECT " +
                    "COUNT(*) as total, " +
                    "SUM(CASE WHEN type = 'found' THEN 1 ELSE 0 END) as found_count, " +
                    "SUM(CASE WHEN type = 'lost' THEN 1 ELSE 0 END) as lost_count, " +
                    "SUM(CASE WHEN status = 'completed' THEN 1 ELSE 0 END) as completed_count " +
                    "FROM items WHERE user_id = ? AND status != 'deleted'";
            ps = conn.prepareStatement(sql);
            ps.setInt(1, userId);
            rs = ps.executeQuery();

            Map<String, Object> stats = new HashMap<>();
            if (rs.next()) {
                stats.put("total", rs.getInt("total"));
                stats.put("found_count", rs.getInt("found_count"));
                stats.put("lost_count", rs.getInt("lost_count"));
                stats.put("completed_count", rs.getInt("completed_count"));
            } else {
                stats.put("total", 0);
                stats.put("found_count", 0);
                stats.put("lost_count", 0);
                stats.put("completed_count", 0);
            }
            System.out.println("[ItemServlet] 用户统计: " + stats);
            ResponseUtil.success(resp, stats);
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("[ItemServlet] 获取用户统计异常: " + e.getMessage());
            ResponseUtil.error(resp, 500, "获取用户统计失败");
        } finally {
            DbUtil.close(conn, ps, rs);
        }
    }

    // ==================== 搜索物品 ====================
    private void searchItems(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String keyword = req.getParameter("keyword");
        System.out.println("[ItemServlet] 搜索物品 - keyword=" + keyword);

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DbUtil.getConnection();

            int page = 1;
            int pageSize = 20;
            String pageStr = req.getParameter("page");
            String pageSizeStr = req.getParameter("pageSize");
            if (pageStr != null && !pageStr.isEmpty()) page = Integer.parseInt(pageStr);
            if (pageSizeStr != null && !pageSizeStr.isEmpty()) pageSize = Integer.parseInt(pageSizeStr);
            int offset = (page - 1) * pageSize;

            StringBuilder where = new StringBuilder(" WHERE i.status != 'deleted'");
            List<Object> params = new ArrayList<>();
            if (keyword != null && !keyword.trim().isEmpty()) {
                where.append(" AND (i.title LIKE ? OR i.description LIKE ?)");
                String like = "%" + keyword.trim() + "%";
                params.add(like);
                params.add(like);
            }

            String countSql = "SELECT COUNT(*) as total FROM items i" + where.toString();
            ps = conn.prepareStatement(countSql);
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
            rs = ps.executeQuery();
            int total = 0;
            if (rs.next()) total = rs.getInt("total");
            DbUtil.close(null, ps, rs);

            String sql = "SELECT i.*, u.username, u.nickname, u.avatar, u.is_certified as user_certified " +
                    "FROM items i LEFT JOIN users u ON i.user_id = u.id" +
                    where.toString() + " ORDER BY i.created_at DESC LIMIT ? OFFSET ?";
            ps = conn.prepareStatement(sql);
            int idx = 1;
            for (Object p : params) ps.setObject(idx++, p);
            ps.setInt(idx++, pageSize);
            ps.setInt(idx, offset);
            rs = ps.executeQuery();

            List<Map<String, Object>> list = new ArrayList<>();
            while (rs.next()) list.add(itemToMap(rs));

            Map<String, Object> data = new HashMap<>();
            data.put("list", list);
            data.put("total", total);
            System.out.println("[ItemServlet] 搜索结果: total=" + total);
            ResponseUtil.success(resp, data);
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("[ItemServlet] 搜索异常: " + e.getMessage());
            ResponseUtil.error(resp, 500, "搜索失败");
        } finally {
            DbUtil.close(conn, ps, rs);
        }
    }

    // ==================== 全局看板统计 ====================
    private void getDashboardStats(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        System.out.println("[ItemServlet] 获取全局看板统计");

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DbUtil.getConnection();
            Map<String, Object> data = new HashMap<>();

            // 1. 统计卡片: 总发布量、已解决数
            String cardSql = "SELECT " +
                    "COUNT(*) as totalItems, " +
                    "SUM(CASE WHEN status = 'completed' THEN 1 ELSE 0 END) as resolvedCount, " +
                    "SUM(CASE WHEN status = 'active' THEN 1 ELSE 0 END) as activeCount, " +
                    "SUM(CASE WHEN type = 'found' THEN 1 ELSE 0 END) as foundCount, " +
                    "SUM(CASE WHEN type = 'lost' THEN 1 ELSE 0 END) as lostCount " +
                    "FROM items WHERE status != 'deleted'";
            ps = conn.prepareStatement(cardSql);
            rs = ps.executeQuery();
            if (rs.next()) {
                int totalItems = rs.getInt("totalItems");
                int resolvedCount = rs.getInt("resolvedCount");
                int foundCount = rs.getInt("foundCount");
                int lostCount = rs.getInt("lostCount");
                data.put("totalItems", totalItems);
                data.put("resolvedCount", resolvedCount);
                data.put("resolveRate", totalItems > 0 ? Math.round(resolvedCount * 100.0 / totalItems) : 0);
                data.put("foundCount", foundCount);
                data.put("lostCount", lostCount);
            } else {
                data.put("totalItems", 0);
                data.put("resolvedCount", 0);
                data.put("resolveRate", 0);
                data.put("foundCount", 0);
                data.put("lostCount", 0);
            }
            DbUtil.close(null, ps, rs);

            // 2. 今日新增
            String todaySql = "SELECT COUNT(*) as todayNew FROM items WHERE DATE(created_at) = CURDATE() AND status != 'deleted'";
            ps = conn.prepareStatement(todaySql);
            rs = ps.executeQuery();
            if (rs.next()) {
                data.put("todayNew", rs.getInt("todayNew"));
            } else {
                data.put("todayNew", 0);
            }
            DbUtil.close(null, ps, rs);

            // 3. 近7天趋势: 每天新增数(招领/寻物)
            String trendSql = "SELECT DATE(created_at) as day, type, COUNT(*) as cnt " +
                    "FROM items WHERE created_at >= DATE_SUB(CURDATE(), INTERVAL 6 DAY) AND status != 'deleted' " +
                    "GROUP BY DATE(created_at), type ORDER BY day ASC";
            ps = conn.prepareStatement(trendSql);
            rs = ps.executeQuery();
            Map<String, Map<String, Integer>> trendMap = new LinkedHashMap<>();
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
            java.util.Calendar cal = java.util.Calendar.getInstance();
            for (int i = 6; i >= 0; i--) {
                cal.setTime(new java.util.Date());
                cal.add(java.util.Calendar.DATE, -i);
                String day = sdf.format(cal.getTime());
                Map<String, Integer> dayMap = new HashMap<>();
                dayMap.put("found", 0);
                dayMap.put("lost", 0);
                trendMap.put(day, dayMap);
            }
            while (rs.next()) {
                String day = rs.getString("day");
                String type = rs.getString("type");
                int cnt = rs.getInt("cnt");
                if (trendMap.containsKey(day)) {
                    trendMap.get(day).put(type, cnt);
                }
            }
            DbUtil.close(null, ps, rs);

            List<Map<String, Object>> trendList = new ArrayList<>();
            for (Map.Entry<String, Map<String, Integer>> entry : trendMap.entrySet()) {
                Map<String, Object> dayData = new HashMap<>();
                dayData.put("date", entry.getKey());
                dayData.put("foundCount", entry.getValue().get("found"));
                dayData.put("lostCount", entry.getValue().get("lost"));
                trendList.add(dayData);
            }
            data.put("dailyTrend", trendList);

            // 4. 分类统计: JOIN categories 表获取分类名称
            String catSql = "SELECT i.category, c.name as category_name, COUNT(*) as cnt " +
                    "FROM items i LEFT JOIN categories c ON i.category = c.id " +
                    "WHERE i.status != 'deleted' AND i.category IS NOT NULL " +
                    "GROUP BY i.category, c.name ORDER BY cnt DESC LIMIT 10";
            ps = conn.prepareStatement(catSql);
            rs = ps.executeQuery();
            List<Map<String, Object>> catList = new ArrayList<>();
            while (rs.next()) {
                Map<String, Object> catItem = new HashMap<>();
                catItem.put("categoryId", rs.getString("category"));
                String catName = rs.getString("category_name");
                catItem.put("categoryName", catName != null ? catName : ("分类" + rs.getString("category")));
                catItem.put("count", rs.getInt("cnt"));
                catList.add(catItem);
            }
            data.put("categoryDistribution", catList);

            System.out.println("[ItemServlet] 看板统计完成: totalItems=" + data.get("totalItems") +
                    ", todayNew=" + data.get("todayNew") + ", trendDays=" + trendList.size() +
                    ", categoryTypes=" + catList.size());
            ResponseUtil.success(resp, data);
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("[ItemServlet] 看板统计异常: " + e.getMessage());
            ResponseUtil.error(resp, 500, "获取看板统计失败: " + e.getMessage());
        } finally {
            DbUtil.close(conn, ps, rs);
        }
    }

    // ==================== 智能匹配推荐 ====================
    private void smartMatch(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();
        int itemId = Integer.parseInt(pathInfo.substring(pathInfo.lastIndexOf("/") + 1));
        System.out.println("[ItemServlet] 智能匹配 - itemId=" + itemId);

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DbUtil.getConnection();

            String getSql = "SELECT * FROM items WHERE id = ?";
            ps = conn.prepareStatement(getSql);
            ps.setInt(1, itemId);
            rs = ps.executeQuery();
            if (!rs.next()) {
                System.out.println("[ItemServlet] 匹配失败: 物品不存在 id=" + itemId);
                ResponseUtil.error(resp, 404, "物品不存在");
                return;
            }

            String currentType = rs.getString("type");
            String currentCategory = rs.getString("category");
            DbUtil.close(null, ps, rs);

            String matchType = "found".equals(currentType) ? "lost" : "found";
            System.out.println("[ItemServlet] 匹配类型: " + currentType + " -> " + matchType);

            String sql = "SELECT i.*, u.username, u.nickname, u.avatar, u.is_certified as user_certified " +
                    "FROM items i LEFT JOIN users u ON i.user_id = u.id " +
                    "WHERE i.type = ? AND i.status = 'active' AND i.id != ? " +
                    "ORDER BY CASE WHEN i.category = ? THEN 0 ELSE 1 END, i.created_at DESC LIMIT 5";
            ps = conn.prepareStatement(sql);
            ps.setString(1, matchType);
            ps.setInt(2, itemId);
            ps.setString(3, currentCategory);
            rs = ps.executeQuery();

            List<Map<String, Object>> list = new ArrayList<>();
            while (rs.next()) {
                list.add(itemToMap(rs));
            }
            System.out.println("[ItemServlet] 匹配结果: " + list.size() + "条");
            ResponseUtil.success(resp, list);
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("[ItemServlet] 匹配异常: " + e.getMessage());
            ResponseUtil.error(resp, 500, "智能匹配失败");
        } finally {
            DbUtil.close(conn, ps, rs);
        }
    }

    // ==================== 将 ResultSet 转为 Map（统一字段名） ====================
    //    按照分类和日期进行推荐
    private Map<String, Object> itemToMap(ResultSet rs) throws SQLException {
        Map<String, Object> map = new HashMap<>();
        map.put("id", rs.getInt("id"));
        map.put("userId", rs.getInt("user_id"));
        map.put("title", rs.getString("title"));
        map.put("description", rs.getString("description"));

        // type: 数据库存 'found'/'lost'，返回 1(招领)/2(寻物)
        String dbType = rs.getString("type");
        if ("found".equals(dbType)) {
            map.put("type", 1);
        } else if ("lost".equals(dbType)) {
            map.put("type", 2);
        } else {
            map.put("type", dbType);
        }

        // categoryId: 数据库存字符串，返回数字
        String dbCategory = rs.getString("category");
        try {
            map.put("categoryId", dbCategory != null ? Integer.parseInt(dbCategory) : null);
        } catch (NumberFormatException e) {
            map.put("categoryId", dbCategory);
        }

        map.put("location", rs.getString("location"));
        map.put("date", rs.getString("date"));
        // 修复双重编码问题：旧数据被 JsonUtil.toJson 多包了一层，存储形如 "[\"/uploads/xxx.png\"]"
        String imagesStr = rs.getString("images");
        if (imagesStr != null && imagesStr.length() > 2 && imagesStr.startsWith("\"")) {
            try {
                Map<String, Object> tempMap = JsonUtil.toMap("{\"inner\":" + imagesStr + "}");
                Object inner = tempMap.get("inner");
                if (inner instanceof String && ((String) inner).startsWith("[")) {
                    imagesStr = (String) inner;
                }
            } catch (Exception ignored) {}
        }
        map.put("images", imagesStr);
        map.put("contact", rs.getString("contact"));

        // status: 数据库存 'active'/'completed'/'deleted'，返回 1(进行中)/2(已解决)
        String dbStatus = rs.getString("status");
        if ("active".equals(dbStatus)) {
            map.put("status", 1);
        } else if ("completed".equals(dbStatus)) {
            map.put("status", 2);
        } else {
            map.put("status", dbStatus);
        }

        // createTime: 数据库存 created_at
        Timestamp createTime = rs.getTimestamp("created_at");
        map.put("createTime", createTime != null ? createTime.toString() : null);

        // 用户信息
        map.put("username", rs.getString("username"));
        map.put("nickname", rs.getString("nickname"));
        map.put("avatar", rs.getString("avatar"));
        map.put("certified", rs.getBoolean("user_certified"));

        return map;
    }

    // ==================== 读取请求体 ====================
    private String readBody(HttpServletRequest req) throws IOException {
        StringBuilder sb = new StringBuilder();
        String line;
        java.io.BufferedReader reader = req.getReader();
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        return sb.toString();
    }
}