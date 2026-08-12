package com.example.backend.repository.admin;

import com.example.backend.entity.ObservateurVideoAssignee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ObservateurVideoAssigneeRepository extends JpaRepository<ObservateurVideoAssignee, Long> {

    List<ObservateurVideoAssignee> findByObservateur_IdAndActifTrue(Integer idObservateur);

    boolean existsByObservateur_IdAndVideo_IdVideoAndActifTrue(Integer idObservateur, Integer idVideo);

    Optional<ObservateurVideoAssignee> findByObservateur_IdAndVideo_IdVideo(Integer idObservateur, Integer idVideo);

    List<ObservateurVideoAssignee> findByObservateur_Id(Integer idObservateur);

    List<ObservateurVideoAssignee> findByVideo_IdVideoAndActifTrue(Integer idVideo);
}