package com.miniquests.user.application;

import com.miniquests.user.domain.User;
import com.miniquests.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@Transactional
public class UserServiceIntegrationTest {
    @Autowired
    private UserService userService;
    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("유저 생성 후 생성된 정보를 조회하여 반환")
    void createsUserOnFirstCallAndReturnsExistingUserOnSecondCall() {
        String issuer = "https://issuer.example";
        String subject = UUID.randomUUID().toString();

        User first = userService.getOrCreate(
                "테스트 회원", issuer, subject, "test@example.com"
        );

        userRepository.flush();

        assertNotNull(first.getId());
        assertNotNull(first.getCreatedAt());
        assertNotNull(first.getUpdatedAt());

        User second = userService.getOrCreate(
                "다른 이름", issuer, subject, "other@example.com"
        );

        assertEquals(first.getId(), second.getId());
        assertEquals("테스트 회원", second.getDisplayName());
        assertEquals("test@example.com", second.getEmail());
    }
}
