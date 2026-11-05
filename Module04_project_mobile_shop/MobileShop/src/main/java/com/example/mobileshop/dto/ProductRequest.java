package com.example.mobileshop.dto;

import com.example.mobileshop.entity.ProductStatus;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record ProductRequest(
        @NotBlank @Size(max = 160) String name,
        @NotBlank @Size(max = 90) String brand,
        @NotNull @DecimalMin("0.0") BigDecimal price,
        @NotNull @Min(0) Integer stock,
        @Size(max = 1000) @Pattern(regexp = "^$|^/uploads/[0-9a-f-]{36}\\.(jpg|png)$", message = "Ảnh phải được tải từ máy tính lên hệ thống") String imageUrl,
        ProductStatus status
) {
}
