package com.example.learning.controller;

import com.example.learning.record.Person;
import com.example.learning.service.StructuredOutputService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/output")
public class StructuredOutputController {

    @Resource
    private StructuredOutputService outputService;

    @GetMapping("/person")
    public Person extractPerson(@RequestParam String text) {
        return outputService.extractPerson(text);
    }

    @GetMapping("/personConverter")
    public Person extractPersonConverter(@RequestParam String text) {
        return outputService.extractPersonConverter(text);
    }
}
