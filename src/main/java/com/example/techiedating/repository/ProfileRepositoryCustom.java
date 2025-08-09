package com.example.techiedating.repository;

import com.example.techiedating.model.UserProfile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProfileRepositoryCustom {
    Page<UserProfile> searchProfiles(
            String currentUserId,
            String query,
            String gender,
            Integer minExperience,
            Integer maxExperience,
            Pageable pageable
    );
}
