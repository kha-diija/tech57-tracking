package com.example.backend.repository.admin;

import com.example.backend.entity.ObservateurResourceAssignee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ObservateurResourceAssigneeRepository extends JpaRepository<ObservateurResourceAssignee, Long> {

    List<ObservateurResourceAssignee> findByObservateur_IdAndActifTrue(Integer idObservateur);

    boolean existsByObservateur_IdAndRessource_IdRessourceAndActifTrue(Integer idObservateur, Integer idRessource);

    Optional<ObservateurResourceAssignee> findByObservateur_IdAndRessource_IdRessource(Integer idObservateur, Integer idRessource);

    List<ObservateurResourceAssignee> findByObservateur_Id(Integer idObservateur);

    List<ObservateurResourceAssignee> findByRessource_IdRessourceAndActifTrue(Integer idRessource);
}