<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.Map" %>
<%@ page import="com.example.model.RegisterFormBean" %>
<%@ page import="jakarta.servlet.http.HttpServletRequest" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>用户注册</title>
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
            width: 400px;
        }
        h2 {
            text-align: center;
            color: #333;
            margin-bottom: 25px;
        }
        .form-group {
            margin-bottom: 15px;
        }
        label {
            display: block;
            margin-bottom: 5px;
            color: #555;
            font-weight: bold;
        }
        input[type="text"],
        input[type="password"],
        input[type="email"] {
            width: 100%;
            padding: 10px;
            border: 1px solid #ddd;
            border-radius: 4px;
            box-sizing: border-box;
            font-size: 14px;
        }
        input[type="submit"] {
            width: 100%;
            padding: 12px;
            background-color: #5cb85c;
            color: white;
            border: none;
            border-radius: 4px;
            font-size: 16px;
            cursor: pointer;
            margin-top: 10px;
        }
        input[type="submit"]:hover {
            background-color: #4cae4c;
        }
        .error {
            color: #d9534f;
            font-size: 13px;
            margin-top: 3px;
        }
        .error-message {
            background-color: #f2dede;
            color: #a94442;
            padding: 10px;
            border-radius: 4px;
            margin-bottom: 15px;
            text-align: center;
        }
    </style>
</head>
<body>
    <div class="container">
        <h2>用户注册</h2>

        <%
            String systemError = (String) request.getAttribute("error");
            Map<String, String> errors = null;
            String formUsername = "";
            String formPassword = "";
            String formConfirmPassword = "";
            String formEmail = "";
            String formPhone = "";

            Object formBeanObj = request.getAttribute("formBean");
            if (formBeanObj != null) {
                RegisterFormBean formBean = (RegisterFormBean) formBeanObj;
                errors = formBean.getErrors();
                if (formBean.getUsername() != null) formUsername = formBean.getUsername();
                if (formBean.getPassword() != null) formPassword = formBean.getPassword();
                if (formBean.getConfirmPassword() != null) formConfirmPassword = formBean.getConfirmPassword();
                if (formBean.getEmail() != null) formEmail = formBean.getEmail();
                if (formBean.getPhone() != null) formPhone = formBean.getPhone();
            }
        %>

        <% if (systemError != null && !systemError.isEmpty()) { %>
            <div class="error-message"><%= systemError %></div>
        <% } %>

        <form action="${pageContext.request.contextPath}/register" method="post">
            <div class="form-group">
                <label for="username">用户名</label>
                <input type="text" id="username" name="username"
                       value="<%= formUsername %>" placeholder="请输入用户名">
                <% if (errors != null && errors.containsKey("username")) { %>
                    <div class="error"><%= errors.get("username") %></div>
                <% } %>
            </div>

            <div class="form-group">
                <label for="password">密码</label>
                <input type="password" id="password" name="password"
                       value="<%= formPassword %>" placeholder="请输入密码">
                <% if (errors != null && errors.containsKey("password")) { %>
                    <div class="error"><%= errors.get("password") %></div>
                <% } %>
            </div>

            <div class="form-group">
                <label for="confirmPassword">确认密码</label>
                <input type="password" id="confirmPassword" name="confirmPassword"
                       value="<%= formConfirmPassword %>" placeholder="请再次输入密码">
                <% if (errors != null && errors.containsKey("confirmPassword")) { %>
                    <div class="error"><%= errors.get("confirmPassword") %></div>
                <% } %>
            </div>

            <div class="form-group">
                <label for="email">邮箱</label>
                <input type="email" id="email" name="email"
                       value="<%= formEmail %>" placeholder="请输入邮箱地址">
                <% if (errors != null && errors.containsKey("email")) { %>
                    <div class="error"><%= errors.get("email") %></div>
                <% } %>
            </div>

            <div class="form-group">
                <label for="phone">手机号</label>
                <input type="text" id="phone" name="phone"
                       value="<%= formPhone %>" placeholder="请输入11位手机号">
                <% if (errors != null && errors.containsKey("phone")) { %>
                    <div class="error"><%= errors.get("phone") %></div>
                <% } %>
            </div>

            <input type="submit" value="注册">
        </form>
    </div>
</body>
</html>
