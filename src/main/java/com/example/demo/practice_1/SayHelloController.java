package com.example.demo.practice_1;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SayHelloController {

    private final SayHelloService sayHelloService;

    public SayHelloController(SayHelloService sayHelloService) {
        this.sayHelloService = sayHelloService;
    }

    @GetMapping("/hello")
    public String hello() {
        return sayHelloService.sayHello();
    }
}