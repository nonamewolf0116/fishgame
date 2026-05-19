package com.example.fishgame.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(origins = "http://localhost:63342")
public class TestController {

    @GetMapping("/test")
    public String test() {
        return "backend running";
    }
}
