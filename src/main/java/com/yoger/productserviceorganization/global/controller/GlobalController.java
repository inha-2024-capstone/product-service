package com.yoger.productserviceorganization.global.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GlobalController {
    @GetMapping("/health")
    public ResponseEntity<Void> healthCheck() {
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
