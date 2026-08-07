package com.example.backend.repository.gestionuser;

import com.example.backend.entity.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository("gsuserRepository")
public interface gsuserRepository extends JpaRepository<Utilisateur, Integer> {

    Optional<Utilisateur> findByEmail(String email);

    boolean existsByEmail(String email);
}
