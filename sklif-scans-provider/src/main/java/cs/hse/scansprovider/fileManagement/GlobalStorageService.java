package cs.hse.scansprovider.fileManagement;

import com.amazonaws.services.s3.model.S3ObjectSummary;
import cs.hse.scansprovider.client.AmazonClient;
import cs.hse.scansprovider.fileManagement.models.FileResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;


import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class GlobalStorageService {

    private final AmazonClient amazonClient;

    public ResponseEntity<List<FileResponse>> getAllFiles() {
        var files = amazonClient.getAllFiles().stream().map(
                (file) -> new FileResponse(file.getBucketName(), file.getKey(), file.getETag(), file.getSize(),
                        file.getLastModified(), file.getStorageClass())
        ).collect(Collectors.toList());
        return ResponseEntity.ok(files);
    }

    public ResponseEntity<String> createDirectory(String directoryName) {
        var response = amazonClient.createDirectory(directoryName);

        switch (response) {
            case FOLDER_CREATION_SUCCESSFUL -> {
                return ResponseEntity.ok("Folder created successfully");
            }
            case FOLDER_CREATION_FAILED -> {
                return new ResponseEntity<>("Folder creation failed", HttpStatus.BAD_GATEWAY);
            }
            default ->
            {
                return new ResponseEntity<>("Error", HttpStatus.BAD_REQUEST);
            }
        }
    }

    public ResponseEntity<String> uploadFile(MultipartFile multipartFile, String organizationsName) {
        var fileUrl = amazonClient.uploadFile(multipartFile, organizationsName);
        if (!fileUrl.isEmpty()) {
            return ResponseEntity.ok(fileUrl);
        }
        return new ResponseEntity<>("Could not upload file", HttpStatus.BAD_REQUEST);
    }

    public ResponseEntity<String> deleteFiles(List<String> fileNames) {

        var response = amazonClient.deleteFiles(fileNames);

        switch (response) {
            case S3_ERROR -> {
                return new ResponseEntity<>("Encountered S3 error", HttpStatus.BAD_GATEWAY);
            }
            case PARTIAL_DELETE -> {
                return new ResponseEntity<>("Files have been deleted only partially", HttpStatus.BAD_GATEWAY);
            }
            case S3_PARSE_CONNECT_ERROR -> {
                return new ResponseEntity<>("S3 could not be connected", HttpStatus.I_AM_A_TEAPOT);
            }
            case SUCCESSFUL_DELETE -> {
                return ResponseEntity.ok("Successful delete");
            }
            default ->
            {
                return new ResponseEntity<>("Error", HttpStatus.BAD_GATEWAY);
            }
        }
    }

    public ResponseEntity<String> deleteFolderAndFiles(String folderName) {
        var response = amazonClient.deleteFolder(folderName);

        switch (response) {
            case BUCKET_NOT_FOUND -> {
                return new ResponseEntity<>("There is no such bucket", HttpStatus.BAD_REQUEST);
            }
            case SUCCESSFUL_DELETE -> {
                return ResponseEntity.ok("Successful delete");
            }
            case ORGANIZATION_DIRECTORY_NOT_FOUND -> {
                return ResponseEntity.ok("No such directory");
            }
            default ->
            {
                return new ResponseEntity<>("Error", HttpStatus.BAD_GATEWAY);
            }
        }
    }
}
