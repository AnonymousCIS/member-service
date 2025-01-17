package org.anonymous.member.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;
import org.anonymous.global.entities.BaseEntity;
import org.anonymous.member.constants.Authority;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 회원 (Member) Entity
 *
 */
@Data
@Entity
public class Member extends BaseEntity {

    @Id @GeneratedValue
    private Long seq; // 회원 번호

    @Column(length = 65, nullable = false, unique = true)
    private String email; // 이메일 (로그인 ID)

    @Column(length = 65, nullable = false)
    private String password;

    @Column(length = 40, nullable = false)
    private String name;

    private boolean requiredTerms1; // 필수 약관

    private boolean requiredTerms2;

    private boolean requiredTerms3;

    @Column(length = 50)
    private String optionalTerms; // 선택 약관

    @JsonIgnore // 순환 참조 발생 방지용
    @ToString.Exclude
    @OneToMany(mappedBy = "member", cascade = CascadeType.REMOVE)
    // 관계의 주인은 Many 쪽인 Authorities_member
    private List<Authorities> authorities; // 회원쪽에서도 권한 조회 가능하도록

    // 비밀번호 변경 일시
    private LocalDateTime credentialChangedAt;

    // 자기소개
    @Lob
    private String bio;

    public List<Authority> get_authorities() {
        return authorities == null || authorities.isEmpty() ? List.of()
                : authorities.stream().map(Authorities::getAuthority).toList();
    }
}