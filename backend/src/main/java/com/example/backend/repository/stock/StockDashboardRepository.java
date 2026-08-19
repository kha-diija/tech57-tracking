package com.example.backend.repository.stock;

import com.example.backend.entity.StockMateriel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StockDashboardRepository extends JpaRepository<StockMateriel, Integer> {

    @Query("SELECT COALESCE(SUM(s.quantiteDisponible), 0) FROM StockMateriel s")
    long sumQuantiteDisponible();

    @Query("SELECT COALESCE(SUM(s.quantiteReservee), 0) FROM StockMateriel s")
    long sumQuantiteReservee();

    @Query("SELECT COALESCE(SUM(s.quantiteEnPanne), 0) FROM StockMateriel s")
    long sumQuantiteEnPanne();

    @Query("SELECT s FROM StockMateriel s WHERE s.quantiteDisponible <= s.seuilAlerte")
    List<StockMateriel> findStockBas();
}