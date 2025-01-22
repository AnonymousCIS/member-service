package org.anonymous.member.repositories;

import org.anonymous.member.entities.MemberStatus;
import org.anonymous.member.entities.MemberStatusId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

public interface MemberStatusRepository extends JpaRepository<MemberStatus, MemberStatusId>, QuerydslPredicateExecutor<MemberStatus> {
}
