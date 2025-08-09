//package com.example.techiedating.controller;
//
//import com.example.techiedating.TestDataUtil;
//import com.example.techiedating.dto.UserSkillDTO;
//import com.example.techiedating.dto.UserSkillRequest;
//import com.example.techiedating.model.Skill;
//import com.example.techiedating.model.User;
//import com.example.techiedating.model.UserSkill;
//import com.example.techiedating.repository.SkillRepository;
//import com.example.techiedating.repository.UserRepository;
//import com.example.techiedating.repository.UserSkillRepository;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.http.MediaType;
//import org.springframework.security.test.context.support.WithMockUser;
//import org.springframework.test.context.ActiveProfiles;
//import org.springframework.test.web.servlet.MockMvc;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.List;
//
//import static org.hamcrest.Matchers.*;
//import static org.junit.jupiter.api.Assertions.*;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
//
//@SpringBootTest
//@AutoConfigureMockMvc
//@ActiveProfiles("test")
//@Transactional
//class SkillControllerTest {
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @Autowired
//    private UserRepository userRepository;
//
//    @Autowired
//    private SkillRepository skillRepository;
//
//    @Autowired
//    private UserSkillRepository userSkillRepository;
//
//    private User testUser;
//    private Skill testSkill1;
//    private Skill testSkill2;
//    private UserSkill existingUserSkill;
//
//    @BeforeEach
//    void setUp() {
//        // Create test user
//        testUser = new User();
//        testUser.setUsername("testuser");
//        testUser.setEmail("test@example.com");
//        testUser.setPassword("password");
//        testUser = userRepository.save(testUser);
//
//        // Create test skills
//        testSkill1 = new Skill();
//        testSkill1.setName("Java");
//        testSkill1 = skillRepository.save(testSkill1);
//
//        testSkill2 = new Skill();
//        testSkill2.setName("Spring Boot");
//        testSkill2 = skillRepository.save(testSkill2);
//
//        // Add one skill to the user
//        existingUserSkill = new UserSkill();
//        existingUserSkill.setUser(testUser);
//        existingUserSkill.setSkill(testSkill1);
//        existingUserSkill.setLevel(3);
//        existingUserSkill = userSkillRepository.save(existingUserSkill);
//    }
//
//    @Test
//    void getAllSkills_shouldReturnAllSkills() throws Exception {
//        mockMvc.perform(get("/api/v1/skills"))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$", hasSize(2)))
//                .andExpect(jsonPath("$[0].name", is("Java")))
//                .andExpect(jsonPath("$[1].name", is("Spring Boot")));
//    }
//
//    @Test
//    @WithMockUser(username = "testuser")
//    void getUserSkills_shouldReturnUserSkills() throws Exception {
//        mockMvc.perform(get("/api/v1/skills/me"))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$", hasSize(1)))
//                .andExpect(jsonPath("$[0].skillId", is(testSkill1.getId())))
//                .andExpect(jsonPath("$[0].skillName", is("Java")))
//                .andExpect(jsonPath("$[0].level", is(3)));
//    }
//
//    @Test
//    @WithMockUser(username = "testuser")
//    void addSkillToUser_shouldAddSkill() throws Exception {
//        UserSkillRequest request = new UserSkillRequest();
//        request.setSkillId(testSkill2.getId());
//        request.setLevel(4);
//
//        mockMvc.perform(post("/api/v1/skills/me")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(TestDataUtil.asJsonString(request)))
//                .andExpect(status().isCreated())
//                .andExpect(jsonPath("$.skillId", is(testSkill2.getId())))
//                .andExpect(jsonPath("$.skillName", is("Spring Boot")))
//                .andExpect(jsonPath("$.level", is(4)));
//
//        // Verify the skill was added
//        List<UserSkill> userSkills = userSkillRepository.findByUserId(testUser.getId());
//        assertEquals(2, userSkills.size());
//        assertTrue(userSkills.stream().anyMatch(us ->
//            us.getSkill().getId().equals(testSkill2.getId()) && us.getLevel() == 4));
//    }
//
//    @Test
//    @WithMockUser(username = "testuser")
//    void updateUserSkill_shouldUpdateSkillLevel() throws Exception {
//        UserSkillRequest request = new UserSkillRequest();
//        request.setSkillId(testSkill1.getId());
//        request.setLevel(5);
//
//        mockMvc.perform(put("/api/v1/skills/me/" + existingUserSkill.getId())
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(TestDataUtil.asJsonString(request)))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.skillId", is(testSkill1.getId())))
//                .andExpect(jsonPath("$.level", is(5)));
//
//        // Verify the skill was updated
//        UserSkill updatedSkill = userSkillRepository.findById(existingUserSkill.getId())
//                .orElseThrow();
//        assertEquals(5, updatedSkill.getLevel());
//    }
//
//    @Test
//    @WithMockUser(username = "testuser")
//    void removeSkillFromUser_shouldRemoveSkill() throws Exception {
//        mockMvc.perform(delete("/api/v1/skills/me/" + existingUserSkill.getId()))
//                .andExpect(status().isNoContent());
//
//        // Verify the skill was removed
//        assertFalse(userSkillRepository.existsById(existingUserSkill.getId()));
//    }
//
//    @Test
//    @WithMockUser(username = "testuser")
//    void addDuplicateSkill_shouldReturnBadRequest() throws Exception {
//        UserSkillRequest request = new UserSkillRequest();
//        request.setSkillId(testSkill1.getId()); // Already exists for user
//        request.setLevel(3);
//
//        mockMvc.perform(post("/api/v1/skills/me")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(TestDataUtil.asJsonString(request)))
//                .andExpect(status().isBadRequest());
//    }
//
//    @Test
//    @WithMockUser(username = "testuser")
//    void updateNonExistentSkill_shouldReturnNotFound() throws Exception {
//        UserSkillRequest request = new UserSkillRequest();
//        request.setSkillId(testSkill1.getId());
//        request.setLevel(5);
//
//        mockMvc.perform(put("/api/v1/skills/me/9999")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(TestDataUtil.asJsonString(request)))
//                .andExpect(status().isNotFound());
//    }
//}
