package cs.hse.aiclientservice.aiService;

import lombok.*;

enum ResultStatus {
    IN_PROGRESS,
    READY,
    FAILED
}


@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
public class ResultStatusModel {
    String key;
    String status;
    float totalVolume;
    int filesProcessed;
    int totalFiles;
}

