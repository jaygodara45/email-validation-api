package com.javaguides.email;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WelcomeMessage {

    @GetMapping("/welcome")
    public String welcome(){
        return "Hello! Welcome to me!";
    }
}
