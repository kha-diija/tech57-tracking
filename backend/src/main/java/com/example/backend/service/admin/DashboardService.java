package com.example.backend.service.admin;

import com.example.backend.dto.admin.dashboard.*;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

@Service
public class DashboardService {

    public List<KpiResponse> getKpis(String period) {
        // Optionnel : Adapter les valeurs selon la période ("7d" ou "30d")
        KpiResponse kpi1 = new KpiResponse();
        kpi1.setId("missions");
        kpi1.setLabel("Missions actives");
        kpi1.setValue("30d".equals(period) ? 450 : 128);
        kpi1.setTrend(12);
        kpi1.setTrendUp(true);
        kpi1.setComparison("vs " + period);

        KpiResponse kpi2 = new KpiResponse();
        kpi2.setId("etablissements");
        kpi2.setLabel("Établissements suivis");
        kpi2.setValue(342);
        kpi2.setTrend(4.2);
        kpi2.setTrendUp(true);
        kpi2.setComparison("sur le territoire");

        KpiResponse kpi3 = new KpiResponse();
        kpi3.setId("techniciens");
        kpi3.setLabel("Techniciens sur le terrain");
        kpi3.setValue(47);
        kpi3.setTrend(98);
        kpi3.setTrendUp(true);
        kpi3.setComparison("taux de disponibilité");

        KpiResponse kpi4 = new KpiResponse();
        kpi4.setId("conformite");
        kpi4.setLabel("Taux de conformité");
        kpi4.setValue(94.6);
        kpi4.setSuffix("%");
        kpi4.setTrend(2.6);
        kpi4.setTrendUp(true);
        kpi4.setComparison("objectif 92%");

        return Arrays.asList(kpi1, kpi2, kpi3, kpi4);
    }

    public List<WeeklyMissionResponse> getWeeklyMissions() {
        WeeklyMissionResponse m1 = new WeeklyMissionResponse(); m1.setDay("Lun"); m1.setPlanned(42); m1.setCompleted(34);
        WeeklyMissionResponse m2 = new WeeklyMissionResponse(); m2.setDay("Mar"); m2.setPlanned(51); m2.setCompleted(40);
        WeeklyMissionResponse m3 = new WeeklyMissionResponse(); m3.setDay("Mer"); m3.setPlanned(60); m3.setCompleted(52);
        WeeklyMissionResponse m4 = new WeeklyMissionResponse(); m4.setDay("Jeu"); m4.setPlanned(38); m4.setCompleted(30);
        WeeklyMissionResponse m5 = new WeeklyMissionResponse(); m5.setDay("Ven"); m5.setPlanned(58); m5.setCompleted(55);
        WeeklyMissionResponse m6 = new WeeklyMissionResponse(); m6.setDay("Sam"); m6.setPlanned(25); m6.setCompleted(22);
        WeeklyMissionResponse m7 = new WeeklyMissionResponse(); m7.setDay("Dim"); m7.setPlanned(14); m7.setCompleted(14);
        return Arrays.asList(m1, m2, m3, m4, m5, m6, m7);
    }

    public List<InstallationPointResponse> getInstallationProgress() {
        InstallationPointResponse p1 = new InstallationPointResponse(); p1.setWeek("S1"); p1.setValue(120);
        InstallationPointResponse p2 = new InstallationPointResponse(); p2.setWeek("S2"); p2.setValue(260);
        InstallationPointResponse p3 = new InstallationPointResponse(); p3.setWeek("S3"); p3.setValue(410);
        InstallationPointResponse p4 = new InstallationPointResponse(); p4.setWeek("S4"); p4.setValue(590);
        InstallationPointResponse p5 = new InstallationPointResponse(); p5.setWeek("S5"); p5.setValue(780);
        InstallationPointResponse p6 = new InstallationPointResponse(); p6.setWeek("S6"); p6.setValue(980);
        InstallationPointResponse p7 = new InstallationPointResponse(); p7.setWeek("S7"); p7.setValue(1180);
        InstallationPointResponse p8 = new InstallationPointResponse(); p8.setWeek("S8"); p8.setValue(1400);
        return Arrays.asList(p1, p2, p3, p4, p5, p6, p7, p8);
    }

    public List<MaterialDistributionResponse> getMaterialDistribution() {
        MaterialDistributionResponse d1 = new MaterialDistributionResponse(); d1.setLabel("Installé"); d1.setValue(3412); d1.setColor("#e85002");
        MaterialDistributionResponse d2 = new MaterialDistributionResponse(); d2.setLabel("En transit"); d2.setValue(412); d2.setColor("#ff9d76");
        MaterialDistributionResponse d3 = new MaterialDistributionResponse(); d3.setLabel("En stock"); d3.setValue(890); d3.setColor("#2a2f3d");
        return Arrays.asList(d1, d2, d3);
    }

