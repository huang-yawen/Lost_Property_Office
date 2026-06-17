package com.example.controller;

import com.example.dao.DbUtil;
import com.example.model.RegisterFormBean;
import com.example.model.UserBean;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/register")
public class ControllerServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // 1. 接收注册表单参数
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        String confirmPassword = request.getParameter("confirmPassword");
        String email = request.getParameter("email");
        String phone = request.getParameter("phone");

        // 2. 创建 RegisterFormBean 对象并进行表单校验
        RegisterFormBean formBean = new RegisterFormBean(username, password, confirmPassword, email, phone);
        boolean isValid = formBean.validate();

        if (!isValid) {
            // 校验失败：将错误信息和已填写的合法数据存入 request 域，转发回 register.jsp
            request.setAttribute("formBean", formBean);
            request.getRequestDispatcher("/register.jsp").forward(request, response);
            return;
        }

        // 3. 校验通过，检查用户名是否已存在
        try {
            UserBean existingUser = DbUtil.findUserByUsername(username);
            if (existingUser != null) {
                // 用户名已存在
                formBean.getErrors().put("username", "用户名已被注册");
                request.setAttribute("formBean", formBean);
                request.getRequestDispatcher("/register.jsp").forward(request, response);
                return;
            }

            // 4. 用户名不存在，创建 UserBean 并保存到数据库
            UserBean user = new UserBean(username, password, email, phone);
            DbUtil.saveUser(user);

            // 5. 注册成功，将 UserBean 存入 session（模拟自动登录）
            HttpSession session = request.getSession();
            session.setAttribute("user", user);

            // 6. 重定向到 loginSuccess.jsp
            response.sendRedirect(request.getContextPath() + "/loginSuccess.jsp");

        } catch (SQLException e) {
            e.printStackTrace();
            // 数据库异常，跳转到错误页面或返回注册页
            request.setAttribute("error", "系统内部错误，请稍后重试");
            request.getRequestDispatcher("/register.jsp").forward(request, response);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // GET 请求直接重定向到注册页面
        response.sendRedirect(request.getContextPath() + "/register.jsp");
    }
}