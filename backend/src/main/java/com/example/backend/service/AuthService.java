package com.example.backend.service;

import com.example.backend.dto.auth.LoginRequest;
import com.example.backend.dto.auth.LoginResponse;
import com.example.backend.entity.PasswordResetToken;
import com.example.backend.entity.RefreshToken;
import com.example.backend.entity.Utilisateur;
import com.example.backend.exception.AccountDisabledException;
import com.example.backend.exception.AccountLockedException;
import com.example.backend.exception.InvalidCredentialsException;
import com.example.backend.exception.InvalidRefreshTokenException;
import com.example.backend.exception.InvalidResetTokenException;
import com.example.backend.repository.PasswordResetTokenRepository;
import com.example.backend.repository.RefreshTokenRepository;
import com.example.backend.repository.UtilisateurRepository;
import com.example.backend.security.JwtTokenProvider;
import com.example.backend.security.UserPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Locale;
import java.util.UUID;

@Service
public class AuthService {

    private static final short MAX_TENTATIVES = 5;
    private static final long LOCK_DURATION_MINUTES = 15;
    private static final long RESET_TOKEN_VALIDITY_MINUTES = 30;

    private final UtilisateurRepository utilisateurRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final EmailService emailService;

    @Value("${jwt.refresh-token-expiration-ms:604800000}")
    private long refreshTokenExpirationMs;

    public AuthService(UtilisateurRepository utilisateurRepository,
                       RefreshTokenRepository refreshTokenRepository,
                       PasswordResetTokenRepository passwordResetTokenRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider jwtTokenProvider,
                       EmailService emailService) {
        this.utilisateurRepository = utilisateurRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.emailService = emailService;
    }

    @Transactional
    public LoginResponse login(LoginRequest request, HttpServletRequest httpRequest) {
        Utilisateur utilisateur = utilisateurRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException("Email ou mot de passe incorrect"));

        if (!utilisateur.isCompteActif()) {
            throw new AccountDisabledException("Ce compte a été désactivé. Contactez un administrateur.");
        }

        if (utilisateur.getCompteVerrouilleJusqu() != null
                && utilisateur.getCompteVerrouilleJusqu().isAfter(LocalDateTime.now())) {
            throw new AccountLockedException(
                    "Compte temporairement verrouillé suite à plusieurs échecs. Réessayez après "
                            + utilisateur.getCompteVerrouilleJusqu());
        }

        if (utilisateur.getMotDePasse() == null
                || !passwordEncoder.matches(request.getMotDePasse(), utilisateur.getMotDePasse())) {
            handleFailedAttempt(utilisateur);
            throw new InvalidCredentialsException("Email ou mot de passe incorrect");
        }

        // Succès : reset des compteurs de sécurité
        utilisateur.setTentativesEchouees((short) 0);
        utilisateur.setCompteVerrouilleJusqu(null);
        utilisateur.setDernierLogin(LocalDateTime.now());
        utilisateur.setDerniereIp(extractClientIp(httpRequest));
        utilisateurRepository.save(utilisateur);

        UserPrincipal principal = UserPrincipal.create(utilisateur);
        String accessToken = jwtTokenProvider.generateAccessToken(principal);
        String rawRefreshToken = generateAndStoreRefreshToken(utilisateur, httpRequest);

