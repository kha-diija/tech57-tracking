package com.example.backend.dto.technicien.Dashboard;

import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public class CheckOutRequest {
    private String gpsCheckout;

    // --- GESTION DES PHOTOS ---
    private List<MultipartFile> photos;
    private List<String> photoTypes; // "Avant" ou "Après" pour chaque photo

    // --- GESTION DE L'ATTESTATION (FICHIER) ---
    private MultipartFile attestationFile;

    // --- MATÉRIELS (LISTES MULTIPLES) ---
    private List<Integer> materielSortiIds;
    private List<Integer> materielRetourIds;
    private List<String> etatsRetours;

    // ✅ AJOUT : Bénéficiaires réel (seulement à la 1ère visite)
    private Integer beneficiairesReel;

    // --- GETTERS ET SETTERS ---
    public String getGpsCheckout() { return gpsCheckout; }
    public void setGpsCheckout(String gpsCheckout) { this.gpsCheckout = gpsCheckout; }

    public List<MultipartFile> getPhotos() { return photos; }
    public void setPhotos(List<MultipartFile> photos) { this.photos = photos; }

    public List<String> getPhotoTypes() { return photoTypes; }
    public void setPhotoTypes(List<String> photoTypes) { this.photoTypes = photoTypes; }

    public MultipartFile getAttestationFile() { return attestationFile; }
    public void setAttestationFile(MultipartFile attestationFile) { this.attestationFile = attestationFile; }

    public List<Integer> getMaterielSortiIds() { return materielSortiIds; }
    public void setMaterielSortiIds(List<Integer> materielSortiIds) { this.materielSortiIds = materielSortiIds; }

    public List<Integer> getMaterielRetourIds() { return materielRetourIds; }
    public void setMaterielRetourIds(List<Integer> materielRetourIds) { this.materielRetourIds = materielRetourIds; }

    public List<String> getEtatsRetours() { return etatsRetours; }
    public void setEtatsRetours(List<String> etatsRetours) { this.etatsRetours = etatsRetours; }

    // ✅ AJOUT : Getter et Setter pour bénéficiaires réel
    public Integer getBeneficiairesReel() { return beneficiairesReel; }
    public void setBeneficiairesReel(Integer beneficiairesReel) { this.beneficiairesReel = beneficiairesReel; }
}