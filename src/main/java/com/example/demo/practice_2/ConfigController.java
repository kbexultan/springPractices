package com.example.demo.practice_2;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ConfigController {

    @Autowired
    private AppProperties appProperties;

    @GetMapping("/config")
    public String config() {
        return "message: " + appProperties.getMessage() + ", timeout: " + appProperties.getTimeout();
    }
}
