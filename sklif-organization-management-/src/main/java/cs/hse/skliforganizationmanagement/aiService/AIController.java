package cs.hse.skliforganizationmanagement.aiService;

import lombok.AllArgsConstructor;
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
    public String getMask(@RequestParam("id") Long id) {
        return aiService.getMaskForDICOM(id);
    }

}
