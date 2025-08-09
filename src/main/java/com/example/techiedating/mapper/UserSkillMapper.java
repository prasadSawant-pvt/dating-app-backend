package com.example.techiedating.mapper;

import com.example.techiedating.dto.UserSkillDTO;
import com.example.techiedating.model.UserSkill;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface UserSkillMapper {
    UserSkillMapper INSTANCE = Mappers.getMapper(UserSkillMapper.class);
    
    @Mapping(target = "skillId", source = "skill.id")
    @Mapping(target = "skillName", source = "skill.name")
    UserSkillDTO toUserSkillDTO(UserSkill userSkill);
}
