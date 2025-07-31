package com.atlas.Psikosys.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.util.Map;

@Service
public class PersonalityService {

    @Value("classpath:personalities.json")
    private Resource personalitiesResource;

    private Map<String, Map<String, Object>> personalities;

    @PostConstruct
    public void init() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        personalities = mapper.readValue(personalitiesResource.getInputStream(),
                new TypeReference<Map<String, Map<String, Object>>>() {});
    }

    public Map<String, Map<String, Object>> getAllPersonalities() {
        return personalities;
    }

    public Map<String, Object> getPersonality(String key) {
        return personalities.get(key);
    }

    public String getPersonalityPrompt(String personalityName) {
        System.out.println("PersonalityService.getPersonalityPrompt çağrıldı: " + personalityName);

        Map<String, Object> personality = personalities.get(personalityName);
        if (personality != null) {
            String prompt = (String) personality.get("prompt");

            if (prompt != null) {
                // {message} placeholder kontrolü
                if (!prompt.contains("{message}")) {
                    System.out.println("UYARI: " + personalityName + " personality'sinde {message} placeholder'ı yok!");
                }
                return prompt;
            }
        }

        System.out.println("Personality bulunamadı, default döndürülüyor");
        return "Sen deneyimli bir Jung'cu terapistsin. Kullanıcının mesajını analiz et: {message}";
    }
}