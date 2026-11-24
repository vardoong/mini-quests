package com.miniquests.user.application;

import com.miniquests.user.domain.User;
import com.miniquests.user.domain.UserStatus;
import com.miniquests.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public User getOrCreate(
            String displayName,
            String oidcIssuer,
            String oidcSubject,
            String email
    ) {
        Optional<User> userOptional = userRepository.findByOidcIssuerAndOidcSubject(oidcIssuer, oidcSubject);

        // userOptional 있으면 회원정보 반환
        if (userOptional.isPresent()) {
            return userOptional.get();
        }

        // userOptional 없으면 생성하고 반환
        User user = User.builder()
                .displayName(displayName)
                .oidcIssuer(oidcIssuer)
                .oidcSubject(oidcSubject)
                .email(email)
                .status(UserStatus.ACTIVE)
                .build();

        return userRepository.save(user);
    }

}
