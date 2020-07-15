package com.ppdai.stargate.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {
    @RequestMapping(value = "/hs", method = RequestMethod.GET)
    public ResponseEntity<String> checkHealth() {
        return new ResponseEntity<>("OK", HttpStatus.OK);
    }
}
