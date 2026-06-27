package com.example.orose.service.stock;

import com.example.orose.dto.stock.*;
import com.example.orose.model.*;
import com.example.orose.repository.*;
import com.example.orose.repository.stock.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class StockService {

    private final AlimentRepository alimentRepository;
    private final MedicamentRepository medicamentRepository;
    private final EntreeStockAlimentRepository entreeAlimentRepository;
    private final EntreeStockMedicamentRepository entreeMedicamentRepository;
    private final MouvementStockAlimentRepository mouvementAlimentRepository;
    private final MouvementStockMedicamentRepository mouvementMedicamentRepository;
    private final LotCrevetteRepository lotCrevetteRepository;
    private final MouvementStockCrevetteRepository mouvementCrevetteRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final AlerteRepository alerteRepository;

    public StockService(AlimentRepository alimentRepository,
            MedicamentRepository medicamentRepository,
            EntreeStockAlimentRepository entreeAlimentRepository,
            EntreeStockMedicamentRepository entreeMedicamentRepository,
            MouvementStockAlimentRepository mouvementAlimentRepository,
            MouvementStockMedicamentRepository mouvementMedicamentRepository,
            LotCrevetteRepository lotCrevetteRepository,
            MouvementStockCrevetteRepository mouvementCrevetteRepository,
            UtilisateurRepository utilisateurRepository,
            AlerteRepository alerteRepository) {
        this.alimentRepository = alimentRepository;
        this.medicamentRepository = medicamentRepository;
        this.entreeAlimentRepository = entreeAlimentRepository;
        this.entreeMedicamentRepository = entreeMedicamentRepository;
        this.mouvementAlimentRepository = mouvementAlimentRepository;
        this.mouvementMedicamentRepository = mouvementMedicamentRepository;
        this.lotCrevetteRepository = lotCrevetteRepository;
        this.mouvementCrevetteRepository = mouvementCrevetteRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.alerteRepository = alerteRepository;
    }

    // ────────────────────── Utilitaires ──────────────────────

    private Float toFloat(BigDecimal bd) {
        return bd == null ? 0f : bd.floatValue();
    }

    private BigDecimal calcStockMedicament(Medicament m) {
        BigDecimal stock = BigDecimal.ZERO;
        for (EntreeStockMedicament e : entreeMedicamentRepository.findByQuantiteRestanteGreaterThan(BigDecimal.ZERO)) {
            if (e.getMedicament().getId().equals(m.getId())) {
                stock = stock.add(e.getQuantiteRestante());
            }
        }
        return stock;
    }

    private Float seuilMedicament(Medicament m) {
        return m.getSeuilMinimum().floatValue();
    }

    // ────────────────────── Dashboard ──────────────────────

    public StockDashboardDTO getDashboard() {
        StockDashboardDTO dto = new StockDashboardDTO();
        remplirStockCrevette(dto);
        remplirStockAliment(dto);
        remplirStockMedicament(dto);
        dto.setNbProduitsFaibles(compterProduitsFaibles());
        dto.setNbLotsPerimes(compterLotsPerimes());
        dto.setAlertes(getAlertes());
        return dto;
    }

    private void remplirStockCrevette(StockDashboardDTO dto) {
        BigDecimal biomasse = lotCrevetteRepository.sumBiomasseDisponible();
        dto.setStockCrevetteKg(toFloat(biomasse));
        dto.setValeurCrevetteAr(toFloat(biomasse.multiply(BigDecimal.valueOf(30000))));
    }

    private void remplirStockAliment(StockDashboardDTO dto) {
        BigDecimal stockAliment = entreeAlimentRepository.sumQuantiteRestante();
        dto.setStockAlimentKg(stockAliment != null ? stockAliment.floatValue() : 0f);
        dto.setAutonomieAlimentJours(estimerAutonomieAliment());
    }

    private void remplirStockMedicament(StockDashboardDTO dto) {
        BigDecimal total = BigDecimal.ZERO;
        for (EntreeStockMedicament e : entreeMedicamentRepository.findByQuantiteRestanteGreaterThan(BigDecimal.ZERO)) {
            total = total.add(e.getQuantiteRestante());
        }
        dto.setStockMedicamentTotal(toFloat(total));
    }

    private int compterProduitsFaibles() {
        int nb = 0;
        for (Aliment a : alimentRepository.findAll()) {
            BigDecimal stock = entreeAlimentRepository.sumStockByAliment(a.getId().longValue());
            if (stock != null && stock.compareTo(a.getSeuilMinimumKg()) <= 0) {
                nb++;
            }
        }
        for (Medicament m : medicamentRepository.findAll()) {
            if (calcStockMedicament(m).compareTo(m.getSeuilMinimum()) <= 0) {
                nb++;
            }
        }
        return nb;
    }

    private int compterLotsPerimes() {
        int nb = 0;
        LocalDate now = LocalDate.now();
        for (EntreeStockAliment e : entreeAlimentRepository.findAll()) {
            if (e.getQuantiteRestanteKg().compareTo(BigDecimal.ZERO) > 0 && e.getDateExpiration().isBefore(now)) {
                nb++;
            }
        }
        for (EntreeStockMedicament e : entreeMedicamentRepository.findAll()) {
            if (e.getQuantiteRestante().compareTo(BigDecimal.ZERO) > 0 && e.getDateExpiration().isBefore(now)) {
                nb++;
            }
        }
        return nb;
    }

    // ────────────────────── Autonomie ──────────────────────

    public Float estimerAutonomieAliment() {
        BigDecimal stockTotal = entreeAlimentRepository.sumQuantiteRestante();
        if (stockTotal == null || stockTotal.compareTo(BigDecimal.ZERO) <= 0) {
            return 0f;
        }

        LocalDate dateSeuil = LocalDate.now().minusDays(7);
        BigDecimal conso = entreeAlimentRepository.sumEntreesDepuis(dateSeuil);
        return conso != null && conso.compareTo(BigDecimal.ZERO) > 0
                ? (stockTotal.floatValue() / conso.floatValue()) * 7f
                : 30f;
    }

    // ────────────────────── Alertes ──────────────────────

    public List<StockAlerteDTO> getAlertes() {
        List<StockAlerteDTO> alertes = new ArrayList<>();
        alertes.addAll(buildAlertesRuptureFaible());
        alertes.addAll(buildAlertesPeremption());
        ajouterAlerteCrevette(alertes);
        return alertes;
    }

    private List<StockAlerteDTO> buildAlertesRuptureFaible() {
        List<StockAlerteDTO> alertes = new ArrayList<>();
        for (Aliment a : alimentRepository.findAll()) {
            BigDecimal stock = entreeAlimentRepository.sumStockByAliment(a.getId().longValue());
            if (stock != null) {
                if (stock.compareTo(BigDecimal.ZERO) <= 0) {
                    alertes.add(new StockAlerteDTO("RUPTURE", "ROUGE",
                            "Rupture de stock: " + a.getLibelle(), 0f));
                } else if (stock.compareTo(a.getSeuilMinimumKg()) <= 0) {
                    alertes.add(new StockAlerteDTO("STOCK_FAIBLE", "ORANGE",
                            "Stock faible: " + a.getLibelle() + " (" + stock.floatValue() + " kg)", stock.floatValue()));
                }
            }
        }
        for (Medicament m : medicamentRepository.findAll()) {
            BigDecimal stock = calcStockMedicament(m);
            if (stock.compareTo(BigDecimal.ZERO) <= 0) {
                alertes.add(new StockAlerteDTO("RUPTURE", "ROUGE",
                        "Rupture de stock: " + m.getLibelle(), 0f));
            } else if (stock.compareTo(m.getSeuilMinimum()) <= 0) {
                alertes.add(new StockAlerteDTO("STOCK_FAIBLE", "ORANGE",
                        "Stock faible: " + m.getLibelle() + " (" + stock.floatValue() + " " + m.getUnite() + ")", stock.floatValue()));
            }
        }
        return alertes;
    }

    private List<StockAlerteDTO> buildAlertesPeremption() {
        List<StockAlerteDTO> alertes = new ArrayList<>();
        LocalDate now = LocalDate.now();
        LocalDate seuilExpiration = now.plusDays(7);

        for (EntreeStockAliment e : entreeAlimentRepository.findStocksDisponibles()) {
            if (e.getDateExpiration().isBefore(now)) {
                alertes.add(new StockAlerteDTO("EXPIRE", "ROUGE",
                        "Lot périmé: " + e.getAliment().getLibelle() + " (expiré le " + e.getDateExpiration() + ")",
                        e.getQuantiteRestanteKg().floatValue()));
            } else if (e.getDateExpiration().isBefore(seuilExpiration)) {
                alertes.add(new StockAlerteDTO("EXPIRATION_PROCHAINE", "ORANGE",
                        "Expiration proche: " + e.getAliment().getLibelle() + " (expire le " + e.getDateExpiration() + ")",
                        e.getQuantiteRestanteKg().floatValue()));
            }
        }

        for (EntreeStockMedicament e : entreeMedicamentRepository.findByQuantiteRestanteGreaterThan(BigDecimal.ZERO)) {
            if (e.getDateExpiration().isBefore(now)) {
                alertes.add(new StockAlerteDTO("EXPIRE", "ROUGE",
                        "Lot périmé: " + e.getMedicament().getLibelle() + " (expiré le " + e.getDateExpiration() + ")",
                        e.getQuantiteRestante().floatValue()));
            } else if (e.getDateExpiration().isBefore(seuilExpiration)) {
                alertes.add(new StockAlerteDTO("EXPIRATION_PROCHAINE", "ORANGE",
                        "Expiration proche: " + e.getMedicament().getLibelle() + " (expire le " + e.getDateExpiration() + ")",
                        e.getQuantiteRestante().floatValue()));
            }
        }

        return alertes;
    }

    private void ajouterAlerteCrevette(List<StockAlerteDTO> alertes) {
        BigDecimal biomasseDispo = lotCrevetteRepository.sumBiomasseDisponible();
        if (biomasseDispo == null || biomasseDispo.compareTo(BigDecimal.ZERO) <= 0) {
            alertes.add(new StockAlerteDTO("RUPTURE_CREVETTE", "ROUGE",
                    "Rupture stock crevette — aucun stock disponible", 0f));
        }
    }

    // ────────────────────── Liste produits ──────────────────────

    public List<ProduitStockDTO> getListeProduits() {
        List<ProduitStockDTO> produits = new ArrayList<>();
        produits.addAll(buildProduitsAliment());
        produits.addAll(buildProduitsMedicament());
        return produits;
    }

    private List<ProduitStockDTO> buildProduitsAliment() {
        List<ProduitStockDTO> produits = new ArrayList<>();
        for (Aliment a : alimentRepository.findAll()) {
            BigDecimal stock = entreeAlimentRepository.sumStockByAliment(a.getId().longValue());
            Float stockF = stock != null ? stock.floatValue() : 0f;
            Float seuil = a.getSeuilMinimumKg().floatValue();
            String statut = stockF <= 0f ? "Rupture" : (stockF <= seuil ? "Faible" : "Optimal");
            String css = stockF <= 0f ? "danger" : (stockF <= seuil ? "warning" : "success");
            produits.add(new ProduitStockDTO(a.getId(), a.getLibelle(), "ALIMENT", stockF, seuil, statut, css));
        }
        return produits;
    }

    private List<ProduitStockDTO> buildProduitsMedicament() {
        List<ProduitStockDTO> produits = new ArrayList<>();
        for (Medicament m : medicamentRepository.findAll()) {
            BigDecimal stock = calcStockMedicament(m);
            Float stockF = stock.floatValue();
            Float seuil = seuilMedicament(m);
            String statut = stockF <= 0f ? "Rupture" : (stockF <= seuil ? "Faible" : "Optimal");
            String css = stockF <= 0f ? "danger" : (stockF <= seuil ? "warning" : "success");
            produits.add(new ProduitStockDTO(m.getId(), m.getLibelle(), "MÉDICAMENT", stockF, seuil, statut, css));
        }
        return produits;
    }

    // ────────────────────── Entrée manuelle ──────────────────────

    @Transactional
    public void enregistrerEntreeIntrant(EntreeStockIntrantDTO dto) {
        Utilisateur resp = utilisateurRepository.findById(dto.getIdResponsable().longValue())
                .orElseThrow(() -> new RuntimeException("Responsable introuvable"));

        Medicament medicament = medicamentRepository.findById(dto.getIdProduit())
                .orElseThrow(() -> new RuntimeException("Médicament introuvable"));

        EntreeStockMedicament entree = new EntreeStockMedicament();
        entree.setMedicament(medicament);
        entree.setResponsable(resp);
        entree.setQuantite(BigDecimal.valueOf(dto.getQuantite()));
        entree.setQuantiteRestante(BigDecimal.valueOf(dto.getQuantite()));
        entree.setPrixTotalAr(BigDecimal.valueOf(dto.getPrixTotalAr() != null ? dto.getPrixTotalAr() : 0));
        entree.setDateReception(dto.getDateReception() != null ? dto.getDateReception() : LocalDate.now());
        entree.setDateExpiration(dto.getDateExpiration() != null ? dto.getDateExpiration() : LocalDate.now().plusMonths(6));
        entreeMedicamentRepository.save(entree);
    }

    // ────────────────────── Sortie manuelle ──────────────────────

    @Transactional
    public void enregistrerSortieManuelle(SortieStockIntrantDTO dto) {
        validerMotifSortie(dto.getMotif());

        Utilisateur resp = utilisateurRepository.findById(dto.getIdResponsable().longValue())
                .orElseThrow(() -> new RuntimeException("Responsable introuvable"));

        BigDecimal qte = BigDecimal.valueOf(dto.getQuantite());
        List<EntreeStockMedicament> lots = lotsDisponiblesTriesFIFO();
        BigDecimal aRetirer = retirerStockFIFO(lots, qte, dto.getMotif(), resp);

        if (aRetirer.compareTo(BigDecimal.ZERO) > 0) {
            throw new RuntimeException("Stock médicament insuffisant pour effectuer la sortie");
        }
    }

    private void validerMotifSortie(String motif) {
        if (!"PERTE".equals(motif) && !"DESTRUCTION".equals(motif)) {
            throw new RuntimeException("Motif invalide. Seuls PERTE et DESTRUCTION sont autorisés.");
        }
    }

    private List<EntreeStockMedicament> lotsDisponiblesTriesFIFO() {
        return entreeMedicamentRepository.findByQuantiteRestanteGreaterThan(BigDecimal.ZERO)
                .stream()
                .sorted(Comparator.comparing(EntreeStockMedicament::getDateExpiration))
                .collect(Collectors.toList());
    }

    private BigDecimal retirerStockFIFO(List<EntreeStockMedicament> lots, BigDecimal aRetirer,
            String motif, Utilisateur resp) {
        for (EntreeStockMedicament lot : lots) {
            if (aRetirer.compareTo(BigDecimal.ZERO) <= 0) break;
            if (lot.getQuantiteRestante().compareTo(BigDecimal.ZERO) <= 0) continue;

            BigDecimal retire = lot.getQuantiteRestante().min(aRetirer);
            lot.setQuantiteRestante(lot.getQuantiteRestante().subtract(retire));
            entreeMedicamentRepository.save(lot);

            MouvementStockMedicament mvt = new MouvementStockMedicament();
            mvt.setEntreeMedicament(lot);
            mvt.setTypeMouvement(motif);
            mvt.setQuantite(retire);
            mvt.setMotif(motif + " - " + resp.getNom());
            mvt.setDateMouvement(LocalDateTime.now());
            mvt.setResponsable(resp);
            mouvementMedicamentRepository.save(mvt);

            aRetirer = aRetirer.subtract(retire);
        }
        return aRetirer;
    }

    // ────────────────────── Historique mouvements ──────────────────────

    public List<MouvementStockDTO> getHistoriqueMouvements() {
        List<MouvementStockDTO> mouvements = new ArrayList<>();
        mouvements.addAll(buildSortiesAliment());
        mouvements.addAll(buildSortiesMedicament());
        mouvements.addAll(buildSortiesCrevette());
        mouvements.addAll(buildEntreesAliment());
        mouvements.addAll(buildEntreesMedicament());
        mouvements.addAll(buildEntreesCrevette());
        mouvements.sort((a, b) -> b.getDateMouvement().compareTo(a.getDateMouvement()));
        return mouvements;
    }

    private List<MouvementStockDTO> buildSortiesAliment() {
        List<MouvementStockDTO> list = new ArrayList<>();
        for (MouvementStockAliment m : mouvementAlimentRepository.findAllByOrderByDateMouvementDesc()) {
            MouvementStockDTO dto = new MouvementStockDTO();
            dto.setDateMouvement(m.getDateMouvement());
            dto.setProduit(m.getEntreeAliment().getAliment().getLibelle());
            dto.setType("SORTIE");
            dto.setQuantite(-m.getQuantiteKg().floatValue());
            dto.setMotif(m.getTypeMouvement());
            dto.setSource("Manuel");
            dto.setResponsable(m.getUtilisateur().getNom());
            list.add(dto);
        }
        return list;
    }

    private List<MouvementStockDTO> buildSortiesMedicament() {
        List<MouvementStockDTO> list = new ArrayList<>();
        for (MouvementStockMedicament m : mouvementMedicamentRepository.findAll()) {
            MouvementStockDTO dto = new MouvementStockDTO();
            dto.setDateMouvement(m.getDateMouvement());
            dto.setProduit(m.getEntreeMedicament().getMedicament().getLibelle());
            dto.setType("SORTIE");
            dto.setQuantite(-m.getQuantite().floatValue());
            dto.setMotif(m.getTypeMouvement());
            dto.setSource("Manuel");
            dto.setResponsable(m.getResponsable().getNom());
            list.add(dto);
        }
        return list;
    }

    private List<MouvementStockDTO> buildSortiesCrevette() {
        List<MouvementStockDTO> list = new ArrayList<>();
        for (MouvementStockCrevette m : mouvementCrevetteRepository.findAllByOrderByDateMouvementDesc()) {
            MouvementStockDTO dto = new MouvementStockDTO();
            dto.setDateMouvement(m.getDateMouvement());
            dto.setProduit("Crevette - " + m.getLotCrevette().getNumeroLotUnique());
            dto.setType("SORTIE");
            dto.setQuantite(-m.getQuantiteKg().floatValue());
            dto.setMotif(m.getTypeMouvement());
            dto.setSource("Manuel");
            dto.setResponsable(m.getUtilisateur().getNom());
            list.add(dto);
        }
        return list;
    }

    private List<MouvementStockDTO> buildEntreesAliment() {
        List<MouvementStockDTO> list = new ArrayList<>();
        for (EntreeStockAliment e : entreeAlimentRepository.findAll()) {
            MouvementStockDTO dto = new MouvementStockDTO();
            dto.setDateMouvement(e.getDateReception().atStartOfDay());
            dto.setProduit(e.getAliment().getLibelle());
            dto.setType("ENTRÉE");
            dto.setQuantite(e.getQuantiteKg().floatValue());
            dto.setMotif("Réception fournisseur");
            dto.setSource(e.getPrixTotalAr().compareTo(BigDecimal.ZERO) > 0 ? "Achat" : "Manuel");
            dto.setResponsable(e.getResponsable().getNom());
            list.add(dto);
        }
        return list;
    }

    private List<MouvementStockDTO> buildEntreesMedicament() {
        List<MouvementStockDTO> list = new ArrayList<>();
        for (EntreeStockMedicament e : entreeMedicamentRepository.findAll()) {
            MouvementStockDTO dto = new MouvementStockDTO();
            dto.setDateMouvement(e.getDateReception().atStartOfDay());
            dto.setProduit(e.getMedicament().getLibelle());
            dto.setType("ENTRÉE");
            dto.setQuantite(e.getQuantite().floatValue());
            dto.setMotif("Réception fournisseur");
            dto.setSource(e.getPrixTotalAr().compareTo(BigDecimal.ZERO) > 0 ? "Achat" : "Manuel");
            dto.setResponsable(e.getResponsable().getNom());
            list.add(dto);
        }
        return list;
    }

    private List<MouvementStockDTO> buildEntreesCrevette() {
        List<MouvementStockDTO> list = new ArrayList<>();
        for (LotCrevette l : lotCrevetteRepository.findAllByOrderByDateRecolteDesc()) {
            MouvementStockDTO dto = new MouvementStockDTO();
            dto.setDateMouvement(l.getDateRecolte().atStartOfDay());
            dto.setProduit("Crevette (calibre ≥ 15g)");
            dto.setType("ENTRÉE");
            dto.setQuantite(l.getBiomasseTotaleKg().floatValue());
            dto.setMotif("Récolte - Lot " + l.getNumeroLotUnique());
            dto.setSource("Automatique");
            String bassin = l.getCycleBassinAssoc() != null && l.getCycleBassinAssoc().getBassin() != null
                    ? l.getCycleBassinAssoc().getBassin().getCode() : "N/A";
            dto.setResponsable(bassin);
            list.add(dto);
        }
        return list;
    }

    public List<MouvementStockDTO> getDerniersMouvements(int limit) {
        List<MouvementStockDTO> all = getHistoriqueMouvements();
        return all.stream().limit(limit).collect(Collectors.toList());
    }

    // ────────────────────── Accesseurs ──────────────────────

    public Float getStockAlimentTotal() {
        BigDecimal total = entreeAlimentRepository.sumQuantiteRestante();
        return total != null ? total.floatValue() : 0f;
    }

    public List<LotCrevette> getLotsCrevette() {
        return lotCrevetteRepository.findAllByOrderByDateRecolteDesc();
    }

    public List<Aliment> getAliments() {
        return alimentRepository.findAll();
    }

    public List<Medicament> getMedicaments() {
        return medicamentRepository.findAll();
    }
}
