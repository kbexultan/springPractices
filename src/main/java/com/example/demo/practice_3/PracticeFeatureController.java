package com.example.demo.practice_3;


import org.springframework.beans.factory.ObjectProvider;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PracticeFeatureController {

    private final ObjectProvider<PracticeFeatureService> practiceFeatureService;

    public PracticeFeatureController(ObjectProvider<PracticeFeatureService> practiceFeatureService) {
        this.practiceFeatureService = practiceFeatureService;
    }

    @GetMapping("/feature")
    public String feature() {
        PracticeFeatureService service = practiceFeatureService.getIfAvailable();
        return service != null ? service.execute() : "Practice feature disabled";
    }
}
