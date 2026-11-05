package com.example.mobileshop.dto;

import jakarta.validation.constraints.*;

public record CustomerRequest(
        @NotBlank @Size(max = 120) String name,
        @NotBlank @Email @Size(max = 160) String email,
        @NotBlank @Pattern(regexp = "^[0-9+(). -]{9,20}$", message = "Số điện thoại không hợp lệ") String phone,
        @NotBlank @Size(max = 255) String address,
        Boolean active
) {
}