    public List<ActivityItemResponse> getRecentActivity() {
        ActivityItemResponse a1 = new ActivityItemResponse();
        a1.setId("a1"); a1.setAuthor("K. El Amrani"); a1.setAction("a terminé l’installation à"); a1.setTarget("Lycée Ibn Sina"); a1.setTime("Il y a 2 min"); a1.setStatus("success");

        ActivityItemResponse a2 = new ActivityItemResponse();
        a2.setId("a2"); a2.setAuthor("S. Bennani"); a2.setAction("s’est enregistré(e) à"); a2.setTarget("École Al Farabi"); a2.setTime("Il y a 9 min"); a2.setStatus("info");

        ActivityItemResponse a3 = new ActivityItemResponse();
        a3.setId("a3"); a3.setAuthor("M. Zaki"); a3.setAction("a signalé un événement à"); a3.setTarget("Collège Anfa"); a3.setTime("Il y a 22 min"); a3.setStatus("warning");

        return Arrays.asList(a1, a2, a3);
    }

    public List<UpcomingMissionResponse> getUpcomingMissions() {
        UpcomingMissionResponse u1 = new UpcomingMissionResponse();
        u1.setCode("M-8901"); u1.setTitle("Université Hassan II — Bloc C"); u1.setSubtitle("K. El Amrani"); u1.setTime("10:30"); u1.setTechnicien("K. El Amrani");

        UpcomingMissionResponse u2 = new UpcomingMissionResponse();
        u2.setCode("M-8902"); u2.setTitle("École Al Wahda"); u2.setSubtitle("S. Bennani"); u2.setTime("11:15"); u2.setTechnicien("S. Bennani");

        return Arrays.asList(u1, u2);
    }

    // --- Nouvelles fonctionnalités ---

    public byte[] exportDashboardData() {
        StringBuilder csv = new StringBuilder("\uFEFF"); // BOM UTF-8 pour Excel

        csv.append("=== RAPPORT DASHBOARD - ADMINISTRATION ===\r\n\r\n");

        // 1. Section KPI
        csv.append("--- INDICATEURS CLES (KPI) ---\r\n");
        csv.append("Indicateur;Valeur;Comparaison\r\n");
        List<KpiResponse> kpis = getKpis("7d");
        for (KpiResponse kpi : kpis) {
            csv.append("\"").append(kpi.getLabel()).append("\";")
                    .append("\"").append(kpi.getValue()).append(kpi.getSuffix() != null ? kpi.getSuffix() : "").append("\";")
                    .append("\"").append(kpi.getComparison()).append("\"\r\n");
        }
        csv.append("\r\n");

        // 2. Section Missions à venir
        csv.append("--- MISSIONS A VENIR ---\r\n");
        csv.append("Code;Titre;Heure;Technicien\r\n");
        List<UpcomingMissionResponse> upcoming = getUpcomingMissions();
        for (UpcomingMissionResponse m : upcoming) {
            csv.append("\"").append(m.getCode()).append("\";")
                    .append("\"").append(m.getTitle()).append("\";")
                    .append("\"").append(m.getTime()).append("\";")
                    .append("\"").append(m.getTechnicien()).append("\"\r\n");
        }
        csv.append("\r\n");

        // 3. Section Activité récente
        csv.append("--- ACTIVITE RECENTE ---\r\n");
        csv.append("Auteur;Action;Cible;Heure\r\n");
        List<ActivityItemResponse> activities = getRecentActivity();
        for (ActivityItemResponse a : activities) {
            csv.append("\"").append(a.getAuthor()).append("\";")
                    .append("\"").append(a.getAction()).append("\";")
                    .append("\"").append(a.getTarget()).append("\";")
                    .append("\"").append(a.getTime()).append("\"\r\n");
        }
        csv.append("\r\n");

        // 4. Section Progression des installations
        csv.append("--- PROGRESSION DES INSTALLATIONS ---\r\n");
        csv.append("Semaine;Valeur\r\n");
        List<InstallationPointResponse> progress = getInstallationProgress();
        for (InstallationPointResponse p : progress) {
            csv.append("\"").append(p.getWeek()).append("\";")
                    .append("\"").append(p.getValue()).append("\"\r\n");
        }

        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    public UpcomingMissionResponse createMission(Object missionRequest) {
        // Logique d'enregistrement en base de données à implémenter ici
        UpcomingMissionResponse newMission = new UpcomingMissionResponse();
        newMission.setCode("M-" + (int)(Math.random() * 9000 + 1000));
        newMission.setTitle("Nouvelle Mission Terrain");
        newMission.setSubtitle("Assignée via Dashboard");
        newMission.setTime("09:00");
        newMission.setTechnicien("À assigner");
        return newMission;
    }
}