package com.example.techiedating.config;

import com.example.techiedating.model.*;
import com.example.techiedating.repository.PhotoRepository;
import com.example.techiedating.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashSet;

@Configuration
@Profile("dev")
@RequiredArgsConstructor
public class DataInitializer {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PhotoRepository photoRepository;

    @Bean
    @Transactional
    public CommandLineRunner initDatabase() {
        return args -> {
            // First delete all photos to avoid foreign key constraint violations
            photoRepository.deleteAll();
            // Then delete all users
            userRepository.deleteAll();

            createUserWithProfile("alice@example.com", "Alice", "Smith", passwordEncoder.encode("password123"),
                    "Alice Smith", "I'm a software engineer who loves hiking and photography.",
                    LocalDate.of(1990, 5, 15), 37.7749, -122.4194, Gender.FEMALE);

            createUserWithProfile("bob@example.com", "Bob", "Johnson", passwordEncoder.encode("password123"),
                    "Bob Johnson", "Tech lead with a passion for AI and machine learning. Love traveling and trying new foods.",
                    LocalDate.of(1988, 8, 22), 40.7128, -74.0060, Gender.MALE);

            createUserWithProfile("charlie@example.com", "Charlie", "Brown", passwordEncoder.encode("password123"),
                    "Charlie Brown", "Full-stack developer by day, amateur chef by night. Let's cook something amazing together!",
                    LocalDate.of(1992, 3, 10), 41.8781, -87.6298, Gender.OTHER);

            System.out.println("Test data initialized successfully!");
        };
    }

    @Transactional
    protected void createUserWithProfile(String email, String firstName, String lastName, String encodedPassword,
                                         String displayName, String bio, LocalDate dateOfBirth,
                                         double latitude, double longitude, Gender gender) {

        User user = createUser(email, firstName, lastName, encodedPassword);

        // Save user first to get the ID
        user = userRepository.save(user);

        // Create profile with the same ID as the user
        UserProfile profile = UserProfile.builder()
                .id(user.getId())  // Set the ID to match the user's ID
                .displayName(displayName)
                .bio(bio)
                .dateOfBirth(dateOfBirth)
                .latitude(latitude)
                .longitude(longitude)
                .gender(gender)
                .experienceYrs(5)
                .interests(new HashSet<>(Arrays.asList("Hiking", "Photography", "Traveling")))
                .user(user)
                .build();

        user.setUserProfile(profile);
        userRepository.save(user);  // Save again with profile
    }

    private User createUser(String email, String firstName, String lastName, String encodedPassword) {
        String username = email.split("@")[0];
        return User.builder()
                .email(email)
                .username(username)
                .firstName(firstName)
                .lastName(lastName)
                .password(encodedPassword)
                .active(true)
                .build();
    }
}
