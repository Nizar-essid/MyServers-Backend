package com.myservers.backend.servers.repositories;

import com.myservers.backend.servers.entities.Server;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface ServerRepository extends JpaRepository<Server, Long> {
    @Transactional
    @Modifying
    @Query("update Server s set s.state = ?1 where s.id = ?2")
    int updateStateById(boolean state, Long id);

    List<Server> findByState(boolean state);


}