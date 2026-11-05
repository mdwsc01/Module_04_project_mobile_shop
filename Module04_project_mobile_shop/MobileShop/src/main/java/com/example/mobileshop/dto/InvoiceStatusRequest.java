package com.example.mobileshop.dto;

import com.example.mobileshop.entity.InvoiceStatus;
import jakarta.validation.constraints.NotNull;

public record InvoiceStatusRequest(@NotNull InvoiceStatus status) {
}
