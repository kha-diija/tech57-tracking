package com.example.backend.service.gestionuser;

import com.example.backend.dto.gestionuser.UserRequestDto;
import com.example.backend.dto.gestionuser.UserResponseDto;
import com.example.backend.entity.*; // Utilisateur, Administrateur, Technicien, Observateur, GestionnaireStock
import com.example.backend.repository.UtilisateurRepository;
import com.example.backend.service.gestionuser.UtilisateurService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UtilisateurServiceImpl implements UtilisateurService {

    private final UtilisateurRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UtilisateurServiceImpl(UtilisateurRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponseDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public UserResponseDto createUser(UserRequestDto dto) {
        // 1. Vérification de l'unicité de l'email
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Un utilisateur avec cet email existe déjà : " + dto.getEmail());
        }

        // 2. Instanciation de la sous-classe concrète selon le type d'utilisateur
        Utilisateur user = creerInstanceSelonRole(dto.getTypeUtilisateur());

        // 3. Remplissage des champs communs
        user.setNom(dto.getNom().trim());
        user.setPrenom(dto.getPrenom().trim());
        user.setEmail(dto.getEmail().trim().toLowerCase());
        user.setTelephone(dto.getTelephone());

        // Hachage du mot de passe (la présence est déjà garantie par @NotBlank sur le DTO,
        // mais on garde une double vérification défensive)
        if (dto.getMotDePasse() == null || dto.getMotDePasse().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le mot de passe initial est obligatoire.");
        }
        user.setMotDePasse(passwordEncoder.encode(dto.getMotDePasse()));

        user.setCompteActif(true);
        user.setTentativesEchouees((short) 0);
        // dateCreation est gérée automatiquement par @PrePersist dans Utilisateur

        // 4. Sauvegarde BDD et retour du DTO
        Utilisateur savedUser = userRepository.save(user);
        return mapToResponse(savedUser);
    }

    @Override
    @Transactional
    public UserResponseDto toggleStatus(Integer id, Authentication currentUser) {
        Utilisateur user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur non trouvé"));

        if ("ADMINISTRATEUR".equalsIgnoreCase(user.getTypeUtilisateur())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Impossible de bloquer un administrateur");
        }
        if (isSelf(user, currentUser)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Vous ne pouvez pas bloquer votre propre compte");
        }

        boolean nouveauStatut = !user.isCompteActif();
        user.setCompteActif(nouveauStatut);

        if (nouveauStatut) {
            user.setTentativesEchouees((short) 0);
            user.setCompteVerrouilleJusqu(null);
        }

        return mapToResponse(userRepository.save(user));
    }

    @Override
    @Transactional
    public void deleteUser(Integer id, Authentication currentUser) {
        Utilisateur user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur non trouvé"));

        if ("ADMINISTRATEUR".equalsIgnoreCase(user.getTypeUtilisateur())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Impossible de supprimer un administrateur");
        }
        if (isSelf(user, currentUser)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Vous ne pouvez pas supprimer votre propre compte");
        }

        try {
            userRepository.delete(user);
            userRepository.flush();
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Impossible de supprimer cet utilisateur : il est lié à d'autres données (missions, sorties de matériel, etc.). Bloquez-le plutôt.");
        }
    }

    // ----- Helpers -----

    private boolean isSelf(Utilisateur target, Authentication currentUser) {
        if (currentUser == null || currentUser.getName() == null) {
            return false;
        }
        // Hypothèse : le "username" Spring Security = l'email (cas classique en JWT).
        return target.getEmail() != null && target.getEmail().equalsIgnoreCase(currentUser.getName());
    }

    /**
     * Fabrique pour instancier la sous-classe de Utilisateur correspondant au DiscriminatorValue
     */
    private Utilisateur creerInstanceSelonRole(String typeUtilisateur) {
        if (typeUtilisateur == null || typeUtilisateur.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le type d'utilisateur ne peut pas être vide.");
        }

        return switch (typeUtilisateur.toUpperCase()) {
            case "ADMINISTRATEUR" -> new Administrateur();
            case "TECHNICIEN" -> new Technicien();
            case "OBSERVATEUR" -> new Observateur();
            case "GESTIONNAIRE_STOCK" -> new GestionnaireStock();
            default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Type d'utilisateur inconnu : " + typeUtilisateur);
        };
    }

    /**
     * Conversion d'une entité Utilisateur vers UserResponseDto
     */
    private UserResponseDto mapToResponse(Utilisateur u) {
        UserResponseDto dto = new UserResponseDto();
        dto.setId(u.getId());
        dto.setPrenom(u.getPrenom());
        dto.setNom(u.getNom());
        dto.setEmail(u.getEmail());
        dto.setTelephone(u.getTelephone());
        dto.setTypeUtilisateur(u.getTypeUtilisateur()); // méthode @Transient -> lit le @DiscriminatorValue
        dto.setCompteActif(u.isCompteActif());
        dto.setDateCreation(u.getDateCreation());
        return dto;
    }
}
