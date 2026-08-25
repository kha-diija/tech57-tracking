package com.example.backend.dto.admin.etablissement;

import java.util.ArrayList;
import java.util.List;

public class EtablissementImportResult {
    private int totalLignes;
    private int crees;
    private int misAJour;
    private int ignores;
    private List<String> erreurs = new ArrayList<>();

    public int getTotalLignes() { return totalLignes; }
    public void setTotalLignes(int totalLignes) { this.totalLignes = totalLignes; }
    public int getCrees() { return crees; }
    public void setCrees(int crees) { this.crees = crees; }
    public int getMisAJour() { return misAJour; }
    public void setMisAJour(int misAJour) { this.misAJour = misAJour; }
    public int getIgnores() { return ignores; }
    public void setIgnores(int ignores) { this.ignores = ignores; }
    public List<String> getErreurs() { return erreurs; }
    public void setErreurs(List<String> erreurs) { this.erreurs = erreurs; }
}