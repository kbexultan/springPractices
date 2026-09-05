package com.example.demo.practice_1;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SayHelloController {

    @Autowired
    private SayHelloService sayHelloService;

    @GetMapping("/hello")
    public String hello() {
        return sayHelloService.sayHello();
    }
}