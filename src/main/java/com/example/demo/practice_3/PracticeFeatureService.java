package com.example.demo.practice_3;


import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(
        name = "practice.feature.enabled",
        havingValue = "true"
)
public class PracticeFeatureService {

    public String execute() {
        return "Practice feature enabled";
    }
}
