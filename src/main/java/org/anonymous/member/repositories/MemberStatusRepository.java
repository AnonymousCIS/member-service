package org.anonymous.member.repositories;

import com.querydsl.core.types.dsl.BooleanExpression;
import org.anonymous.member.entities.MemberStatus;
import org.anonymous.member.entities.MemberStatusId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

import java.util.Optional;

public interface MemberStatusRepository extends JpaRepository<MemberStatus, MemberStatusId>, QuerydslPredicateExecutor<MemberStatus> {

}
