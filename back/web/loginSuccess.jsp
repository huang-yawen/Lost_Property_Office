<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.example.model.UserBean" %>
<%@ page import="jakarta.servlet.http.HttpSession" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>注册成功</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            background-color: #f4f4f4;
            margin: 0;
            padding: 0;
            display: flex;
            justify-content: center;
            align-items: center;
            min-height: 100vh;
        }
        .container {
            background: #fff;
            padding: 30px 40px;
            border-radius: 8px;
            box-shadow: 0 0 15px rgba(0,0,0,0.1);
            width: 450px;
            text-align: center;
        }
        h2 {
            color: #5cb85c;
            margin-bottom: 20px;
        }
        .info {
            text-align: left;
            margin: 20px 0;
            padding: 15px;
            background-color: #f9f9f9;
            border-radius: 4px;
        }
        .info p {
            margin: 8px 0;
            color: #555;
        }
        .info label {
            font-weight: bold;
            color: #333;
        }
        .logout {
            display: inline-block;
            margin-top: 15px;
            padding: 10px 20px;
            background-color: #d9534f;
            color: white;
            text-decoration: none;
            border-radius: 4px;
        }
        .logout:hover {
            background-color: #c9302c;
        }
        .back {
            display: inline-block;
            margin-top: 15px;
            margin-left: 10px;
            padding: 10px 20px;
            background-color: #5bc0de;
            color: white;
            text-decoration: none;
            border-radius: 4px;
        }
        .back:hover {
            background-color: #46b8da;
        }
    </style>
</head>
<body>
    <div class="container">
        <h2>🎉 注册成功！</h2>

        <%
            UserBean user = (UserBean) session.getAttribute("user");
            if (user != null) {
        %>
            <div class="info">
                <p><label>欢迎您：</label><%= user.getUsername() %></p>
                <p><label>邮箱：</label><%= user.getEmail() %></p>
                <p><label>手机号：</label><%= user.getPhone() %></p>
            </div>
            <p style="color: #888;">您已自动登录系统</p>
            <a href="#" class="logout">退出登录</a>
            <a href="${pageContext.request.contextPath}/register.jsp" class="back">返回注册</a>
        <%
            } else {
        %>
            <p style="color: #d9534f;">您尚未登录，请先注册。</p>
            <a href="${pageContext.request.contextPath}/register.jsp" class="back">前往注册</a>
        <%
            }
        %>
    </div>
</body>
</html>
