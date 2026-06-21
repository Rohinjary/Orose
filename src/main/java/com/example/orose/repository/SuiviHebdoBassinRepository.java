package com.example.orose.repository;

import com.example.orose.model.SuiviHebdoBassin;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SuiviHebdoBassinRepository extends JpaRepository<SuiviHebdoBassin, Long> {

    List<SuiviHebdoBassin> findByCycleBassinAssocIdOrderByDateSuiviAsc(Integer id);

    Optional<SuiviHebdoBassin> findTopByCycleBassinAssocIdOrderByDateSuiviDesc(Integer id);

    @Query("SELECT s FROM SuiviHebdoBassin s WHERE s.cycleBassinAssoc.id = :id ORDER BY s.dateSuivi DESC")
    List<SuiviHebdoBassin> findTop2ByCycleBassinAssocIdOrderByDateSuiviDesc(
            @Param("id") Integer id, Pageable pageable);
}
