package com.example.backend.repository.admin;

import com.example.backend.entity.ChecklistEquipement;
import com.example.backend.entity.ChecklistItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChecklistItemRepository extends JpaRepository<ChecklistItem, Integer> {
    List<ChecklistItem> findByChecklist(ChecklistEquipement checklistEquipement);
    void deleteByChecklist(ChecklistEquipement checklistEquipement);
    @Query("SELECT i FROM ChecklistItem i WHERE i.checklist.idChecklist IN :ids")
    List<ChecklistItem> findByChecklistIds(@Param("ids") List<Integer> ids);
}