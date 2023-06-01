package cs.hse.scansprovider.fileManagement;

import cs.hse.scansprovider.client.AmazonClient;
import cs.hse.scansprovider.fileManagement.models.DeleteFilesRequest;
import cs.hse.scansprovider.fileManagement.models.FileResponse;
import cs.hse.scansprovider.fileManagement.models.RenameFileRequest;
import cs.hse.scansprovider.fileManagement.models.UploadFilesResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class LocalStorageService {

    private final AmazonClient amazonClient;

    public ResponseEntity<List<FileResponse>> getAllOrganizationFiles(String organizationName) {
        var files = amazonClient.getAllFilesWith(organizationName).stream().map(
                (file) -> new FileResponse(file.getBucketName(), file.getKey().replaceFirst(organizationName, ""), file.getETag(), file.getSize(),
                        file.getLastModified(), file.getStorageClass())
        ).collect(Collectors.toList());
        return ResponseEntity.ok(files);
    }

    public ResponseEntity<UploadFilesResponse> uploadFiles(MultipartFile[] multipartFiles, String organizationsName) {

        List<String> urls = new ArrayList<>();

        for (MultipartFile file:
             multipartFiles) {
            var fileUrl = amazonClient.uploadFile(file, organizationsName);
            if (fileUrl.isEmpty()) {
                return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);

            }
            urls.add(fileUrl);

        }

        return ResponseEntity.ok(new UploadFilesResponse(urls));
    }

    public ResponseEntity<String> renameFile(RenameFileRequest request) {
       if (amazonClient.renameFile(request.getOldPath(), request.getNewPath()) == AmazonClient.ResponseType.SUCCESSFUL_RENAME) {
           return ResponseEntity.ok("File renamed successfully");
       }
       return new ResponseEntity<>("Encountered S3 error", HttpStatus.BAD_GATEWAY);
    }

}
