package com.example.mobileshop.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.util.List;

public record InvoiceRequest(
        @NotNull Long customerId,
        @NotEmpty List<@Valid Item> items
) {
    public record Item(@NotNull Long productId, @Min(1) int quantity) {
    }
}
