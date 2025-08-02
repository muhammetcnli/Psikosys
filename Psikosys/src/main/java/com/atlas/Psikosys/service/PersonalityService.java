package com.atlas.Psikosys.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

@Service
public class PersonalityService {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public Map<String, PersonalityData> getAllPersonalities(String language) {
        try {
            String fileName = "personalities-" + language + ".json";
            ClassPathResource resource = new ClassPathResource(fileName);

            if (!resource.exists()) {
                // Fallback to default personalities.json
                resource = new ClassPathResource("personalities.json");
            }

            try (InputStream inputStream = resource.getInputStream()) {
                return objectMapper.readValue(inputStream, new TypeReference<Map<String, PersonalityData>>() {});
            }
        } catch (IOException e) {
            throw new RuntimeException("Could not load personalities for language: " + language, e);
        }
    }

    public Map<String, PersonalityData> getAllPersonalities() {
        // Default Türkçe personalities
        return getAllPersonalities("tr");
    }

    public String getPersonalityPrompt(String personalityKey, String language) {
        Map<String, PersonalityData> personalities = getAllPersonalities(language);
        PersonalityData personality = personalities.get(personalityKey);

        if (personality != null) {
            return personality.getPrompt();
        }

        // Fallback: default language (Turkish) prompt
        Map<String, PersonalityData> fallbackPersonalities = getAllPersonalities("tr");
        PersonalityData fallbackPersonality = fallbackPersonalities.get(personalityKey);

        if (fallbackPersonality != null) {
            return fallbackPersonality.getPrompt();
        }

        throw new RuntimeException("Personality not found: " + personalityKey);
    }

    public String getPersonalityPrompt(String personalityKey) {
        // Default Türkçe
        return getPersonalityPrompt(personalityKey, "tr");
    }

    public static class PersonalityData {
        private String name;
        private String description;
        private String prompt;
        private String analysisStyle;

        // Getters and setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getPrompt() { return prompt; }
        public void setPrompt(String prompt) { this.prompt = prompt; }
        public String getAnalysisStyle() { return analysisStyle; }
        public void setAnalysisStyle(String analysisStyle) { this.analysisStyle = analysisStyle; }
    }
}