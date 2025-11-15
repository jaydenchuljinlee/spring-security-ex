package com.example.security.view.web.main;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class PassedController {
    @ResponseBody
    @GetMapping("/passed")
    public String passedLogin() {
        return "login is passed";
    }
}
