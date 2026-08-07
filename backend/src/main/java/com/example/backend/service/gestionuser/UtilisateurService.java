package com.example.backend.service.gestionuser;

import com.example.backend.dto.gestionuser.UserRequestDto;
import com.example.backend.dto.gestionuser.UserResponseDto;
import org.springframework.security.core.Authentication;

import java.util.List;

public interface UtilisateurService {
    List<UserResponseDto> getAllUsers();

    UserResponseDto createUser(UserRequestDto dto);

    UserResponseDto toggleStatus(Integer id, Authentication currentUser);

    void deleteUser(Integer id, Authentication currentUser);
}
