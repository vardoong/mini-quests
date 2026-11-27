package com.miniquests.quest.domain;

import com.miniquests.common.error.BusinessException;
import com.miniquests.quest.error.QuestErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "user_quests")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class UserQuest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, updatable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "quest_id", nullable = false, updatable = false)
    private Quest quest;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private UserQuestStatus status;

    @Column(name = "started_at", nullable = false, updatable = false)
    @CreatedDate
    private Instant startedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Builder
    private UserQuest(
            Long userId,
            Quest quest
    ) {
        this.userId = userId;
        this.quest = quest;
        this.status = UserQuestStatus.IN_PROGRESS;
    }

    public void complete(Instant completedAt) {
        if (status == UserQuestStatus.COMPLETED) {
            throw new BusinessException(QuestErrorCode.QUEST_ALREADY_COMPLETED);
        }

        this.completedAt = Objects.requireNonNull(completedAt);
        this.status = UserQuestStatus.COMPLETED;
    }
}
