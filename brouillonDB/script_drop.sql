-- ============================================================
-- SCRIPT DE NETTOYAGE — Avant insertion des données de test
-- Ordre inverse des dépendances (FK d'abord)
-- ============================================================

DELETE FROM mouvement_stock_crevette;
DELETE FROM mouvement_stock_medicament;
DELETE FROM mouvement_stock_aliment;
DELETE FROM distribution_nourriture_lot;
DELETE FROM distribution_nourriture;
DELETE FROM traitement_medicament_lot;
DELETE FROM traitement;
DELETE FROM incident_sanitaire;
DELETE FROM lot_crevette;
DELETE FROM entree_stock_medicament;
DELETE FROM entree_stock_aliment;
DELETE FROM histo_statut_bassin;
DELETE FROM cycle_bassin_assoc;
DELETE FROM cycle;
DELETE FROM bassin;
DELETE FROM suivi_hebdo_bassin;
DELETE FROM evolution_hebdo_espece;
DELETE FROM espece_crevette;
DELETE FROM creneau_horaire;
DELETE FROM statut_bassin;
DELETE FROM utilisateur_role;
DELETE FROM role;
DELETE FROM medicament;
DELETE FROM aliment;
DELETE FROM utilisateur;
