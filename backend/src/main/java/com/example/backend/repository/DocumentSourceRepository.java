package com.example.backend.repository;

import com.example.backend.entity.DocumentSource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DocumentSourceRepository extends JpaRepository<DocumentSource, Integer> {
}