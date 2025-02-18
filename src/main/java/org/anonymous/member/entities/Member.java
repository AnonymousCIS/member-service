package org.anonymous.member.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;
import org.anonymous.global.entities.BaseEntity;
import org.anonymous.member.constants.Authority;
import org.anonymous.member.constants.Gender;
import org.anonymous.member.constants.MemberCondition;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 회원 (Member) Entity
 *
 */
@Data
@Entity
@JsonIgnoreProperties(ignoreUnknown=true)
public class Member extends BaseEntity {

    @Id @GeneratedValue
    private Long seq; // 회원 번호

    @Column(length = 40, nullable = false)
    private String name;

    @Column(length = 65, nullable = false, unique = true)
    private String email; // 이메일 (로그인 ID)

    @Column(length = 65, nullable = false)
    private String password;

    @Column(length = 10, nullable = false)
    private String zipCode;

    @Column(length = 100, nullable = false)
    private String address;

    @Column(length = 100)
    private String addressSub;

    @Column(length=20, nullable = false)
    private String phoneNumber;

    @Column(length = 50)
    private String optionalTerms; // 선택 약관

    @Column(length = 10, nullable = false)
    @Enumerated(EnumType.STRING)
    private Gender gender;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Column(nullable = false)
    private LocalDate birthDt;

    @Enumerated(EnumType.STRING)
    private MemberCondition memberCondition;

    @JsonIgnore // 순환 참조 발생 방지용
    @ToString.Exclude
    @OneToMany(mappedBy = "member", cascade = CascadeType.REMOVE)
    // 관계의 주인은 Many 쪽인 Authorities_member
    private List<Authorities> authorities; // 회원쪽에서도 권한 조회 가능하도록

    // 비밀번호 변경 일시
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime credentialChangedAt;

    // 자기소개
    @Lob
    private String bio;

    @JsonIgnore
    public List<Authority> get_authorities() {
        return authorities == null || authorities.isEmpty() ? List.of()
                : authorities.stream().map(Authorities::getAuthority).toList();
    }
}