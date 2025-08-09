package com.example.techiedating.service;

import com.example.techiedating.dto.SkillData;
import com.example.techiedating.model.Skill;
import com.example.techiedating.model.SkillCategory;
import com.example.techiedating.repository.SkillRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SkillDataLoader {

    private static final String SKILLS_JSON_PATH = "data/skills.json";
    private final SkillRepository skillRepository;
    private final ObjectMapper objectMapper;

    @PostConstruct
    @Transactional
    public void loadSkills() {
        if (skillRepository.count() > 0) {
            log.info("Skills already loaded, skipping data loading");
            return;
        }

        try {
            log.info("Loading skills from JSON file: {}", SKILLS_JSON_PATH);
            List<SkillData> skillsData = loadSkillsFromJson();
            
            // Convert to entities and save only if they don't exist
            List<Skill> newSkills = skillsData.stream()
                    .filter(skillData -> !skillRepository.existsByName(skillData.getName()))
                    .map(this::mapToSkill)
                    .collect(Collectors.toList());
            
            if (!newSkills.isEmpty()) {
                skillRepository.saveAll(newSkills);
                log.info("Successfully loaded {} skills into the database", newSkills.size());
            } else {
                log.info("No new skills to load");
            }
            
        } catch (IOException e) {
            log.error("Failed to load skills from JSON file: {}", SKILLS_JSON_PATH, e);
        }
    }

    @SuppressWarnings("unchecked")
    private List<SkillData> loadSkillsFromJson() throws IOException {
        ClassPathResource resource = new ClassPathResource(SKILLS_JSON_PATH);
        try (InputStream inputStream = resource.getInputStream()) {
            Map<String, List<SkillData>> data = objectMapper.readValue(
                    inputStream,
                    new TypeReference<Map<String, List<SkillData>>>() {}
            );
            return data.get("skills");
        }
    }

    private Skill mapToSkill(SkillData skillData) {
        return Skill.builder()
                .name(skillData.getName())
                .category(skillData.getCategory())
                .description(skillData.getDescription())
                .build();
    }
}
