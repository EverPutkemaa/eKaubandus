package eKaubandus.eKauplus.api.service.Impl;

import eKaubandus.eKauplus.api.service.S3Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.UUID;

@Service
public class S3ServiceImpl implements S3Service {

    @Autowired
    private S3Client s3Client;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    @Override
    public String uploadFile(MultipartFile file, String folderName) throws IOException {

        // Genereeri unikaalne failinimi
        String fileExtension = getFileExtension(file.getOriginalFilename());
        String key = folderName + "/" + UUID.randomUUID().toString() + fileExtension;

        // Laadi fail üles S3-sse
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType(file.getContentType())
                .build();

        s3Client.putObject(putObjectRequest,
                RequestBody.fromBytes(file.getBytes()));

        // Tagasta URL failile
        return "https://" + bucketName + ".s3.amazonaws.com/" + key;
    }

    @Override
    public void deleteFile(String fileUrl) {

        if (fileUrl != null && fileUrl.contains(bucketName)) {
            String key = fileUrl.substring(fileUrl.indexOf(bucketName) + bucketName.length() + 1);

            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            s3Client.deleteObject(deleteObjectRequest);
        }
    }

    private String getFileExtension(String filename) {
        if (filename == null || filename.lastIndexOf(".") == -1) {
            return "";
        }
        return filename.substring(filename.lastIndexOf("."));
    }

}