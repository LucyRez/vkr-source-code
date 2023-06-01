package cs.hse.scansprovider.studyManagement;

import cs.hse.scansprovider.client.AmazonClient;
import cs.hse.scansprovider.fileManagement.models.DICOMInfo;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class StudiesService {
    private final AmazonClient amazonClient;

    public ResponseEntity<DICOMInfo> getSingleFileInfo(String key) {
        var info = amazonClient.getSingleFileInfo(key);
        if (info == null) {
            return new ResponseEntity<>(null, HttpStatus.BAD_GATEWAY);
        }

        return ResponseEntity.ok(info);
    }

    public ResponseEntity<StudyResponse> getStudyInfo(String key) {
        var info = amazonClient.getStudyInfo(key);
        if (info == null) {
            return new ResponseEntity<>(null, HttpStatus.BAD_GATEWAY);
        }

        return ResponseEntity.ok(info);
    }

    public ResponseEntity<List<StudyResponse>> getStudies(String organizationName) {
        return ResponseEntity.ok(amazonClient.getStudies(organizationName));
    }

    public ResponseEntity<String> comment(String key, String comment) {

        var response =  amazonClient.comment(key, comment);

        switch (response) {
            case SUCCESSFUL_COMMENT -> {
                return ResponseEntity.ok("Comment successfully added");
            }
            case FILE_NOT_FOUND -> {
                return new ResponseEntity<>("File for comment was not found", HttpStatus.BAD_REQUEST);
            }
            case S3_PARSE_CONNECT_ERROR -> {
                return new ResponseEntity<>("Error occurred while parsing dcm file", HttpStatus.BAD_GATEWAY);
            }
            default ->
            {
                return new ResponseEntity<>("Error", HttpStatus.BAD_REQUEST);
            }
        }
    }

    public ResponseEntity<String> getFile(String key) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        return new ResponseEntity<>(amazonClient.getFileBinary(key), headers, HttpStatus.OK);
    }

    public ResponseEntity<byte[]> getFileAsByte(String key) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        return new ResponseEntity<>(amazonClient.getFileByte(key), headers, HttpStatus.OK);
    }

}