        return new LoginResponse(
                accessToken,
                rawRefreshToken,
                utilisateur.getId(),
                utilisateur.getNom(),
                utilisateur.getPrenom(),
                utilisateur.getEmail(),
                utilisateur.getTypeUtilisateur(),
                redirectUrlForRole(utilisateur.getTypeUtilisateur())
        );
    }

    @Transactional
    public LoginResponse refresh(String rawRefreshToken, HttpServletRequest httpRequest) {
        String hash = hash(rawRefreshToken);
        RefreshToken stored = refreshTokenRepository.findByTokenHash(hash)
                .orElseThrow(() -> new InvalidRefreshTokenException("Refresh token invalide"));

        if (stored.isRevoked() || stored.getDateExpiration().isBefore(LocalDateTime.now())) {
            throw new InvalidRefreshTokenException("Refresh token expiré ou révoqué, veuillez vous reconnecter");
        }

        Utilisateur utilisateur = stored.getUtilisateur();

        // Rotation : on révoque l'ancien et on en émet un nouveau
        stored.setRevoked(true);
        refreshTokenRepository.save(stored);

        UserPrincipal principal = UserPrincipal.create(utilisateur);
        String newAccessToken = jwtTokenProvider.generateAccessToken(principal);
        String newRawRefreshToken = generateAndStoreRefreshToken(utilisateur, httpRequest);

        return new LoginResponse(
                newAccessToken,
                newRawRefreshToken,
                utilisateur.getId(),
                utilisateur.getNom(),
                utilisateur.getPrenom(),
                utilisateur.getEmail(),
                utilisateur.getTypeUtilisateur(),
                redirectUrlForRole(utilisateur.getTypeUtilisateur())
        );
    }

    @Transactional
    public void logout(String rawRefreshToken) {
        refreshTokenRepository.findByTokenHash(hash(rawRefreshToken))
                .ifPresent(token -> {
                    token.setRevoked(true);
                    refreshTokenRepository.save(token);
                });
    }

    /**
     * Toujours "silencieux" côté sécurité : ne révèle jamais si l'email
     * existe ou non. Le controller renvoie systématiquement le même message.
     */
    @Transactional
    public void forgotPassword(String email) {
        utilisateurRepository.findByEmail(email).ifPresent(utilisateur -> {
            // On invalide les anciens tokens en attente pour cet utilisateur
            passwordResetTokenRepository.deleteByUtilisateur_Id(utilisateur.getId());

            String rawToken = UUID.randomUUID().toString();

            PasswordResetToken resetToken = new PasswordResetToken();
            resetToken.setUtilisateur(utilisateur);
            resetToken.setTokenHash(hash(rawToken));
            resetToken.setDateExpiration(LocalDateTime.now().plusMinutes(RESET_TOKEN_VALIDITY_MINUTES));
            passwordResetTokenRepository.save(resetToken);

            emailService.sendPasswordResetEmail(
                    utilisateur.getEmail(), utilisateur.getPrenom(), rawToken);
        });
    }

    @Transactional
    public void resetPassword(String rawToken, String nouveauMotDePasse) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByTokenHash(hash(rawToken))
                .orElseThrow(() -> new InvalidResetTokenException(
                        "Lien de réinitialisation invalide ou expiré"));

        if (resetToken.isUtilise() || resetToken.getDateExpiration().isBefore(LocalDateTime.now())) {
            throw new InvalidResetTokenException("Lien de réinitialisation invalide ou expiré");
        }

        Utilisateur utilisateur = resetToken.getUtilisateur();
        utilisateur.setMotDePasse(passwordEncoder.encode(nouveauMotDePasse));
        utilisateur.setTentativesEchouees((short) 0);
        utilisateur.setCompteVerrouilleJusqu(null);
        utilisateurRepository.save(utilisateur);

        resetToken.setUtilise(true);
        passwordResetTokenRepository.save(resetToken);
    }

    private void handleFailedAttempt(Utilisateur utilisateur) {
        short tentatives = (short) (utilisateur.getTentativesEchouees() + 1);
        utilisateur.setTentativesEchouees(tentatives);

        if (tentatives >= MAX_TENTATIVES) {
            utilisateur.setCompteVerrouilleJusqu(LocalDateTime.now().plusMinutes(LOCK_DURATION_MINUTES));
        }
        utilisateurRepository.save(utilisateur);
    }

    private String generateAndStoreRefreshToken(Utilisateur utilisateur, HttpServletRequest httpRequest) {
        String rawToken = UUID.randomUUID().toString();

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUtilisateur(utilisateur);
        refreshToken.setTokenHash(hash(rawToken));
        refreshToken.setDateExpiration(LocalDateTime.now().plusSeconds(refreshTokenExpirationMs / 1000));
        refreshToken.setUserAgent(httpRequest != null ? httpRequest.getHeader("User-Agent") : null);
        refreshToken.setIpAdresse(httpRequest != null ? extractClientIp(httpRequest) : null);

        refreshTokenRepository.save(refreshToken);
        return rawToken;
    }

    private String hash(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(value.getBytes());
            return Base64.getEncoder().encodeToString(hashed);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Algorithme de hachage indisponible", e);
        }
    }

    private String extractClientIp(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        String forwarded = request.getHeader("X-Forwarded-For");
        return (forwarded != null && !forwarded.isBlank()) ? forwarded.split(",")[0].trim() : request.getRemoteAddr();
    }

    /**
     * Détermine la route Angular de redirection post-login selon le rôle.
     * À adapter/étendre au fur et à mesure de la création des modules front.
     */
    private String redirectUrlForRole(String role) {
        return switch (role.toUpperCase(Locale.ROOT)) {
            case "ADMINISTRATEUR" -> "/admin/dashboard";
            case "TECHNICIEN" -> "/technicien/dashboard";
            case "OBSERVATEUR" -> "/client/dashboard";
            case "GESTIONNAIRE_STOCK" -> "/gestionnaire/dashboard";
            case "PARTENAIRE" -> "/partenaire/dashboard";
            default -> "/login";
        };
    }
}