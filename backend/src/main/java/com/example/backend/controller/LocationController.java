package com.example.backend.controller;

import com.example.backend.dto.admin.etablissement.CommuneResponse;
import com.example.backend.dto.admin.etablissement.ProvinceResponse;
import com.example.backend.dto.admin.etablissement.RegionResponse;
import com.example.backend.repository.admin.CommuneRepository;
import com.example.backend.repository.admin.ProvinceRepository;
import com.example.backend.repository.admin.RegionRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/locations")
public class LocationController {

    private final RegionRepository regionRepository;
    private final ProvinceRepository provinceRepository;
    private final CommuneRepository communeRepository;

    public LocationController(RegionRepository regionRepository,
                              ProvinceRepository provinceRepository,
                              CommuneRepository communeRepository) {
        this.regionRepository = regionRepository;
        this.provinceRepository = provinceRepository;
        this.communeRepository = communeRepository;
    }

    @GetMapping("/regions")
    public ResponseEntity<List<RegionResponse>> getRegions() {
        List<RegionResponse> result = regionRepository.findAll().stream()
                .map(r -> new RegionResponse(r.getIdRegion(), r.getNom(), r.getCode()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/provinces")
    public ResponseEntity<List<ProvinceResponse>> getProvinces(
            @RequestParam(required = false) Integer regionId) {

        List<ProvinceResponse> result = (regionId != null
                ? provinceRepository.findByRegion_IdRegion(regionId)
                : provinceRepository.findAll())
                .stream()
                .map(p -> new ProvinceResponse(
                        p.getIdProvince(), p.getNom(), p.getCode(), p.getRegion().getIdRegion()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/communes")
    public ResponseEntity<List<CommuneResponse>> getCommunes(
            @RequestParam(required = false) Integer provinceId) {

        List<CommuneResponse> result = (provinceId != null
                ? communeRepository.findByProvince_IdProvince(provinceId)
                : communeRepository.findAll())
                .stream()
                .map(c -> new CommuneResponse(
                        c.getIdCommune(), c.getNom(), c.getCode(), c.getProvince().getIdProvince()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }
}