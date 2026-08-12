package com.example.backend.repository;

import com.example.backend.entity.VideoMateriel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VideoMaterielRepository extends JpaRepository<VideoMateriel, Integer> {
}