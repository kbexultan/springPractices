package com.example.demo.practice_1;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class sayHelloController {

    @GetMapping("/hello")
    public String hello() {
        return "Hello Bexultan!";
    }
}