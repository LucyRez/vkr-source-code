package cs.hse.scansprovider.fileManagement;

import com.amazonaws.services.s3.model.S3ObjectSummary;
import cs.hse.scansprovider.fileManagement.models.CreateDirectoryRequest;
import cs.hse.scansprovider.fileManagement.models.DeleteFilesRequest;
import cs.hse.scansprovider.fileManagement.models.FileResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("api/storage/global")
public class GlobalStorageController {
    private final GlobalStorageService globalStorageService;
    private final OriginFilter originFilter = new OriginFilter();

    @PostMapping("create")
    public @ResponseBody ResponseEntity<String> createOrganizationDirectory(@RequestBody CreateDirectoryRequest createDirectoryRequest) {
        if ( !originFilter.checkOrigin()) {
            return new ResponseEntity<>("Prohibited: not registered origin", HttpStatus.UNAUTHORIZED);
        }
        return globalStorageService.createDirectory(createDirectoryRequest.getOrganizationName());
    }

    @GetMapping("get")
    public @ResponseBody ResponseEntity<List<FileResponse>> getAllFiles() {
        if ( !originFilter.checkOrigin()) {
            return new ResponseEntity("Prohibited: not registered origin", HttpStatus.UNAUTHORIZED);
        }
        return globalStorageService.getAllFiles();
    }

    @PostMapping("upload")
    public @ResponseBody ResponseEntity<String> uploadFile(@RequestPart MultipartFile multipartFile, @RequestParam String organizationName) {
        if ( !originFilter.checkOrigin()) {
            return new ResponseEntity<>("Prohibited: not registered origin", HttpStatus.UNAUTHORIZED);
        }
        return globalStorageService.uploadFile(multipartFile, organizationName);
    }

    @PostMapping("delete")
    public @ResponseBody ResponseEntity<String> deleteFiles(@RequestBody DeleteFilesRequest deleteFilesRequest) {
        if ( !originFilter.checkOrigin()) {
            return new ResponseEntity<>("Prohibited: not registered origin", HttpStatus.UNAUTHORIZED);
        }
        return  globalStorageService.deleteFiles(deleteFilesRequest.getFileNames());
    }

    @GetMapping("delete-folder")
    public @ResponseBody ResponseEntity<String> deleteFolder(@RequestParam String folderName) {
        if ( !originFilter.checkOrigin()) {
            return new ResponseEntity<>("Prohibited: not registered origin", HttpStatus.UNAUTHORIZED);
        }
        return  globalStorageService.deleteFolderAndFiles(folderName);
    }


}
