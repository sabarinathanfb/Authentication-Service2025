package com.link360.authenticationservice2025.repositories;

import com.link360.authenticationservice2025.models.Session;
import com.link360.authenticationservice2025.models.SessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface SessionRepository extends JpaRepository<Session, Long> {

    Session save(Session session);
    Optional<Session> findByUserId(Long userId);

}
