package org.anonymous.member.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.anonymous.member.constants.TokenAction;

import java.time.LocalDateTime;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TempToken {
    @Id
    @Column(length = 45)
    private String token;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private TokenAction action;

    @ManyToOne(fetch = FetchType.LAZY)
    private Member member;

    private LocalDateTime expiredTime;

    @Column(length = 65, nullable = false)
    private String origin;

    /**
     * 토큰 만료 여부
     * @return
     */
    public boolean isExpired() {
        return expiredTime == null && expiredTime.isBefore(LocalDateTime.now());
    }
}

























