package org.anonymous.member.repositories;

import org.anonymous.member.entities.Member;
import org.anonymous.member.entities.QMember;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long>, QuerydslPredicateExecutor<Member> {

    // Query Method
    // 권한(Authority)은 @OneToMany 라서 지연 로딩 상태지만
    // finByEmail 메서드 사용시에는 즉시 로딩되도록 fetch Join
    @EntityGraph("authorities")
    Optional<Member> findByEmail(String email);

    Optional<Member> findByNameAndPhoneNumber(String name, String Mobile);

    Optional<Member> findByPhoneNumber(String mobile);

    default boolean phoneNumberExists(String phoneNumber) {
        QMember member = QMember.member;

        return exists(member.phoneNumber.eq(phoneNumber));
    }

    default boolean exists(String email) {

        QMember member = QMember.member;

        return exists(member.email.eq(email));
    }
}