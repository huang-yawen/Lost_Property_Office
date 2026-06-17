package com.example.model;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class RegisterFormBean {
    private String username;
    private String password;
    private String confirmPassword;
    private String email;
    private String phone;

    private Map<String, String> errors;

    public RegisterFormBean() {
        errors = new HashMap<>();
    }

    public RegisterFormBean(String username, String password, String confirmPassword, String email, String phone) {
        this();
        this.username = username;
        this.password = password;
        this.confirmPassword = confirmPassword;
        this.email = email;
        this.phone = phone;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Map<String, String> getErrors() {
        return errors;
    }

    public void setErrors(Map<String, String> errors) {
        this.errors = errors;
    }

    /**
     * 校验注册表单数据
     * @return true if validation passes, false otherwise
     */
    public boolean validate() {
        boolean valid = true;

        // 验证用户名
        if (username == null || username.trim().isEmpty()) {
            errors.put("username", "用户名不能为空");
            valid = false;
        }

        // 验证密码
        if (password == null || password.trim().isEmpty()) {
            errors.put("password", "密码不能为空");
            valid = false;
        }

        // 验证确认密码
        if (confirmPassword == null || confirmPassword.trim().isEmpty()) {
            errors.put("confirmPassword", "确认密码不能为空");
            valid = false;
        }

        // 验证密码与确认密码是否一致
        if (password != null && confirmPassword != null
                && !password.isEmpty() && !confirmPassword.isEmpty()
                && !password.equals(confirmPassword)) {
            errors.put("confirmPassword", "密码与确认密码不一致");
            valid = false;
        }

        // 验证邮箱
        if (email == null || email.trim().isEmpty()) {
            errors.put("email", "邮箱不能为空");
            valid = false;
        } else {
            String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
            if (!Pattern.matches(emailRegex, email)) {
                errors.put("email", "邮箱格式不正确");
                valid = false;
            }
        }

        // 验证手机号
        if (phone == null || phone.trim().isEmpty()) {
            errors.put("phone", "手机号不能为空");
            valid = false;
        } else {
            String phoneRegex = "^1[3-9]\\d{9}$";
            if (!Pattern.matches(phoneRegex, phone)) {
                errors.put("phone", "手机号格式不正确（需为11位手机号）");
                valid = false;
            }
        }

        return valid;
    }
}