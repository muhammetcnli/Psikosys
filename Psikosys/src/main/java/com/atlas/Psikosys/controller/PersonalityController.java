package com.atlas.Psikosys.controller;

import com.atlas.Psikosys.service.PersonalityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@RestController
@RequestMapping("/api/personalities")
public class PersonalityController {

    @Autowired
    private PersonalityService personalityService;

    @GetMapping
    public Map<String, Map<String, Object>> getAllPersonalities() {
        return personalityService.getAllPersonalities();
    }
}