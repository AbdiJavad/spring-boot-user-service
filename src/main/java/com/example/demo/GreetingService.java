package com.example.demo;

import org.springframework.stereotype.Service;

@Service
public class GreetingService {

    public String getFormalGreeting() {
        return "Sehr geehrter Herr Jawad, willkommen in Deutschland!";
    }
}
