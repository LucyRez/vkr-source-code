package cs.hse.scansprovider.fileManagement;

import cs.hse.scansprovider.fileManagement.models.FileResponse;
import cs.hse.scansprovider.fileManagement.models.RenameFileRequest;
import cs.hse.scansprovider.fileManagement.models.UploadFilesResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("api/storage/local")
public class LocalStorageController {

    private final OriginFilter originFilter = new OriginFilter();
    private final LocalStorageService localStorageService;

    @GetMapping("/get")
    public @ResponseBody ResponseEntity<List<FileResponse>> getAllOrganizationFiles(@RequestParam String organizationName) {
        if ( !originFilter.checkOrigin()) {
            return new ResponseEntity("Prohibited: not registered origin", HttpStatus.UNAUTHORIZED);
        }
        return localStorageService.getAllOrganizationFiles(organizationName);
    }

    @PostMapping("/upload")
    public @ResponseBody ResponseEntity<UploadFilesResponse> uploadFiles(@RequestPart MultipartFile[] multipartFiles, @RequestParam String path) {
        if ( !originFilter.checkOrigin()) {
            return new ResponseEntity<>(null, HttpStatus.UNAUTHORIZED);
        }
        return localStorageService.uploadFiles(multipartFiles, path);
    }

    @PostMapping("/rename")
    public @ResponseBody ResponseEntity<String> renameFile(@RequestBody RenameFileRequest request) {
        if ( !originFilter.checkOrigin()) {
            return new ResponseEntity<>(null, HttpStatus.UNAUTHORIZED);
        }

        return localStorageService.renameFile(request);
    }

}
