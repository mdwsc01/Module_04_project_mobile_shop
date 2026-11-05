package com.example.mobileshop.dto;

import jakarta.validation.constraints.*;

public class RegisterRequest {
    @NotBlank(message = "Vui lòng nhập username")
    @Pattern(regexp = "^[A-Za-z0-9_]{3,60}$", message = "Username dài 3–60 ký tự, gồm chữ, số hoặc dấu _")
    private String username;
    @NotBlank(message = "Vui lòng nhập mật khẩu")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{6,}$",
            message = "Mật khẩu tối thiểu 6 ký tự, gồm chữ thường, chữ hoa, số và ký tự đặc biệt")
    private String password;
    @NotBlank(message = "Vui lòng xác nhận mật khẩu")
    private String confirmPassword;

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
}
