package com.example.dao;

import com.example.model.UserBean;

import java.io.InputStream;
import java.sql.*;
import java.util.Properties;

public class DbUtil {
    private static String driver;
    private static String url;
    private static String username;
    private static String password;

    static {
        try {
            // 加载配置文件
            InputStream is = DbUtil.class.getClassLoader().getResourceAsStream("db.properties");
            Properties props = new Properties();
            if (is != null) {
                props.load(is);
                driver = props.getProperty("driver");
                url = props.getProperty("url");
                username = props.getProperty("username");
                password = props.getProperty("password");
                // 加载驱动
                Class.forName(driver);
            } else {
                throw new RuntimeException("db.properties 配置文件未找到");
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("数据库配置加载失败", e);
        }
    }

    /**
     * 获取数据库连接
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, username, password);
    }

    /**
     * 关闭数据库资源
     */
    public static void closeResource(Connection conn, PreparedStatement ps, ResultSet rs) {
        try {
            if (rs != null) {
                rs.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            if (ps != null) {
                ps.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            if (conn != null) {
                conn.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * 保存用户到数据库
     */
    public static void saveUser(UserBean user) throws SQLException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            String sql = "INSERT INTO t_user (username, password, email, phone) VALUES (?, ?, ?, ?)";
            ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPassword());
            ps.setString(3, user.getEmail());
            ps.setString(4, user.getPhone());
            ps.executeUpdate();

            // 获取自动生成的主键id
            rs = ps.getGeneratedKeys();
            if (rs.next()) {
                user.setId(rs.getInt(1));
            }
        } finally {
            closeResource(conn, ps, rs);
        }
    }

    /**
     * 根据用户名查找用户
     */
    public static UserBean findUserByUsername(String username) throws SQLException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        UserBean user = null;
        try {
            conn = getConnection();
            String sql = "SELECT id, username, password, email, phone FROM t_user WHERE username = ?";
            ps = conn.prepareStatement(sql);
            ps.setString(1, username);
            rs = ps.executeQuery();
            if (rs.next()) {
                user = new UserBean();
                user.setId(rs.getInt("id"));
                user.setUsername(rs.getString("username"));
                user.setPassword(rs.getString("password"));
                user.setEmail(rs.getString("email"));
                user.setPhone(rs.getString("phone"));
            }
        } finally {
            closeResource(conn, ps, rs);
        }
        return user;
    }
}