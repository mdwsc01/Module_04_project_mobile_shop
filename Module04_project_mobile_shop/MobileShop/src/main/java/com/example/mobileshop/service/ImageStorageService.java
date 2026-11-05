package com.example.mobileshop.service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import javax.imageio.ImageIO;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;

@Service
public class ImageStorageService {
    private final Path directory;
    public ImageStorageService(@Value("${file.upload-dir:uploads}") String root) throws IOException {
        directory=Paths.get(root).toAbsolutePath().normalize();
        Files.createDirectories(directory);
    }
    public String store(MultipartFile file) throws IOException {
        if(file.isEmpty()) throw bad("Vui lòng chọn một file ảnh");
        if(file.getSize()>5*1024*1024) throw bad("Ảnh phải nhỏ hơn hoặc bằng 5 MB");
        if(!Set.of("image/jpeg","image/png").contains(Objects.toString(file.getContentType(),""))) throw bad("Chỉ hỗ trợ JPG hoặc PNG");
        String ext;
        try(var input=ImageIO.createImageInputStream(file.getInputStream())) {
            if(input==null) throw bad("File ảnh không hợp lệ");
            var readers=ImageIO.getImageReaders(input);
            if(!readers.hasNext()) throw bad("File không phải ảnh hợp lệ");
            var reader=readers.next();
            try {
                reader.setInput(input);
                String format=reader.getFormatName().toLowerCase(Locale.ROOT);
                if(format.equals("jpeg") && file.getContentType().equals("image/jpeg")) ext=".jpg";
                else if(format.equals("png") && file.getContentType().equals("image/png")) ext=".png";
                else throw bad("Định dạng file không khớp nội dung ảnh");
                int w=reader.getWidth(0),h=reader.getHeight(0);
                if(w<1||h<1||w>6000||h>6000) throw bad("Kích thước ảnh không hợp lệ (tối đa 6000px)");
            } finally {reader.dispose();}
        } catch(javax.imageio.IIOException ex) {throw bad("Ảnh bị hỏng hoặc không đọc được");}
        String filename=UUID.randomUUID()+ext;
        Files.copy(file.getInputStream(),directory.resolve(filename),StandardCopyOption.REPLACE_EXISTING);
        return "/uploads/"+filename;
    }
    private ResponseStatusException bad(String reason) {return new ResponseStatusException(HttpStatus.BAD_REQUEST,reason);}
    public Path directory(){return directory;}
}
