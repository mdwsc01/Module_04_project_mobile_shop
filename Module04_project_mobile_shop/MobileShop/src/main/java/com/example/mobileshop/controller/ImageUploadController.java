package com.example.mobileshop.controller;

import com.example.mobileshop.service.ImageStorageService;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/uploads")
public class ImageUploadController {
    private final ImageStorageService storage;

    public ImageUploadController(ImageStorageService storage) {
        this.storage = storage;
    }

    @PostMapping(value = "/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String, String> upload(@RequestParam("file") MultipartFile file) throws IOException {
        return Map.of("imageUrl", storage.store(file));
    }
}
