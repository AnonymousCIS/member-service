package org.anonymous.member.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import org.anonymous.global.entities.MemberBaseEntity;
import org.anonymous.member.constants.DomainStatus;

@Data
@Entity
@IdClass(MemberStatusId.class) // MemberStatusId = 복합키
@JsonIgnoreProperties(ignoreUnknown=true)
public class MemberStatus extends MemberBaseEntity {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    private Member member;

    @Enumerated(EnumType.STRING)
    private DomainStatus memberStatus;

    @Id
    private String type;

    @Id
    private Long seq;
}
