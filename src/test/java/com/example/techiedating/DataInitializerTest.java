package com.example.techiedating;

import com.example.techiedating.config.TestConfig;
import com.example.techiedating.model.Gender;
import com.example.techiedating.model.User;
import com.example.techiedating.model.UserProfile;
import com.example.techiedating.repository.UserProfileRepository;
import com.example.techiedating.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(TestConfig.class)
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE;MODE=PostgreSQL",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
    "spring.jpa.hibernate.ddl-auto=none",
    "spring.jpa.show-sql=true",
    "spring.jpa.properties.hibernate.format_sql=true",
    "spring.flyway.enabled=false",
    "spring.autoconfigure.exclude=com.google.cloud.spring.autoconfigure.sql.CloudSqlAutoConfiguration"
})
@Transactional
public class DataInitializerTest {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @BeforeEach
    void setUp() {
        // Clear existing data
        userProfileRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
        
        // Create test user
        User user = new User();
        user.setId("test-user-1");
        user.setEmail("alice@example.com");
        user.setFirstName("Alice");
        user.setLastName("Smith");
        user.setPassword("$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.AQubh4a"); // password: password
        user.setActive(true);
        user = userRepository.saveAndFlush(user);
        
        // Create test user profile
        UserProfile profile = new UserProfile();
        profile.setId(user.getId());
        profile.setUserId(user.getId());
        profile.setDisplayName("Alice Smith");
        profile.setBio("Software developer who loves hiking and photography");
        profile.setDateOfBirth(LocalDate.of(1990, 5, 15));
        profile.setGender(Gender.FEMALE);
        profile.setLatitude(37.7749);
        profile.setLongitude(-122.4194);
        profile.setInterests(Set.of("Hiking", "Photography", "Coding"));
        
        // Set the bidirectional relationship
        user.setUserProfile(profile);
        profile.setUser(user);
        
        userProfileRepository.saveAndFlush(profile);
    }

    @Test
    public void testDatabaseConnection() {
        // Verify database connection is working
        assertThat(dataSource).isNotNull();
        assertThat(userRepository).isNotNull();
        assertThat(userProfileRepository).isNotNull();
        
        // Verify we can query the database
        assertThat(userRepository.count()).isGreaterThan(0);
        assertThat(userProfileRepository.count()).isGreaterThan(0);
    }

    @Test
    void testDataInitialization() {
        // Test user was created
        Optional<User> userOpt = userRepository.findByEmail("alice@example.com");
        assertThat(userOpt).isPresent();
        
        User alice = userOpt.get();
        assertThat(alice.getEmail()).isEqualTo("alice@example.com");
        assertThat(alice.getFirstName()).isEqualTo("Alice");
        assertThat(alice.getLastName()).isEqualTo("Smith");
        
        // Test user profile was created
        Optional<UserProfile> profileOpt = userProfileRepository.findByUserId(alice.getId());
        assertThat(profileOpt).isPresent();
        
        UserProfile aliceProfile = profileOpt.get();
                
        // Verify profile data
        assertThat(aliceProfile.getDisplayName()).isEqualTo("Alice Smith");
        assertThat(aliceProfile.getBio()).isNotNull().contains("Software developer");
        assertThat(aliceProfile.getGender()).isEqualTo(Gender.FEMALE);
        assertThat(aliceProfile.getDateOfBirth()).isEqualTo(LocalDate.of(1990, 5, 15));
        assertThat(aliceProfile.getInterests()).contains("Hiking", "Photography", "Coding");
        
        // Test the relationship
        assertThat(alice.getUserProfile()).isNotNull();
        assertThat(alice.getUserProfile().getId()).isEqualTo(aliceProfile.getId());
        assertThat(aliceProfile.getUser()).isNotNull();
        assertThat(aliceProfile.getUser().getId()).isEqualTo(alice.getId());
    }

    @Test
    public void testProfileData() {
        // Find profile directly by user email
        Optional<UserProfile> profileOpt = userProfileRepository.findByUserEmail("alice@example.com");
        assertThat(profileOpt).isPresent();
        
        UserProfile profile = profileOpt.get();
        assertThat(profile).isNotNull();
        
        assertThat(profile.getBio()).contains("software engineer");
        assertThat(profile.getDateOfBirth()).isBefore(LocalDate.now().minusYears(18));
        assertThat(profile.getInterests()).isNotEmpty();
        assertThat(profile.getGender()).isIn(Gender.values());
        assertThat(profile.getDisplayName()).isEqualTo("Alice Smith");
    }
}
