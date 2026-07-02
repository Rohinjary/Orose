-- ============================================================
-- db_v3_mise_a_jour_corrige.sql — Schéma final consolidé OROSE
-- Version corrigée :
--   1) Encodage UTF-8 propre (plus d'erreur 0x90 / WIN1252)
--   2) Ajout des INSERT manquants pour cycle / cycle_bassin_assoc
--      AVANT les pesées hebdomadaires (résout les erreurs de FK)
--   3) Ordre d'exécution sécurisé (aliment avant stock, etc.)
-- ============================================================

-- Important : assurez-vous que le client psql utilise l'encodage UTF8
-- (la base "orose" doit aussi avoir été créée avec ENCODING 'UTF8').
-- Si vous éditez ce fichier sous Windows, enregistrez-le en UTF-8 (sans BOM),
-- pas en ANSI/Windows-1252 : c'est l'origine de l'erreur 0x90.

-- ------------------------------------------------------------
-- MODULE 0 : UTILISATEURS & RÔLES
-- ------------------------------------------------------------

CREATE TABLE role (
    id SERIAL PRIMARY KEY,
    code VARCHAR(20) NOT NULL UNIQUE,  -- ADMIN, TECH, RS, DIR
    libelle VARCHAR(50) NOT NULL
);

CREATE TABLE utilisateur (
    id SERIAL PRIMARY KEY,
    nom VARCHAR(100) NOT NULL,
    prenom VARCHAR(100),
    email VARCHAR(255) NOT NULL UNIQUE,
    mot_de_passe VARCHAR(255) NOT NULL,
    statut VARCHAR(20) NOT NULL DEFAULT 'ACTIF'  -- ACTIF, INACTIF
);

CREATE TABLE utilisateur_role (
    id_utilisateur INTEGER NOT NULL REFERENCES utilisateur(id) ON DELETE CASCADE,
    id_role INTEGER NOT NULL REFERENCES role(id) ON DELETE CASCADE,
    PRIMARY KEY (id_utilisateur, id_role)
);

