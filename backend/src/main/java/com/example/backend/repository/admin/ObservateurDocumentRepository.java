package com.example.backend.repository.admin;

import com.example.backend.entity.ObservateurDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ObservateurDocumentRepository extends JpaRepository<ObservateurDocument, Long> {

    List<ObservateurDocument> findByObservateur_IdAndActifTrue(Integer idObservateur);

    boolean existsByObservateur_IdAndDocument_IdSourceAndActifTrue(Integer idObservateur, Integer idSource);

    Optional<ObservateurDocument> findByObservateur_IdAndDocument_IdSource(Integer idObservateur, Integer idSource);

    List<ObservateurDocument> findByObservateur_Id(Integer idObservateur);

    List<ObservateurDocument> findByDocument_IdSourceAndActifTrue(Integer idSource);

    Optional<ObservateurDocument> findByObservateur_IdAndDocument_IdSourceAndActifTrue(
            Integer idObservateur, Integer idSource);
}