package com.myservers.backend.servers.repositories;


import com.myservers.backend.servers.entities.Code;
import com.myservers.backend.servers.entities.CodeState;
import com.myservers.backend.servers.entities.Server;
import org.springframework.data.geo.GeoResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface CodeRepository extends JpaRepository<Code, Long> {

//    @Query("select c.price,c.subscription_duration,COUNT(c.id) from Code c  GROUP BY c.subscription_duration")
    List<Code> findByOriginServerId(Long id);

    List<Code> findByOriginServer_Id(Long id);

    Code findFirstByOriginServer_IdAndSubscriptionDurationAndPriceOrderByDateCreationAsc(Long id, Integer subscription_duration, Float price);

    List<Code> findByOriginServer_IdAndState(Long id, CodeState state);

    Optional<Code> findByIdAndState(Long id, CodeState state);

    @Transactional
    @Modifying
    @Query("update Code c set c.state = ?1 where c.state not in ?2 and c.id = ?3 and c.originServer = ?4")
    int updateStateByStateNotInAndIdAndOriginServer(CodeState state, Collection<CodeState> states, Long id, Server originServer);

    List<Code> findByOriginServer_IdAndStateIn(Long id, Collection<CodeState> states);



    List<Code> findByStateNotAndOriginServer_State(CodeState state, boolean state1);

    long countByStateNotAndOriginServer_StateAndDateCreationLessThanEqual(CodeState state, boolean state1, Date dateCreation);


}
