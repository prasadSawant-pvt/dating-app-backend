package com.example.techiedating.service;

import com.example.techiedating.dto.UserSkillDTO;
import com.example.techiedating.dto.UserSkillRequest;
import com.example.techiedating.exception.ResourceNotFoundException;
import com.example.techiedating.mapper.UserSkillMapper;
import com.example.techiedating.model.Skill;
import com.example.techiedating.model.User;
import com.example.techiedating.model.UserSkill;
import com.example.techiedating.repository.SkillRepository;
import com.example.techiedating.repository.UserRepository;
import com.example.techiedating.repository.UserSkillRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserSkillService {

    private final UserSkillRepository userSkillRepository;
    private final UserRepository userRepository;
    private final SkillRepository skillRepository;
    private final UserSkillMapper userSkillMapper;

    @Transactional(readOnly = true)
    public List<UserSkillDTO> getUserSkills(String username) {
        log.info("Fetching skills for user: {}", username);
        return userSkillRepository.findByUserId(getUserId(username)).stream()
                .map(userSkillMapper::toUserSkillDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public UserSkillDTO addSkillToUser(String username, UserSkillRequest request) {
        log.info("Adding skill {} to user: {}", request.getSkillId(), username);
        String userId = getUserId(username);
        
        // Check if the skill exists
        Skill skill = skillRepository.findById(request.getSkillId())
                .orElseThrow(() -> new ResourceNotFoundException("Skill not found with id: " + request.getSkillId()));
        
        // Check if the user already has this skill
        userSkillRepository.findByUserIdAndSkillId(userId, request.getSkillId())
                .ifPresent(us -> {
                    throw new IllegalStateException("User already has this skill");
                });
        
        // Create and save the user skill
        UserSkill userSkill = UserSkill.builder()
                .user(User.builder().id(userId).build())
                .skill(skill)
                .level(request.getLevel())
                .build();
        
        UserSkill savedUserSkill = userSkillRepository.save(userSkill);
        return userSkillMapper.toUserSkillDTO(savedUserSkill);
    }

    @Transactional
    public UserSkillDTO updateUserSkill(String username, Long userSkillId, UserSkillRequest request) {
        log.info("Updating skill {} for user: {}", userSkillId, username);
        String userId = getUserId(username);
        
        // Find the existing user skill
        UserSkill userSkill = userSkillRepository.findByIdAndUserId(userSkillId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("User skill not found with id: " + userSkillId));
        
        // Update the skill level
        userSkill.setLevel(request.getLevel());
        
        UserSkill updatedUserSkill = userSkillRepository.save(userSkill);
        return userSkillMapper.toUserSkillDTO(updatedUserSkill);
    }

    @Transactional
    public void removeSkillFromUser(String username, Long userSkillId) {
        log.info("Removing skill {} from user: {}", userSkillId, username);
        String userId = getUserId(username);
        
        // Check if the user has this skill
        UserSkill userSkill = userSkillRepository.findByIdAndUserId(userSkillId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("User skill not found with id: " + userSkillId));
        
        userSkillRepository.delete(userSkill);
    }

    private String getUserId(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username))
                .getId();
    }
}
