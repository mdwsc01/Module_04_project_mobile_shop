package com.example.mobileshop;
import com.example.mobileshop.service.ImageStorageService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.server.ResponseStatusException;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.file.*;
import javax.imageio.ImageIO;
import static org.junit.jupiter.api.Assertions.*;

class ImageStorageServiceTest {
 @TempDir Path root;
 @Test void pngIsSavedWithGeneratedName() throws Exception {
    var img=new BufferedImage(5,5,BufferedImage.TYPE_INT_RGB);
    var out=new ByteArrayOutputStream();ImageIO.write(img,"png",out);
    var f=new MockMultipartFile("file","sample.png","image/png",out.toByteArray());
    var service=new ImageStorageService(root.toString());
    var url=service.store(f);
    assertTrue(url.matches("^/uploads/[a-f0-9-]{36}\\.png$"));
    assertTrue(Files.exists(root.resolve(url.substring("/uploads/".length()))));
 }
 @Test void rejectsNonImage() throws Exception {
    var f=new MockMultipartFile("file","fake.png","image/png","not-a-real-image".getBytes());
    var service=new ImageStorageService(root.toString());
    assertThrows(ResponseStatusException.class,()->service.store(f));
 }
 @Test void rejectsWrongMime() throws Exception {
    var f=new MockMultipartFile("file","script.svg","image/svg+xml","<svg/>".getBytes());
    var service=new ImageStorageService(root.toString());
    assertThrows(ResponseStatusException.class,()->service.store(f));
 }
}
