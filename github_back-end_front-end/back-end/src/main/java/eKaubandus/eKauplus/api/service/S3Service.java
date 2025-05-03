package eKaubandus.eKauplus.api.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface S3Service {

    String uploadFile(MultipartFile file, String folderName) throws IOException;
    void deleteFile(String fileUrl);
}