CREATE TABLE journal_action (
    id SERIAL PRIMARY KEY,
    id_utilisateur INTEGER NOT NULL REFERENCES utilisateur(id),
    module VARCHAR(50) NOT NULL,
    action VARCHAR(100) NOT NULL,
    entite_cible VARCHAR(100),
    description TEXT,
    date_heure TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ------------------------------------------------------------
-- MODULE 1 : BASSINS
-- ------------------------------------------------------------

CREATE TABLE statut_bassin (
    id SERIAL PRIMARY KEY,
    code VARCHAR(30) NOT NULL UNIQUE,  -- VIDE, PREPARATION, ACTIF, EN_TRAITEMENT, RECOLTE, QUARANTAINE
    libelle VARCHAR(50) NOT NULL
);

CREATE TABLE bassin (
    id SERIAL PRIMARY KEY,
    code VARCHAR(20) NOT NULL UNIQUE,  -- B01 ... B09
    surface_m2 DECIMAL(10,2) NOT NULL,
    profondeur_metre DECIMAL(4,2) NOT NULL,
    notes TEXT,
    id_statut_actuel INTEGER NOT NULL REFERENCES statut_bassin(id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE histo_statut_bassin (
    id SERIAL PRIMARY KEY,
    id_bassin INTEGER NOT NULL REFERENCES bassin(id) ON DELETE CASCADE,
    id_statut_bassin INTEGER NOT NULL REFERENCES statut_bassin(id),
    id_utilisateur INTEGER NOT NULL REFERENCES utilisateur(id),
    motif TEXT,
    date_changement TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ------------------------------------------------------------
-- MODULE 2 : ESPÈCES
-- ------------------------------------------------------------

CREATE TABLE espece_crevette (
    id SERIAL PRIMARY KEY,
    nom_scientifique VARCHAR(100) NOT NULL,
    nom_courant VARCHAR(50) NOT NULL
);

CREATE TABLE evolution_hebdo_espece (
    id SERIAL PRIMARY KEY,
    id_espece INTEGER NOT NULL REFERENCES espece_crevette(id) ON DELETE CASCADE,
    semaine INTEGER NOT NULL,
    poids_cible_g DECIMAL(10,2) NOT NULL,
    taille_cible_mm DECIMAL(10,2) NOT NULL,
    UNIQUE(id_espece, semaine)
);

-- ------------------------------------------------------------
-- MODULE 3 : CYCLE & ASSOCIATIONS BASSINS
-- ------------------------------------------------------------

CREATE TABLE cycle (
    id SERIAL PRIMARY KEY,
    code_unique_cycle VARCHAR(50) NOT NULL UNIQUE,  -- ex: C01
    id_espece INTEGER NOT NULL REFERENCES espece_crevette(id),
    id_technicien INTEGER REFERENCES utilisateur(id),
    date_debut DATE NOT NULL,
    date_fin_prevue DATE NOT NULL,
    est_cloture BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE cycle_bassin_assoc (
    id SERIAL PRIMARY KEY,
    id_cycle INTEGER NOT NULL REFERENCES cycle(id) ON DELETE CASCADE,
    id_bassin INTEGER NOT NULL REFERENCES bassin(id),
    effectif_initial INTEGER NOT NULL,
    densite_m2 DECIMAL(10,2),
    cout_post_larves DECIMAL(15,2) NOT NULL,
    poids_moyen_actuel DECIMAL(10,2) DEFAULT 0,
    semaine_actuelle INTEGER DEFAULT 0,
    date_fin_reelle DATE,
    est_cloture BOOLEAN NOT NULL DEFAULT FALSE,
    UNIQUE(id_cycle, id_bassin)
);

CREATE UNIQUE INDEX idx_bassin_unique_cycle_actif
    ON cycle_bassin_assoc(id_bassin) WHERE est_cloture = FALSE;

-- ------------------------------------------------------------
-- MODULE 4 : SUIVI HEBDOMADAIRE
-- ------------------------------------------------------------

CREATE TABLE suivi_hebdo_bassin (
    id SERIAL PRIMARY KEY,
    id_cycle_bassin_assoc INTEGER NOT NULL REFERENCES cycle_bassin_assoc(id) ON DELETE CASCADE,
    date_suivi DATE NOT NULL DEFAULT CURRENT_DATE,
    semaine_actuelle INTEGER NOT NULL,
    poids_moyen_gramme DECIMAL(6,2) NOT NULL,
    taille_moyenne_mm DECIMAL(10,2) NOT NULL,
    nb_vivants INTEGER NOT NULL,
    nb_morts INTEGER NOT NULL DEFAULT 0,
    biomasse_calculee_kg DECIMAL(10,2)
        GENERATED ALWAYS AS ((nb_vivants * poids_moyen_gramme / 1000)) STORED,
    id_technicien INTEGER NOT NULL REFERENCES utilisateur(id),
    notes TEXT
);

CREATE OR REPLACE VIEW v_suivi_hebdo_bassin AS
SELECT
    s.*,
    cba.id_bassin,
    cba.id_cycle,
    ROUND((s.nb_vivants::DECIMAL / cba.effectif_initial * 100), 2) AS taux_survie_calcule,
    ROUND((s.nb_morts::DECIMAL  / cba.effectif_initial * 100), 2) AS taux_mortalite_calcule
FROM suivi_hebdo_bassin s
JOIN cycle_bassin_assoc cba ON cba.id = s.id_cycle_bassin_assoc;

-- ------------------------------------------------------------
-- MODULE 5 : NOURRISSAGE (MULTI-LOTS)
-- ------------------------------------------------------------

CREATE TABLE creneau_horaire (
    id SERIAL PRIMARY KEY,
    libelle VARCHAR(20) NOT NULL UNIQUE,  -- MATIN, MIDI, SOIR, NUIT
    ordre INTEGER NOT NULL
);

CREATE TABLE aliment (
    id SERIAL PRIMARY KEY,
    libelle VARCHAR(100) NOT NULL,
    seuil_minimum_kg DECIMAL(10,2) NOT NULL DEFAULT 0
);

CREATE TABLE entree_stock_aliment (
    id SERIAL PRIMARY KEY,
    id_aliment INTEGER NOT NULL REFERENCES aliment(id),
    quantite_kg DECIMAL(10,2) NOT NULL CHECK (quantite_kg > 0),
    quantite_restante_kg DECIMAL(10,2) NOT NULL CHECK (quantite_restante_kg >= 0),
    prix_unitaire_ar DECIMAL(15,2) NOT NULL CHECK (prix_unitaire_ar >= 0),
    prix_total_ar DECIMAL(15,2) GENERATED ALWAYS AS (quantite_kg * prix_unitaire_ar) STORED,
    date_reception DATE NOT NULL DEFAULT CURRENT_DATE,
    date_expiration DATE NOT NULL,
    id_responsable INTEGER NOT NULL REFERENCES utilisateur(id),
    CONSTRAINT check_dates_aliment CHECK (date_expiration >= date_reception)
);

CREATE TABLE distribution_nourriture (
    id SERIAL PRIMARY KEY,
    id_cycle_bassin_assoc INTEGER NOT NULL REFERENCES cycle_bassin_assoc(id) ON DELETE CASCADE,
    id_aliment INTEGER NOT NULL REFERENCES aliment(id),
    id_creneau INTEGER NOT NULL REFERENCES creneau_horaire(id),
    date_distribution DATE NOT NULL DEFAULT CURRENT_DATE,
    heure_nourrissage TIME,
    quantite_prevue_kg DECIMAL(10,2) NOT NULL CHECK (quantite_prevue_kg >= 0),
    quantite_donnee_kg DECIMAL(10,2) NOT NULL DEFAULT 0 CHECK (quantite_donnee_kg >= 0),
    id_responsable INTEGER NOT NULL REFERENCES utilisateur(id),
    statut VARCHAR(20) NOT NULL DEFAULT 'EN_ATTENTE'
        CHECK (statut IN ('EN_ATTENTE', 'NOURRI', 'RETARD', 'RUPTURE')),
    est_valide BOOLEAN NOT NULL DEFAULT FALSE,
    UNIQUE(id_cycle_bassin_assoc, date_distribution, id_creneau)
);

-- TABLE INTERMÉDIAIRE : Associe un repas à 1 ou plusieurs lots de nourriture
CREATE TABLE distribution_nourriture_lot (
    id SERIAL PRIMARY KEY,
    id_distribution INTEGER NOT NULL REFERENCES distribution_nourriture(id) ON DELETE CASCADE,
    id_entree_aliment INTEGER NOT NULL REFERENCES entree_stock_aliment(id),
    quantite_piquee_kg DECIMAL(10,2) NOT NULL CHECK (quantite_piquee_kg > 0),
    UNIQUE(id_distribution, id_entree_aliment)
);

CREATE TABLE mouvement_stock_aliment (
    id SERIAL PRIMARY KEY,
    id_entree_aliment INTEGER NOT NULL REFERENCES entree_stock_aliment(id) ON DELETE CASCADE,
    type_mouvement VARCHAR(20) NOT NULL,  -- PERTE, DESTRUCTION, AJUSTEMENT
    quantite_kg DECIMAL(10,2) NOT NULL CHECK (quantite_kg > 0),
    motif TEXT NOT NULL,
    date_mouvement TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    id_utilisateur INTEGER NOT NULL REFERENCES utilisateur(id)
);

-- ------------------------------------------------------------
-- MODULE 6 : SANITAIRE (MULTI-LOTS)
-- ------------------------------------------------------------

CREATE TABLE medicament (
    id SERIAL PRIMARY KEY,
    libelle VARCHAR(100) NOT NULL,
    unite VARCHAR(20) NOT NULL DEFAULT 'kg',
    seuil_minimum DECIMAL(10,2) NOT NULL DEFAULT 0
);

CREATE TABLE entree_stock_medicament (
    id SERIAL PRIMARY KEY,
    id_medicament INTEGER NOT NULL REFERENCES medicament(id),
    quantite DECIMAL(10,2) NOT NULL CHECK (quantite > 0),
    quantite_restante DECIMAL(10,2) NOT NULL CHECK (quantite_restante >= 0),
    prix_total_ar DECIMAL(15,2) NOT NULL CHECK (prix_total_ar >= 0),
    date_reception DATE NOT NULL DEFAULT CURRENT_DATE,
    date_expiration DATE NOT NULL,
    id_responsable INTEGER NOT NULL REFERENCES utilisateur(id),
    CONSTRAINT check_dates_med CHECK (date_expiration >= date_reception)
);

CREATE TABLE incident_sanitaire (
    id SERIAL PRIMARY KEY,
    id_cycle_bassin_assoc INTEGER NOT NULL REFERENCES cycle_bassin_assoc(id) ON DELETE CASCADE,
    date_detection DATE NOT NULL DEFAULT CURRENT_DATE,
    type_incident VARCHAR(30) NOT NULL,  -- MALADIE, ANOMALIE_EAU, MORTALITE_ANORMALE, AUTRE
    description TEXT NOT NULL,
    niveau_gravite VARCHAR(20) NOT NULL,  -- FAIBLE, MODERE, CRITIQUE
    id_responsable INTEGER NOT NULL REFERENCES utilisateur(id),
    est_resolu BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Entête du traitement clinique
CREATE TABLE traitement (
    id SERIAL PRIMARY KEY,
    id_incident INTEGER NOT NULL REFERENCES incident_sanitaire(id) ON DELETE CASCADE,
    id_medicament INTEGER NOT NULL REFERENCES medicament(id),
    dosage VARCHAR(100) NOT NULL,
    duree_jours INTEGER NOT NULL CHECK (duree_jours > 0),
    date_debut DATE NOT NULL,
    quantite_utilisee DECIMAL(10,2) NOT NULL DEFAULT 0 CHECK (quantite_utilisee >= 0),
    id_responsable INTEGER NOT NULL REFERENCES utilisateur(id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- TABLE INTERMÉDIAIRE : Associe un traitement à 1 ou plusieurs lots de médicaments
CREATE TABLE traitement_medicament_lot (
    id SERIAL PRIMARY KEY,
    id_traitement INTEGER NOT NULL REFERENCES traitement(id) ON DELETE CASCADE,
    id_entree_medicament INTEGER NOT NULL REFERENCES entree_stock_medicament(id),
    quantite_piquee DECIMAL(10,2) NOT NULL CHECK (quantite_piquee > 0),
    UNIQUE(id_traitement, id_entree_medicament)
);

CREATE TABLE mouvement_stock_medicament (
    id SERIAL PRIMARY KEY,
    id_entree_medicament INTEGER NOT NULL REFERENCES entree_stock_medicament(id) ON DELETE CASCADE,
    type_mouvement VARCHAR(20) NOT NULL,  -- PERTE, DESTRUCTION, AJUSTEMENT
    quantite DECIMAL(10,2) NOT NULL CHECK (quantite > 0),
    motif TEXT NOT NULL,
    date_mouvement TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    id_responsable INTEGER NOT NULL REFERENCES utilisateur(id)
);

-- ------------------------------------------------------------
-- MODULE 7 : STOCK CREVETTES (RÉCOLTE) & INVENTAIRE
-- ------------------------------------------------------------

CREATE TABLE lot_crevette (
    id SERIAL PRIMARY KEY,
    numero_lot_unique VARCHAR(50) NOT NULL UNIQUE,  -- ex: LOT-B01-2026
    id_cycle_bassin_assoc INTEGER NOT NULL REFERENCES cycle_bassin_assoc(id),
    biomasse_totale_kg DECIMAL(10,2) NOT NULL,
    biomasse_actuelle_kg DECIMAL(10,2) NOT NULL,
    poids_moyen_final_g DECIMAL(10,2) NOT NULL,
    taille_moyenne_finale_mm DECIMAL(10,2) NOT NULL,
    date_recolte DATE NOT NULL DEFAULT CURRENT_DATE,
    id_responsable INTEGER NOT NULL REFERENCES utilisateur(id)
);

CREATE TABLE mouvement_stock_crevette (
    id SERIAL PRIMARY KEY,
    id_lot_crevette INTEGER NOT NULL REFERENCES lot_crevette(id) ON DELETE CASCADE,
    type_mouvement VARCHAR(20) NOT NULL,  -- PERTE, DESTRUCTION, AJUSTEMENT
    quantite_kg DECIMAL(10,2) NOT NULL CHECK (quantite_kg > 0),
    motif TEXT NOT NULL,
    date_mouvement TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    id_utilisateur INTEGER NOT NULL REFERENCES utilisateur(id)
);

CREATE TABLE inventaire (
    id SERIAL PRIMARY KEY,
    type_produit VARCHAR(20) NOT NULL,  -- ALIMENT, MEDICAMENT, CREVETTE
    id_aliment INTEGER REFERENCES aliment(id),
    id_medicament INTEGER REFERENCES medicament(id),
    id_lot_crevette INTEGER REFERENCES lot_crevette(id),
    stock_theorique DECIMAL(10,2) NOT NULL,
    stock_reel DECIMAL(10,2) NOT NULL,
    ecart DECIMAL(10,2) GENERATED ALWAYS AS (stock_reel - stock_theorique) STORED,
    id_responsable INTEGER NOT NULL REFERENCES utilisateur(id),
    date_inventaire TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_un_seul_produit CHECK (
        (id_aliment IS NOT NULL)::INT +
        (id_medicament IS NOT NULL)::INT +
        (id_lot_crevette IS NOT NULL)::INT = 1
    )
);

-- ------------------------------------------------------------
-- MODULE 8 : ALERTES CONSOLIDÉES
-- ------------------------------------------------------------

CREATE TABLE alerte (
    id SERIAL PRIMARY KEY,
    type_alerte VARCHAR(50) NOT NULL,
    niveau VARCHAR(10) NOT NULL,  -- ORANGE, ROUGE
    module_source VARCHAR(30) NOT NULL,
    id_cycle_bassin_assoc INTEGER REFERENCES cycle_bassin_assoc(id),
    message TEXT NOT NULL,
    est_resolue BOOLEAN NOT NULL DEFAULT FALSE,
    date_creation TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_resolution TIMESTAMP,
    id_resolu_par INTEGER REFERENCES utilisateur(id)
);

-- ------------------------------------------------------------
-- TRIGGERS
-- ------------------------------------------------------------

CREATE OR REPLACE FUNCTION fn_decrement_stock_aliment()
RETURNS TRIGGER AS $$
BEGIN
    UPDATE entree_stock_aliment
    SET quantite_restante_kg = quantite_restante_kg - NEW.quantite_piquee_kg
    WHERE id = NEW.id_entree_aliment;

    IF (SELECT quantite_restante_kg FROM entree_stock_aliment WHERE id = NEW.id_entree_aliment) < 0 THEN
        RAISE EXCEPTION 'Stock aliment insuffisant pour le lot ID %', NEW.id_entree_aliment;
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_decrement_stock_aliment
    AFTER INSERT ON distribution_nourriture_lot
    FOR EACH ROW
    EXECUTE FUNCTION fn_decrement_stock_aliment();

CREATE OR REPLACE FUNCTION fn_decrement_stock_medicament()
RETURNS TRIGGER AS $$
BEGIN
    UPDATE entree_stock_medicament
    SET quantite_restante = quantite_restante - NEW.quantite_piquee
    WHERE id = NEW.id_entree_medicament;

    IF (SELECT quantite_restante FROM entree_stock_medicament WHERE id = NEW.id_entree_medicament) < 0 THEN
        RAISE EXCEPTION 'Stock médicament insuffisant pour le lot ID %', NEW.id_entree_medicament;
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_decrement_stock_medicament
    AFTER INSERT ON traitement_medicament_lot
    FOR EACH ROW
    EXECUTE FUNCTION fn_decrement_stock_medicament();

CREATE OR REPLACE FUNCTION fn_decrement_stock_crevette()
RETURNS TRIGGER AS $$
BEGIN
    UPDATE lot_crevette
    SET biomasse_actuelle_kg = biomasse_actuelle_kg - NEW.quantite_kg
    WHERE id = NEW.id_lot_crevette;

    IF (SELECT biomasse_actuelle_kg FROM lot_crevette WHERE id = NEW.id_lot_crevette) < 0 THEN
        RAISE EXCEPTION 'Stock crevette insuffisant pour ce mouvement';
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_decrement_stock_crevette
    AFTER INSERT ON mouvement_stock_crevette
    FOR EACH ROW
    EXECUTE FUNCTION fn_decrement_stock_crevette();

CREATE OR REPLACE FUNCTION fn_quarantaine_auto()
RETURNS TRIGGER AS $$
DECLARE
    id_statut_quarantaine INTEGER;
    id_bassin_concerne INTEGER;
BEGIN
    IF NEW.niveau_gravite = 'CRITIQUE' THEN
        SELECT id INTO id_statut_quarantaine FROM statut_bassin WHERE code = 'QUARANTAINE';
        SELECT id_bassin INTO id_bassin_concerne
            FROM cycle_bassin_assoc WHERE id = NEW.id_cycle_bassin_assoc;

        UPDATE bassin
        SET id_statut_actuel = id_statut_quarantaine
        WHERE id = id_bassin_concerne;

        INSERT INTO histo_statut_bassin (id_bassin, id_statut_bassin, id_utilisateur, motif)
        VALUES (id_bassin_concerne, id_statut_quarantaine, NEW.id_responsable,
                'Quarantaine automatique - incident critique #' || NEW.id);
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_quarantaine_auto
    AFTER INSERT ON incident_sanitaire
    FOR EACH ROW
    EXECUTE FUNCTION fn_quarantaine_auto();

-- ------------------------------------------------------------
-- DONNÉES DE RÉFÉRENCE
-- ------------------------------------------------------------

INSERT INTO statut_bassin (code, libelle) VALUES
('VIDE',         'Vide'),
('PREPARATION',  'Préparation'),
('ACTIF',        'Actif'),
('EN_TRAITEMENT','En traitement'),
('RECOLTE',      'Récolté'),
('QUARANTAINE',  'Quarantaine');

INSERT INTO creneau_horaire (libelle, ordre) VALUES
('MATIN', 1), ('MIDI', 2), ('SOIR', 3), ('NUIT', 4);

INSERT INTO espece_crevette (nom_scientifique, nom_courant) VALUES
('Fenneropenaeus indicus', 'Crevette blanche');

INSERT INTO evolution_hebdo_espece (id_espece, semaine, poids_cible_g, taille_cible_mm)
SELECT e.id, v.semaine, v.poids, v.taille
FROM espece_crevette e
CROSS JOIN (VALUES
    ( 1,  0.50,   8.00),
    ( 2,  1.00,  12.00),
    ( 3,  1.80,  18.00),
    ( 4,  2.80,  25.00),
    ( 5,  4.00,  32.00),
    ( 6,  5.50,  40.00),
    ( 7,  7.00,  48.00),
    ( 8,  8.50,  55.00),
    ( 9, 10.00,  62.00),
    (10, 11.50,  70.00),
    (11, 13.00,  78.00),
    (12, 14.50,  85.00),
    (13, 16.00,  95.00),
    (14, 17.50, 105.00),
    (15, 19.00, 112.00),
    (16, 20.00, 120.00)
) AS v(semaine, poids, taille)
WHERE e.nom_courant = 'Crevette blanche';

-- ------------------------------------------------------------
-- AUTHENTIFICATION
-- ------------------------------------------------------------

INSERT INTO role (code, libelle) VALUES
('ADMIN', 'Administrateur'),
('DIR', 'Directeur'),
('TECH', 'Technicien'),
('RS', 'Responsable sanitaire')
ON CONFLICT (code) DO NOTHING;

INSERT INTO utilisateur (nom, prenom, email, mot_de_passe, statut)
VALUES ('Admin', 'OROSE', 'admin@baovola.mg', 'admin123', 'ACTIF')
ON CONFLICT (email) DO NOTHING;

INSERT INTO utilisateur_role (id_utilisateur, id_role)
SELECT u.id, r.id
FROM utilisateur u
JOIN role r ON r.code = 'ADMIN'
WHERE u.email = 'admin@baovola.mg'
ON CONFLICT DO NOTHING;

-- ------------------------------------------------------------
-- PROCÉDURES
-- ------------------------------------------------------------

CREATE OR REPLACE PROCEDURE pr_valider_nourrissage_direct(
    p_id_distribution INT,
    p_id_utilisateur INT
) AS $$
DECLARE
    v_id_cycle_bassin_assoc INT;
    v_date_distribution DATE;
    v_id_creneau INT;
    v_creneau_ordre INT;
    v_id_aliment INT;
    v_quantite_prevue DECIMAL(10,2);
    v_quantite_a_retirer DECIMAL(10,2);
    v_stock_global_dispo DECIMAL(10,2);
    v_lot_rec RECORD;
    v_quantite_piquee DECIMAL(10,2);
    v_creneau_libelle VARCHAR(20);
    v_heure_actuelle TIME;
    v_heure_valide BOOLEAN := FALSE;
    v_creneau_retard_bloquant VARCHAR(20);
BEGIN
    v_heure_actuelle := CURRENT_TIME;

    -- 1. Récupérer les infos du repas ciblé ainsi que l'ordre de son créneau et sa date
    SELECT
        dn.id_cycle_bassin_assoc, dn.date_distribution, dn.id_creneau, ch.ordre,
        dn.id_aliment, dn.quantite_prevue_kg, ch.libelle
    INTO
        v_id_cycle_bassin_assoc, v_date_distribution, v_id_creneau, v_creneau_ordre,
        v_id_aliment, v_quantite_prevue, v_creneau_libelle
    FROM distribution_nourriture dn
    JOIN creneau_horaire ch ON ch.id = dn.id_creneau
    WHERE dn.id = p_id_distribution AND dn.statut IN ('EN_ATTENTE', 'RETARD');

    IF NOT FOUND THEN
        RAISE EXCEPTION 'Distribution introuvable, déjà validée ou non éligible (ID: %)', p_id_distribution;
    END IF;

    -- RÈGLE MÉTIER : sécurité de chronologie (même journée uniquement)
    SELECT ch_sub.libelle INTO v_creneau_retard_bloquant
    FROM distribution_nourriture dn_sub
    JOIN creneau_horaire ch_sub ON ch_sub.id = dn_sub.id_creneau
    WHERE dn_sub.id_cycle_bassin_assoc = v_id_cycle_bassin_assoc
      AND dn_sub.date_distribution = v_date_distribution
      AND ch_sub.ordre < v_creneau_ordre
      AND dn_sub.est_valide = FALSE
    ORDER BY ch_sub.ordre ASC
    LIMIT 1;

    IF v_creneau_retard_bloquant IS NOT NULL THEN
        RAISE EXCEPTION 'Action refusée : Impossible de valider le repas du % car le repas précédent du % n''a pas encore été validé pour ce bassin aujourd''hui.',
            v_creneau_libelle, v_creneau_retard_bloquant;
    END IF;

    -- 2. Vérification de la plage horaire de début
    CASE v_creneau_libelle
        WHEN 'MATIN' THEN
            IF v_heure_actuelle >= '06:00:00'::TIME THEN v_heure_valide := TRUE; END IF;
        WHEN 'MIDI' THEN
            IF v_heure_actuelle >= '11:00:00'::TIME THEN v_heure_valide := TRUE; END IF;
        WHEN 'SOIR' THEN
            IF v_heure_actuelle >= '17:00:00'::TIME THEN v_heure_valide := TRUE; END IF;
        WHEN 'NUIT' THEN
            IF v_heure_actuelle >= '22:00:00'::TIME OR v_heure_actuelle < '06:00:00'::TIME THEN v_heure_valide := TRUE; END IF;
    END CASE;

    IF NOT v_heure_valide THEN
        RAISE EXCEPTION 'Action refusée : Impossible de valider le repas du % de manière anticipée. Heure actuelle : %',
            v_creneau_libelle, TO_CHAR(v_heure_actuelle, 'HH24:MI');
    END IF;

    -- 3. Vérification stricte du stock global disponible
    SELECT COALESCE(SUM(quantite_restante_kg), 0) INTO v_stock_global_dispo
    FROM entree_stock_aliment
    WHERE id_aliment = v_id_aliment AND quantite_restante_kg > 0;

    IF v_stock_global_dispo < v_quantite_prevue THEN
        RAISE EXCEPTION 'Action impossible : Stock insuffisant pour cet aliment. Requis : % kg, Disponible : % kg.',
            v_quantite_prevue, v_stock_global_dispo;
    END IF;

    -- 4. Mettre à jour l'entête
    UPDATE distribution_nourriture
    SET
        quantite_donnee_kg = v_quantite_prevue,
        heure_nourrissage = v_heure_actuelle,
        statut = 'NOURRI',
        est_valide = TRUE,
        id_responsable = p_id_utilisateur
    WHERE id = p_id_distribution;

    -- 5. Déstockage multi-lots (FEFO)
    v_quantite_a_retirer := v_quantite_prevue;

    FOR v_lot_rec IN
        SELECT id, quantite_restante_kg
        FROM entree_stock_aliment
        WHERE id_aliment = v_id_aliment AND quantite_restante_kg > 0
        ORDER BY date_expiration ASC, id ASC
    LOOP
        EXIT WHEN v_quantite_a_retirer <= 0;

        IF v_lot_rec.quantite_restante_kg >= v_quantite_a_retirer THEN
            v_quantite_piquee := v_quantite_a_retirer;
            v_quantite_a_retirer := 0;
        ELSE
            v_quantite_piquee := v_lot_rec.quantite_restante_kg;
            v_quantite_a_retirer := v_quantite_a_retirer - v_lot_rec.quantite_restante_kg;
        END IF;

        INSERT INTO distribution_nourriture_lot (id_distribution, id_entree_aliment, quantite_piquee_kg)
        VALUES (p_id_distribution, v_lot_rec.id, v_quantite_piquee);
    END LOOP;

END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION fn_obtenir_ou_creer_planning_du_jour(id_utilisateur_connecte INT)
RETURNS TABLE (
    id_bassin INT,
    code_bassin VARCHAR,
    note_bassin TEXT,
    id_creneau INT,
    creneau_libelle VARCHAR,
    id_distribution INT,
    date_distribution DATE,
    heure_prevue TIME,
    heure_nourrissage TIME,
    quantite_prevue_kg DECIMAL(10,2),
    quantite_donnee_kg DECIMAL(10,2),
    statut_distribution VARCHAR,
    est_valide BOOLEAN
) AS $$
DECLARE
    bassin_rec RECORD;
    creneau_rec RECORD;
    ration_totale_bassin_kg DECIMAL(10,2);
    ration_par_creneau_kg DECIMAL(10,2);
    nb_creneaux INT;
    v_id_aliment INT;
BEGIN
    -- Optimisation du comptage des créneaux
    SELECT COUNT(*) INTO nb_creneaux FROM creneau_horaire;
    IF nb_creneaux = 0 THEN nb_creneaux := 4; END IF;

    -- 1. GÉNÉRATION : Uniquement pour les bassins ACTIF ou EN_TRAITEMENT avec un cycle ouvert
    FOR bassin_rec IN 
        SELECT b.id AS id_bassin, cba.id AS id_cycle_bassin_assoc, b.code
        FROM bassin b
        JOIN cycle_bassin_assoc cba ON b.id = cba.id_bassin
        JOIN statut_bassin sb ON b.id_statut_actuel = sb.id
        WHERE cba.est_cloture = FALSE 
          AND sb.code IN ('ACTIF', 'EN_TRAITEMENT')
    LOOP
        -- Sélection de l'aliment
        SELECT dn_sub.id_aliment INTO v_id_aliment
        FROM distribution_nourriture dn_sub
        WHERE dn_sub.id_cycle_bassin_assoc = bassin_rec.id_cycle_bassin_assoc
        ORDER BY dn_sub.date_distribution DESC, dn_sub.id DESC LIMIT 1;

        IF v_id_aliment IS NULL THEN
            SELECT id_aliment INTO v_id_aliment
            FROM entree_stock_aliment
            WHERE quantite_restante_kg > 0
            ORDER BY date_expiration ASC LIMIT 1;
        END IF;

        IF v_id_aliment IS NULL THEN v_id_aliment := 1; END IF;

        -- Vérification et insertion du planning du jour
        IF NOT EXISTS (
            SELECT 1 FROM distribution_nourriture dn_check
            WHERE dn_check.id_cycle_bassin_assoc = bassin_rec.id_cycle_bassin_assoc 
              AND dn_check.date_distribution = CURRENT_DATE
        ) THEN
            
            SELECT COALESCE(sh.biomasse_calculee_kg, 0) INTO ration_totale_bassin_kg
            FROM suivi_hebdo_bassin sh
            WHERE sh.id_cycle_bassin_assoc = bassin_rec.id_cycle_bassin_assoc
            ORDER BY sh.date_suivi DESC, sh.id DESC LIMIT 1;

            IF ration_totale_bassin_kg > 0 THEN
                ration_totale_bassin_kg := ration_totale_bassin_kg * 0.03;
            ELSE
                ration_totale_bassin_kg := 10.00; 
            END IF;
            ration_par_creneau_kg := ROUND(ration_totale_bassin_kg / nb_creneaux, 2);

            FOR creneau_rec IN SELECT ch_sub.id, ch_sub.libelle FROM creneau_horaire ch_sub ORDER BY ch_sub.ordre LOOP
                INSERT INTO distribution_nourriture (
                    id_cycle_bassin_assoc, id_aliment, id_creneau,
                    date_distribution, heure_nourrissage,
                    quantite_prevue_kg, quantite_donnee_kg, id_responsable,
                    statut, est_valide
                ) VALUES (
                    bassin_rec.id_cycle_bassin_assoc, v_id_aliment, creneau_rec.id,
                    CURRENT_DATE, NULL,
                    ration_par_creneau_kg, 0, id_utilisateur_connecte,
                    'EN_ATTENTE', FALSE
                ) ON CONFLICT DO NOTHING;
            END LOOP;
        END IF;
    END LOOP;

    -- 2. SÉLECTION, CALCUL ET LOGIQUE DE BRIDAGE DES HORAIRES
    -- Principe : heure_theorique = heure_reelle_precedente + decalage
    --            puis on borne strictement heure_theorique dans [borne_min, borne_max] du CRENEAU COURANT.
    -- On travaille en INTERVAL (pas TIME) pour la NUIT afin d'éviter le bug de rebouclage à minuit :
    --   Nuit va de 22:00:00 à 25:00:00 (= 01:00:00 le lendemain, exprimé sans wraparound).
    RETURN QUERY
    WITH planning_brut AS (
        SELECT 
            b.id AS b_id, b.code AS b_code, b.notes AS b_notes,
            ch.id AS ch_id, ch.libelle AS ch_libelle, ch.ordre AS ch_ordre,
            dn.id AS dn_id, dn.date_distribution AS dn_date, 
            dn.heure_nourrissage AS dn_heure_reelle,
            dn.quantite_prevue_kg AS dn_prevu, dn.quantite_donnee_kg AS dn_donne,
            dn.statut AS dn_statut, dn.est_valide AS dn_valide,
            -- Décalage ajouté à l'heure réelle du créneau précédent pour obtenir l'heure théorique du créneau courant
            CASE ch.libelle
                WHEN 'MATIN' THEN '00:00:00'::INTERVAL
                WHEN 'MIDI'  THEN '05:00:00'::INTERVAL
                WHEN 'SOIR'  THEN '06:00:00'::INTERVAL
                WHEN 'NUIT'  THEN '05:00:00'::INTERVAL
            END AS decalage,
            -- Borne basse du créneau courant (= heure par défaut si aucun créneau précédent validé)
            CASE ch.libelle
                WHEN 'MATIN' THEN '06:00:00'::INTERVAL
                WHEN 'MIDI'  THEN '11:00:00'::INTERVAL
                WHEN 'SOIR'  THEN '17:00:00'::INTERVAL
                WHEN 'NUIT'  THEN '22:00:00'::INTERVAL
            END AS borne_min,
            -- Borne haute du créneau courant (dernière heure possible du créneau)
            -- NUIT = 25:00:00 (soit 01:00:00 le lendemain) pour éviter le rebouclage à minuit
            CASE ch.libelle
                WHEN 'MATIN' THEN '10:00:00'::INTERVAL
                WHEN 'MIDI'  THEN '14:00:00'::INTERVAL
                WHEN 'SOIR'  THEN '20:00:00'::INTERVAL
                WHEN 'NUIT'  THEN '25:00:00'::INTERVAL
            END AS borne_max,
            LAG(dn.heure_nourrissage) OVER (PARTITION BY b.id ORDER BY ch.ordre) AS heure_reelle_precedente,
            LAG(dn.date_distribution) OVER (PARTITION BY b.id ORDER BY ch.ordre) AS date_reelle_precedente
        FROM bassin b
        JOIN statut_bassin sb ON b.id_statut_actuel = sb.id
        JOIN cycle_bassin_assoc cba ON b.id = cba.id_bassin AND cba.est_cloture = FALSE
        CROSS JOIN creneau_horaire ch
        LEFT JOIN distribution_nourriture dn ON dn.id_cycle_bassin_assoc = cba.id 
            AND dn.id_creneau = ch.id 
            AND dn.date_distribution = CURRENT_DATE
        WHERE sb.code IN ('ACTIF', 'EN_TRAITEMENT')
    ),
    planning_avec_dates_calculees AS (
        SELECT 
            p.b_id, p.b_code, p.b_notes, p.ch_id, p.ch_libelle, p.ch_ordre, p.dn_id,
            COALESCE(p.date_reelle_precedente, p.dn_date) AS date_base,
            -- Intervalle cible (borné), SANS cast en TIME : on garde la valeur brute (peut aller jusqu'à 25h pour NUIT)
            -- afin de ne perdre aucune information de débordement sur le jour suivant.
            CASE 
                WHEN p.ch_libelle = 'MATIN' THEN p.borne_min
                WHEN p.heure_reelle_precedente IS NOT NULL THEN 
                    LEAST(
                        GREATEST(p.heure_reelle_precedente::INTERVAL + p.decalage, p.borne_min),
                        p.borne_max
                    )
                ELSE p.borne_min
            END AS interval_cible,
            p.dn_heure_reelle, p.dn_prevu, p.dn_donne, p.dn_statut, p.dn_valide
        FROM planning_brut p
    ),
    planning_final AS (
        SELECT
            pc.*,
            -- ts_calcule : pour l'AFFICHAGE. On garde volontairement la même date calendaire
            -- (date_base + heure seule, sans jour supplémentaire) car la ligne NUIT doit rester
            -- rattachée visuellement à la date du jour, même si l'heure réelle est après minuit.
            (pc.date_base + pc.interval_cible::TIME)::TIMESTAMP AS ts_calcule,
            -- ts_reel_cible : pour le calcul du RETARD. Ici on NE tronque PAS l'intervalle en TIME :
            -- si interval_cible = 25:00:00 (NUIT), le timestamp déborde correctement sur le lendemain.
            -- Ça évite le bug où toute heure entre 00:00 et 01:00 était marquée RETARD par erreur,
            -- même quand le créneau NUIT du jour n'avait pas encore commencé.
            (pc.date_base::TIMESTAMP + pc.interval_cible) AS ts_reel_cible
        FROM planning_avec_dates_calculees pc
    )
    SELECT 
        b_id, b_code, b_notes, ch_id, ch_libelle, dn_id,
        (ts_calcule)::DATE AS date_distribution,
        (ts_calcule)::TIME AS heure_prevue,
        dn_heure_reelle, dn_prevu, dn_donne,
        CASE 
            WHEN dn_statut = 'EN_ATTENTE' AND LOCALTIMESTAMP > ts_reel_cible THEN 'RETARD'
            ELSE dn_statut
        END AS statut_distribution,
        dn_valide
    FROM planning_final
    WHERE (ts_calcule)::DATE = CURRENT_DATE
    ORDER BY b_code, ch_ordre;
END;
$$ LANGUAGE plpgsql;


CREATE OR REPLACE PROCEDURE pr_enregistrer_entree_stock(
    p_id_aliment INT,
    p_quantite_kg DECIMAL(10,2),
    p_prix_unitaire_ar DECIMAL(15,2),
    p_date_reception DATE,
    p_date_expiration DATE,
    p_id_utilisateur INT
) AS $$
BEGIN
    IF p_quantite_kg <= 0 THEN
        RAISE EXCEPTION 'La quantité reçue doit être strictement supérieure à 0 kg.';
    END IF;

    IF p_prix_unitaire_ar < 0 THEN
        RAISE EXCEPTION 'Le prix unitaire ne peut pas être négatif.';
    END IF;

    IF p_date_expiration < p_date_reception THEN
        RAISE EXCEPTION 'La date d''expiration (%) ne peut pas être antérieure à la date de réception (%).',
            TO_CHAR(p_date_expiration, 'DD/MM/YYYY'), TO_CHAR(p_date_reception, 'DD/MM/YYYY');
    END IF;

    INSERT INTO entree_stock_aliment (
        id_aliment,
        quantite_kg,
        quantite_restante_kg,
        prix_unitaire_ar,
        date_reception,
        date_expiration,
        id_responsable
    ) VALUES (
        p_id_aliment,
        p_quantite_kg,
        p_quantite_kg,
        p_prix_unitaire_ar,
        p_date_reception,
        p_date_expiration,
        p_id_utilisateur
    );

END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE PROCEDURE pr_enregistrer_distribution_manuelle(
    p_code_bassin VARCHAR(20),
    p_id_aliment INT,
    p_quantite_kg DECIMAL(10,2),
    p_id_utilisateur INT,
    p_date_distribution DATE,
    p_heure_prevue TIME
) AS $$
DECLARE
    v_id_cycle_bassin_assoc INT;
    v_id_distribution INT;
    v_statut_actuel VARCHAR(20);
    v_creneau_libelle VARCHAR(20);
BEGIN
    SELECT cba.id INTO v_id_cycle_bassin_assoc
    FROM cycle_bassin_assoc cba
    JOIN bassin b ON b.id = cba.id_bassin
    WHERE b.code = p_code_bassin AND cba.est_cloture = FALSE;

    IF v_id_cycle_bassin_assoc IS NULL THEN
        RAISE EXCEPTION 'Impossible d''enregistrer : Aucun cycle actif trouvé pour le bassin %.', p_code_bassin;
    END IF;

    SELECT dn.id, dn.statut, ch.libelle
    INTO v_id_distribution, v_statut_actuel, v_creneau_libelle
    FROM distribution_nourriture dn
    JOIN creneau_horaire ch ON dn.id_creneau = ch.id
    WHERE dn.id_cycle_bassin_assoc = v_id_cycle_bassin_assoc
      AND dn.date_distribution = p_date_distribution
    ORDER BY ABS(
        EXTRACT(EPOCH FROM (
            CASE ch.libelle
                WHEN 'MATIN' THEN '06:00:00'::TIME
                WHEN 'MIDI'  THEN '11:00:00'::TIME
                WHEN 'SOIR'  THEN '17:00:00'::TIME
                WHEN 'NUIT'  THEN '22:00:00'::TIME
                ELSE '06:00:00'::TIME
            END - p_heure_prevue
        ))
    ) ASC
    LIMIT 1;

    IF v_id_distribution IS NOT NULL THEN
        IF v_statut_actuel = 'NOURRI' THEN
            RAISE EXCEPTION 'Erreur : Le repas identifié (%) a déjà été validé et distribué sur le terrain. Modification interdite.',
                v_creneau_libelle;
        ELSE
            UPDATE distribution_nourriture
            SET
                id_aliment = p_id_aliment,
                quantite_prevue_kg = p_quantite_kg,
                id_responsable = p_id_utilisateur
            WHERE id = v_id_distribution;
        END IF;
    ELSE
        RAISE EXCEPTION 'Erreur : Aucune planification trouvée pour cette journée. Veuillez d''abord générer le planning.';
    END IF;

END;
$$ LANGUAGE plpgsql;

-- ============================================================
-- DONNÉES DE TEST
-- Cycles : C01 (30/06→30/10/2026), C02 (30/09/2026→30/01/2027), C03 (30/07→30/11/2026)
-- Répartition : C01=B01,B02,B03 / C02=B04,B05,B06 / C03=B07,B08,B09
-- ============================================================

-- 1. Aliment de référence
INSERT INTO aliment (libelle, seuil_minimum_kg)
VALUES ('Granules Croissance Elevee', 50.00)
ON CONFLICT DO NOTHING;

-- 2. Bassins B01 à B09
INSERT INTO bassin (code, surface_m2, profondeur_metre, notes, id_statut_actuel)
SELECT v.code, v.surface, v.profondeur, NULL,
       (SELECT id FROM statut_bassin WHERE code = 'ACTIF')
FROM (VALUES
    ('B01', 1000.00, 1.50),
    ('B02', 1000.00, 1.50),
    ('B03', 1000.00, 1.50),
    ('B04', 1200.00, 1.60),
    ('B05', 1200.00, 1.60),
    ('B06', 1200.00, 1.60),
    ('B07',  900.00, 1.40),
    ('B08',  900.00, 1.40),
    ('B09',  900.00, 1.40)
) AS v(code, surface, profondeur)
ON CONFLICT (code) DO NOTHING;

-- 3. Cycles C01, C02, C03
INSERT INTO cycle (code_unique_cycle, id_espece, id_technicien, date_debut, date_fin_prevue, est_cloture)
SELECT v.code, (SELECT id FROM espece_crevette WHERE nom_courant = 'Crevette blanche'),
       (SELECT id FROM utilisateur WHERE email = 'admin@baovola.mg'),
       v.date_debut::DATE, v.date_fin::DATE, FALSE
FROM (VALUES
    ('C01', '2026-06-30', '2026-10-30'),
    ('C02', '2026-09-30', '2027-01-30'),
    ('C03', '2026-07-30', '2026-11-30')
) AS v(code, date_debut, date_fin)
ON CONFLICT (code_unique_cycle) DO NOTHING;

-- 4. Associations cycle <-> bassin (cycle_bassin_assoc)
INSERT INTO cycle_bassin_assoc (id_cycle, id_bassin, effectif_initial, densite_m2, cout_post_larves)
SELECT (SELECT id FROM cycle WHERE code_unique_cycle = v.cycle),
       (SELECT id FROM bassin WHERE code = v.bassin),
       v.effectif, v.densite, v.cout
FROM (VALUES
    ('C01', 'B01', 50000, 50.00, 1500000.00),
    ('C01', 'B02', 50000, 50.00, 1500000.00),
    ('C01', 'B03', 45000, 45.00, 1350000.00),
    ('C02', 'B04', 45000, 37.50, 1350000.00),
    ('C02', 'B05', 60000, 50.00, 1800000.00),
    ('C02', 'B06', 60000, 50.00, 1800000.00),
    ('C03', 'B07', 40000, 44.44, 1200000.00),
    ('C03', 'B08', 40000, 44.44, 1200000.00),
    ('C03', 'B09', 40000, 44.44, 1200000.00)
) AS v(cycle, bassin, effectif, densite, cout)
ON CONFLICT (id_cycle, id_bassin) DO NOTHING;

-- ------------------------------------------------------------
-- VÉRIFICATION : récupère les vrais id_cycle_bassin_assoc avant de continuer
-- ------------------------------------------------------------

SELECT cba.id AS id_cycle_bassin_assoc, b.code AS bassin, c.code_unique_cycle AS cycle,
       cba.effectif_initial, c.date_debut, c.date_fin_prevue
FROM cycle_bassin_assoc cba
JOIN bassin b ON b.id = cba.id_bassin
JOIN cycle c ON c.id = cba.id_cycle
ORDER BY c.code_unique_cycle, b.code;

-- ------------------------------------------------------------
-- MODULE 2 — PESÉES (dates recalées sur C01 : début 30/06/2026)
-- NB : on récupère les id_cycle_bassin_assoc dynamiquement par code bassin,
-- ce qui évite de dépendre de numéros fixes (1,2,3...) qui pouvaient ne pas exister.
-- ------------------------------------------------------------

-- --- Bassin B01 — cycle C01 ---
INSERT INTO suivi_hebdo_bassin
    (id_cycle_bassin_assoc, date_suivi, semaine_actuelle, poids_moyen_gramme, taille_moyenne_mm, nb_vivants, nb_morts, id_technicien, notes)
SELECT cba.id, v.date_suivi::DATE, v.semaine, v.poids, v.taille, v.vivants, v.morts, 1, v.notes
FROM cycle_bassin_assoc cba
JOIN bassin b ON b.id = cba.id_bassin
CROSS JOIN (VALUES
    ('2026-07-07', 1,  0.5,   8.0,  49800,  200, 'Pesee S1'),
    ('2026-07-28', 4,  2.8,  25.0,  49200,  600, 'Pesee S4'),
    ('2026-08-25', 8,  8.5,  55.0,  48500,  700, 'Pesee S8'),
    ('2026-09-22', 12, 14.5, 85.0,  47800,  700, 'Pesee S12'),
    ('2026-10-20', 16, 20.0, 120.0, 47000,  800, 'Pesee S16 - calibre commercial atteint')
) AS v(date_suivi, semaine, poids, taille, vivants, morts, notes)
WHERE b.code = 'B01';

-- --- Bassin B02 — cycle C01 ---
INSERT INTO suivi_hebdo_bassin
    (id_cycle_bassin_assoc, date_suivi, semaine_actuelle, poids_moyen_gramme, taille_moyenne_mm, nb_vivants, nb_morts, id_technicien, notes)
SELECT cba.id, v.date_suivi::DATE, v.semaine, v.poids, v.taille, v.vivants, v.morts, 1, v.notes
FROM cycle_bassin_assoc cba
JOIN bassin b ON b.id = cba.id_bassin
CROSS JOIN (VALUES
    ('2026-07-07', 1,  0.5,  8.0,  49700, 300, 'Pesee S1'),
    ('2026-08-11', 6,  5.5,  40.0, 48900, 800, 'Pesee S6'),
    ('2026-09-08', 10, 11.5, 70.0, 48200, 700, 'Pesee S10'),
    ('2026-10-06', 14, 17.5, 105.0,47500, 700, 'Pesee S14')
) AS v(date_suivi, semaine, poids, taille, vivants, morts, notes)
WHERE b.code = 'B02';

-- --- Bassin B03 — cycle C01, bassin test quarantaine ---
INSERT INTO suivi_hebdo_bassin
    (id_cycle_bassin_assoc, date_suivi, semaine_actuelle, poids_moyen_gramme, taille_moyenne_mm, nb_vivants, nb_morts, id_technicien, notes)
SELECT cba.id, v.date_suivi::DATE, v.semaine, v.poids, v.taille, v.vivants, v.morts, 1, v.notes
FROM cycle_bassin_assoc cba
JOIN bassin b ON b.id = cba.id_bassin
CROSS JOIN (VALUES
    ('2026-07-07', 1, 0.5, 8.0,  44800, 200,  'Pesee S1'),
    ('2026-08-04', 5, 4.0, 32.0, 43000, 1800, 'Pesee S5 - mortalite elevee constatee')
) AS v(date_suivi, semaine, poids, taille, vivants, morts, notes)
WHERE b.code = 'B03';

-- --- Bassin B04 — cycle C02 (début 30/09/2026) ---
INSERT INTO suivi_hebdo_bassin
    (id_cycle_bassin_assoc, date_suivi, semaine_actuelle, poids_moyen_gramme, taille_moyenne_mm, nb_vivants, nb_morts, id_technicien, notes)
SELECT cba.id, v.date_suivi::DATE, v.semaine, v.poids, v.taille, v.vivants, v.morts, 1, v.notes
FROM cycle_bassin_assoc cba
JOIN bassin b ON b.id = cba.id_bassin
CROSS JOIN (VALUES
    ('2026-10-07', 1,  0.5,  8.0,   44700, 300, 'Pesee S1'),
    ('2026-10-28', 4,  2.8,  25.0,  44100, 600, 'Pesee S4'),
    ('2026-11-25', 8,  8.5,  55.0,  43500, 600, 'Pesee S8'),
    ('2026-12-23', 12, 14.5, 85.0,  42900, 600, 'Pesee S12'),
    ('2027-01-20', 16, 20.0, 120.0, 42200, 700, 'Pesee S16 - calibre commercial atteint')
) AS v(date_suivi, semaine, poids, taille, vivants, morts, notes)
WHERE b.code = 'B04';

-- --- Bassin B05 — cycle C02, bassin test EN_TRAITEMENT ---
INSERT INTO suivi_hebdo_bassin
    (id_cycle_bassin_assoc, date_suivi, semaine_actuelle, poids_moyen_gramme, taille_moyenne_mm, nb_vivants, nb_morts, id_technicien, notes)
SELECT cba.id, v.date_suivi::DATE, v.semaine, v.poids, v.taille, v.vivants, v.morts, 1, v.notes
FROM cycle_bassin_assoc cba
JOIN bassin b ON b.id = cba.id_bassin
CROSS JOIN (VALUES
    ('2026-10-07', 1,  0.5,  8.0,  59700, 300, 'Pesee S1'),
    ('2026-11-11', 6,  5.5,  40.0, 58800, 900, 'Pesee S6')
) AS v(date_suivi, semaine, poids, taille, vivants, morts, notes)
WHERE b.code = 'B05';
-- -> place le bassin B05 en EN_TRAITEMENT entre S6 et S10 via l'interface (test manuel),
--    puis relance l'INSERT ci-dessous pour S10 une fois repassé en ACTIF :
-- INSERT INTO suivi_hebdo_bassin
--     (id_cycle_bassin_assoc, date_suivi, semaine_actuelle, poids_moyen_gramme, taille_moyenne_mm, nb_vivants, nb_morts, id_technicien, notes)
-- SELECT cba.id, '2026-12-09', 10, 11.5, 70.0, 58000, 800, 1, 'Pesee S10 - reprise apres traitement'
-- FROM cycle_bassin_assoc cba JOIN bassin b ON b.id = cba.id_bassin WHERE b.code = 'B05';

-- --- Bassin B06 — cycle C02 ---
INSERT INTO suivi_hebdo_bassin
    (id_cycle_bassin_assoc, date_suivi, semaine_actuelle, poids_moyen_gramme, taille_moyenne_mm, nb_vivants, nb_morts, id_technicien, notes)
SELECT cba.id, v.date_suivi::DATE, v.semaine, v.poids, v.taille, v.vivants, v.morts, 1, v.notes
FROM cycle_bassin_assoc cba
JOIN bassin b ON b.id = cba.id_bassin
CROSS JOIN (VALUES
    ('2026-10-07', 1,  0.5,  8.0,   59800, 200,  'Pesee S1'),
    ('2026-11-25', 8,  8.5,  55.0,  58600, 1200, 'Pesee S8'),
    ('2026-12-30', 14, 17.5, 105.0, 57800, 800,  'Pesee S14'),
    ('2027-01-13', 16, 20.0, 120.0, 57500, 300,  'Pesee S16 - calibre commercial atteint')
) AS v(date_suivi, semaine, poids, taille, vivants, morts, notes)
WHERE b.code = 'B06';

-- Vérification rapide : biomasse calculée auto + taux survie/mortalité via la vue
SELECT id_cycle_bassin_assoc, date_suivi, semaine_actuelle, poids_moyen_gramme,
       biomasse_calculee_kg, taux_survie_calcule, taux_mortalite_calcule
FROM v_suivi_hebdo_bassin
ORDER BY id_cycle_bassin_assoc, date_suivi;

-- ------------------------------------------------------------
-- ALIMENT + STOCK MULTI-LOTS
-- ------------------------------------------------------------

INSERT INTO entree_stock_aliment
    (id_aliment, quantite_kg, quantite_restante_kg, prix_unitaire_ar, date_reception, date_expiration, id_responsable)
VALUES
    -- Lot 1 : reçu le premier, expire le plus tôt -> doit être consommé en premier (FEFO)
    ((SELECT id FROM aliment WHERE libelle = 'Granules Croissance Elevee'),
     500.00, 500.00, 2200.00, '2026-07-02', '2027-01-01', 1),

    -- Lot 2 : reçu ensuite, expire après le lot 1
    ((SELECT id FROM aliment WHERE libelle = 'Granules Croissance Elevee'),
     800.00, 800.00, 2100.00, '2026-08-01', '2027-04-01', 1),

    -- Lot 3 : reçu en dernier, mais expire le plus tard
    ((SELECT id FROM aliment WHERE libelle = 'Granules Croissance Elevee'),
     600.00, 600.00, 2300.00, '2026-09-01', '2027-09-15', 1);

-- Vérification stock global + ordre FEFO attendu
SELECT id, quantite_kg, quantite_restante_kg, prix_unitaire_ar, date_reception, date_expiration
FROM entree_stock_aliment
WHERE id_aliment = (SELECT id FROM aliment WHERE libelle = 'Granules Croissance Elevee')
ORDER BY date_expiration ASC;


UPDATE cycle_bassin_assoc cba
SET
    poids_moyen_actuel = derniere.poids_moyen_gramme,
    semaine_actuelle    = derniere.semaine_actuelle
FROM (
    SELECT DISTINCT ON (id_cycle_bassin_assoc)
        id_cycle_bassin_assoc,
        poids_moyen_gramme,
        semaine_actuelle
    FROM suivi_hebdo_bassin
    ORDER BY id_cycle_bassin_assoc, date_suivi DESC, id DESC
) AS derniere
WHERE cba.id = derniere.id_cycle_bassin_assoc;

-- ── Vérification ──────────────────────────────────────────────
SELECT
    b.code AS bassin,
    c.code_unique_cycle AS cycle,
    cba.semaine_actuelle,
    cba.poids_moyen_actuel
FROM cycle_bassin_assoc cba
JOIN bassin b ON b.id = cba.id_bassin
JOIN cycle c  ON c.id = cba.id_cycle
ORDER BY c.code_unique_cycle, b.code;
