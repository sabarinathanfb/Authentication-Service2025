package com.link360.authenticationservice2025.repositories;

import com.link360.authenticationservice2025.models.Session;
import org.springframework.data.jpa.repository.JpaRepository;


public interface SessionRepository extends JpaRepository<Session, Long> {

    Session save(Session session);

}
