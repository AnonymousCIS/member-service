package org.anonymous.member.repositories;

import org.anonymous.member.entities.TempToken;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

import java.util.Optional;

public interface TempTokenRepository extends JpaRepository<TempToken, String>, QuerydslPredicateExecutor<TempToken> {

    // fetch Join 하기 위함.
    @EntityGraph(attributePaths = "member")
    Optional<TempToken> findByToken(String token);
}
