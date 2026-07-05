package com.example.orose.repository.stock;

import com.example.orose.model.LotCrevette;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface LotCrevetteRepository extends JpaRepository<LotCrevette, Integer> {
    List<LotCrevette> findAllByOrderByDateRecolteDesc();

    /**
     * DÉSACTIVÉ : La biomasse actuelle n'est plus une colonne de LotCrevette.
     * Elle est maintenant calculée dynamiquement à partir de :
     * 1. RecolteDeclaration.recolteReelleKg (biomasse totale)
     * 2. MouvementStockCrevette (pertes/mouvements)
     *
     * Pour obtenir la biomasse actuelle d'un lot, il faut :
     * - Charger les mouvements du lot
     * - Soustraire les pertes à recolteReelleKg
     */
    // @Query("SELECT COALESCE(SUM(l.biomasseActuelleKg), 0) FROM LotCrevette l")
    // BigDecimal sumBiomasseActuelle();

 

    @Query("SELECT COALESCE(SUM(l.biomasseTotaleKg), 0) FROM LotCrevette l WHERE YEAR(l.dateRecolte) = :annee")
    float totaleProductionAnnuelle(@Param("annee") int annee);

    @Query("SELECT COALESCE(SUM(l.biomasseTotaleKg), 0) from LotCrevette l WHERE YEAR(l.dateRecolte) = :annee AND MONTH(l.dateRecolte) = :mois")
    float getProductionMensuelle(@Param("annee") int annee, @Param("mois") int mois);
    
    // @Query("SELECT COALESCE(SUM(l.biomasseActuelleKg), 0) FROM LotCrevette l WHERE l.biomasseActuelleKg > 0")
    // BigDecimal sumBiomasseDisponible();
}
