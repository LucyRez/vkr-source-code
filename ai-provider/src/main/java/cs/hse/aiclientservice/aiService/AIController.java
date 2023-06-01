package cs.hse.aiclientservice.aiService;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("api/ai")
public class AIController {

    private final AIService aiService;

    @GetMapping("get_mask")
    public ResponseEntity<String> getMask(@RequestParam("key") String key) {
        return aiService.getMaskForStudy(key);
    }

    @GetMapping("get_results")
    public ResponseEntity<ResultStatusResponse> getResults(@RequestParam("key") String key) {
        return aiService.getResultsForStudy(key);
    }

}
