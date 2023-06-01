package cs.hse.aiclientservice.aiService;

import cs.hse.aiclientservice.aiService.client.examples.SimpleInferClient;
import cs.hse.aiclientservice.aiService.s3controller.AmazonClient;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
@EnableAsync
@AllArgsConstructor
public class AIRequestSender {


    private final AmazonClient amazonClient;
    private final SimpleInferClient simpleInferClient = new SimpleInferClient();

    public Map<String, ResultStatusModel> results = new HashMap<>();


    public ResultStatusResponse getResultsByKey(String key) {
        log.info("Total results: " + results.keySet().size());
        if (!results.containsKey(key)) {
            return null;
        }

        var res = results.get(key);
        float processedFloat = res.getFilesProcessed();
        return new ResultStatusResponse(
                res.getKey(),
                res.getStatus(),
                res.getTotalVolume(),
                (processedFloat/res.getTotalFiles()) * 100
        );
    }

    @Async
    public void countTotalVolumeAndGetMasks(List<String> keys, String newFolderKey, String studyKey) {
        // Get DICOM for key and convert to int array
        // Get float array
        // Send the data to inference server
        // Convert the array to image
        // Save image in s3

        ResultStatusModel resultStatusModel = new ResultStatusModel(newFolderKey + "/", ResultStatus.IN_PROGRESS.name(), 0.0f, 0, keys.size());

        results.put(studyKey, resultStatusModel);
        log.info("Did put empty result: " + results.keySet().size());
        float totalVolume = 0f;
        for (String fileKey: keys) {
            int[] fileAsIntArray = amazonClient.getFileAsArray(fileKey);

            if (fileAsIntArray == null) {
                continue;
            }
            int width = 512;
            int height = 512;
            int depth = 1;

            var split =  fileKey.split("/");
            String tmpFileName = split[split.length-1];

            float[] fileAsArray = simpleInferClient.intArrayToFloatArray(fileAsIntArray);
            float[][][] threeDimArray = simpleInferClient.oneDimToThreeDim(fileAsArray, width, height, depth);
            totalVolume +=
                    simpleInferClient.getMaskData(tmpFileName, threeDimArray, fileAsArray,
                            width, height, depth);

            String fileName = newFolderKey + "/" + tmpFileName + "_mask" + ".jpg";
            File image = new File(tmpFileName + "_mask" + ".jpg");

            amazonClient.uploadFileTos3bucket(fileName, image);
            image.delete();
            resultStatusModel.setFilesProcessed(resultStatusModel.filesProcessed + 1);
            results.put(studyKey, resultStatusModel);

        }

        log.info("Volume: " + totalVolume/1000);
        resultStatusModel.setStatus(ResultStatus.READY.name());
        resultStatusModel.setTotalVolume(totalVolume/1000);
        results.put(studyKey, resultStatusModel);
        log.info("Did put final result: " + results.get(studyKey).getTotalVolume());
    }

}
