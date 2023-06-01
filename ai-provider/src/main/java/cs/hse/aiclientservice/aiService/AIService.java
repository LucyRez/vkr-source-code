package cs.hse.aiclientservice.aiService;

import cs.hse.aiclientservice.aiService.client.examples.SimpleInferClient;
import cs.hse.aiclientservice.aiService.s3controller.AmazonClient;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@AllArgsConstructor
public class AIService {

    private final AmazonClient amazonClient;
    AIRequestSender aiRequestSender;

    public ResponseEntity<String> getMaskForStudy(String key) {

        // Get dicom file from s3 storage by key
        List<String> keys = amazonClient.getStudyDICOM(key);

        if (keys.isEmpty()) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }

        var tmp = key.split("/");
        String org = tmp[0];
        String study = tmp[tmp.length-1];

        String newFolderKey = org + "/ai-masks/" + study;

        // Create folder in s3 to send the results
        var creationResponse = amazonClient.createDirectory(newFolderKey);
        if (creationResponse != AmazonClient.ResponseType.FOLDER_CREATION_SUCCESSFUL) {
            return new ResponseEntity<>(null, HttpStatus.BAD_GATEWAY);
        }

        aiRequestSender.countTotalVolumeAndGetMasks(keys, newFolderKey, key);

        return ResponseEntity.ok(newFolderKey + "/");
    }

    public ResponseEntity<ResultStatusResponse> getResultsForStudy(String key) {
        return ResponseEntity.ok(aiRequestSender.getResultsByKey(key));
    }

}
