package com.miniquests.user.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "display_name", length = 50, nullable = false)
    private String displayName;

    @Column(columnDefinition = "text", nullable = false)
    private String oidcIssuer;

    @Column(name = "oidc_subject", length = 255, nullable = false)
    private String oidcSubject;

    @Column(name = "email", length = 255, nullable = true)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private UserStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    @CreatedDate
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    @LastModifiedDate
    private Instant updatedAt;

    @Builder
    private User(
            String displayName,
            String oidcIssuer,
            String oidcSubject,
            String email,
            UserStatus status
    ) {
        this.displayName = displayName;
        this.oidcIssuer = oidcIssuer;
        this.oidcSubject = oidcSubject;
        this.email = email;
        this.status = status;
    }
}
