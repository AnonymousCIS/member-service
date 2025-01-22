package org.anonymous.member.entities;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class MemberStatusId {
    private Member member;
    private String type;
    private Long seq;
}
