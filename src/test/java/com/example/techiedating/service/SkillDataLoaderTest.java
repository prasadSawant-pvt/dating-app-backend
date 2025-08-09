//package com.example.techiedating.service;
//
//import com.example.techiedating.model.Skill;
//import com.example.techiedating.model.SkillCategory;
//import com.example.techiedating.repository.SkillRepository;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.context.ActiveProfiles;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.List;
//import java.util.Optional;
//
//import static org.assertj.core.api.Assertions.assertThat;
//
//@SpringBootTest
//@ActiveProfiles("test")
//@Transactional
//class SkillDataLoaderTest {
//
//    @Autowired
//    private SkillDataLoader skillDataLoader;
//
//    @Autowired
//    private SkillRepository skillRepository;
//
//    @BeforeEach
//    void setUp() {
//        // Ensure clean state for each test
//        skillRepository.deleteAll();
//    }
//
//    @Test
//    void loadSkills_shouldLoadSkillsFromJsonFile() {
//        // When
//        skillDataLoader.loadSkills();
//
//        // Then
//        List<Skill> skills = skillRepository.findAll();
//        assertThat(skills).isNotEmpty();
//
//        // Verify some expected skills were loaded
//        assertThat(skills.stream().map(Skill::getName))
//                .contains("Java", "Python", "JavaScript", "Spring Boot", "React");
//
//        // Verify categories were set correctly
//        Optional<Skill> javaSkill = skillRepository.findByName("Java");
//        assertThat(javaSkill).isPresent();
//        assertThat(javaSkill.get().getCategory()).isEqualTo(SkillCategory.PROGRAMMING_LANGUAGES);
//
//        // Verify slugs were generated
//        assertThat(javaSkill.get().getSlug()).isEqualTo("java");
//    }
//
//    @Test
//    void loadSkills_shouldNotDuplicateExistingSkills() {
//        // Given - manually insert a skill that's also in the JSON file
//        Skill existingSkill = new Skill();
//        existingSkill.setName("Java");
//        existingSkill.setCategory(SkillCategory.PROGRAMMING_LANGUAGES);
//        existingSkill.setDescription("Existing Java skill");
//        skillRepository.save(existingSkill);
//
//        // When
//        skillDataLoader.loadSkills();
//
//        // Then - should only have one Java skill
//        List<Skill> javaSkills = skillRepository.findByName("Java");
//        assertThat(javaSkills).hasSize(1);
//        assertThat(javaSkills.get(0).getDescription()).isEqualTo("Existing Java skill");
//    }
//
//    @Test
//    void loadSkills_shouldSkipIfSkillsExist() {
//        // Given - insert some skills
//        Skill skill1 = new Skill();
//        skill1.setName("Existing Skill 1");
//        skill1.setCategory(SkillCategory.OTHER);
//        skillRepository.save(skill1);
//
//        // When
//        skillDataLoader.loadSkills();
//
//        // Then - should not have loaded any new skills
//        List<Skill> allSkills = skillRepository.findAll();
//        assertThat(allSkills).hasSize(1);
//        assertThat(allSkills.get(0).getName()).isEqualTo("Existing Skill 1");
//    }
//}
