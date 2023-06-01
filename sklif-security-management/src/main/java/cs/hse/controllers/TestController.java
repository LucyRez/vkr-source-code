package cs.hse.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping("/api/welcome")
    public @ResponseBody String getWelcomeMessage() {
        return "Welcome! This is API v.1.0";
    }
}
