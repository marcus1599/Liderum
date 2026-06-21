package com.example.liderumnotification.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class StatusController {

    @GetMapping("/")
    public String status() {
        return "liderum-notification-service is running";
    }
}
