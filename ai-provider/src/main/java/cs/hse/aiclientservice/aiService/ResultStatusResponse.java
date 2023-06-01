package cs.hse.aiclientservice.aiService;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
public class ResultStatusResponse {
    String key;
    String status;
    float totalVolume;
    float percentage;
}
