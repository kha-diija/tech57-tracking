package com.example.backend.repository.admin;

import com.example.backend.entity.Province;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProvinceRepository extends JpaRepository<Province, Integer> {
    List<Province> findByRegion_IdRegion(Integer idRegion);
}