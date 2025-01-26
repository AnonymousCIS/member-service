package org.anonymous.member.entities;

import jakarta.persistence.*;
import lombok.Data;
import org.anonymous.global.entities.MemberBaseEntity;
import org.anonymous.member.constants.MemberDomainStatus;

@Data
@Entity
@IdClass(MemberStatusId.class) // MemberStatusId = 복합키
public class MemberStatus extends MemberBaseEntity {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    private Member member;

    @Enumerated(EnumType.STRING)
    private MemberDomainStatus memberStatus;

    @Id
    private String type;

    @Id
    private Long seq;
}
