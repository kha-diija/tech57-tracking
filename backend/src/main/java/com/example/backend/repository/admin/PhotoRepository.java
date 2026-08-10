package com.example.backend.repository.admin;

import com.example.backend.entity.Photo;
import com.example.backend.entity.Intervention;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PhotoRepository extends JpaRepository<Photo, Integer> {
    List<Photo> findByIntervention(Intervention intervention);
    void deleteByIntervention(Intervention intervention);
}