package cs.hse.scansprovider.studyManagement;

import cs.hse.scansprovider.fileManagement.OriginFilter;
import cs.hse.scansprovider.fileManagement.models.CommentRequest;
import cs.hse.scansprovider.fileManagement.models.DICOMInfo;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("api/storage/studies")
public class StudiesController {
    private  final StudiesService studiesService;
    private final OriginFilter originFilter = new OriginFilter();

    // Get all studies for organization
    @GetMapping("/get")
    public @ResponseBody ResponseEntity<List<StudyResponse>> getAllOrganizationStudies(@RequestParam String organizationDirectory) {
        if ( !originFilter.checkOrigin()) {
            return new ResponseEntity("Prohibited: not registered origin", HttpStatus.UNAUTHORIZED);
        }
        return studiesService.getStudies(organizationDirectory);
    }

    // Get one study by its key
    @GetMapping("/get-study")
    public @ResponseBody ResponseEntity<StudyResponse> getStudyInfo(@RequestParam String key) {
        if ( !originFilter.checkOrigin()) {
            return new ResponseEntity("Prohibited: not registered origin", HttpStatus.UNAUTHORIZED);
        }
        return studiesService.getStudyInfo(key);
    }

    // Get information about a single file knowing it's key
    @GetMapping("/get-info")
    public @ResponseBody ResponseEntity<DICOMInfo> getSingleFileInfo(@RequestParam String key) {
        if ( !originFilter.checkOrigin()) {
            return new ResponseEntity("Prohibited: not registered origin", HttpStatus.UNAUTHORIZED);
        }
        return studiesService.getSingleFileInfo(key);
    }

    // Add a comment to a file knowing its key
    @PostMapping("/comment")
    public @ResponseBody ResponseEntity<String> comment(@RequestBody CommentRequest comment) {
        if ( !originFilter.checkOrigin()) {
            return new ResponseEntity("Prohibited: not registered origin", HttpStatus.UNAUTHORIZED);
        }
        return studiesService.comment(comment.getKey(), comment.getComment());
    }

    @GetMapping(path ="/get-file")
    public @ResponseBody ResponseEntity<String> getFile(@RequestParam String key) {
        if ( !originFilter.checkOrigin()) {
            return new ResponseEntity("Prohibited: not registered origin", HttpStatus.UNAUTHORIZED);
        }
        return studiesService.getFile(key);
    }

    @GetMapping(path ="/get-byte")
    public @ResponseBody ResponseEntity<byte[]> getFileAsByte(@RequestParam String key) {
        if ( !originFilter.checkOrigin()) {
            return new ResponseEntity("Prohibited: not registered origin", HttpStatus.UNAUTHORIZED);
        }
        return studiesService.getFileAsByte(key);
    }

}
