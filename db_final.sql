-- ============================================================
-- db_v3_mise_a_jour.sql — Schéma final consolidé OROSE CORRIGÉ
-- Intègre : Gestion multi-lots pour l'alimentation et la santé
-- ============================================================

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
-- MODULE 5 : NOURRISSAGE (MIS À JOUR MULTI-LOTS)
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
    -- Utilisation d'une colonne générée pour éviter les incohérences de calcul
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
    heure_nourrissage TIME, -- Heure effective ou planifiée du nourrissage
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
-- MODULE 6 : SANITAIRE (MIS À JOUR MULTI-LOTS)
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
    id_medicament INTEGER NOT NULL REFERENCES medicament(id), -- Quel médicament global est prescrit
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
    type_alerte VARCHAR(50) NOT NULL,  -- MORTALITE_ANORMALE, PESEE_MANQUANTE, STOCK_CRITIQUE, etc.
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
-- TRIGGERS (MIS À JOUR POUR LES TABLES INTERMÉDIAIRES)
-- ------------------------------------------------------------

-- Décrémente le stock d'aliment à partir de la table de composition par lot
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


-- Décrémente le stock de médicament à partir de la table de composition par lot
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


-- Décrémente le stock crevette après un mouvement (Inchangé)
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


-- Mise en quarantaine automatique si incident CRITIQUE (Inchangé)
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
('QUARANTAINE',  'Quarantaine'),
('INACTIF',      'Inactif');

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

INSERT INTO utilisateur (nom, prenom, email, mot_de_passe, statut) VALUES
('Admin', 'OROSE', 'admin@baovola.mg', 'a_remplacer_par_hash_bcrypt', 'ACTIF');









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
    v_heure_calcul_prevue TIME;
BEGIN
    SELECT COUNT(*) INTO nb_creneaux FROM creneau_horaire;
    IF nb_creneaux = 0 THEN nb_creneaux := 4; END IF;

    -- MODIFICATION ICI : On filtre les bassins pour ne prendre que les statuts ACTIF ou EN_TRAITEMENT
    FOR bassin_rec IN 
        SELECT b.id AS id_bassin, cba.id AS id_cycle_bassin_assoc, b.code
        FROM bassin b
        JOIN cycle_bassin_assoc cba ON b.id = cba.id_bassin
        JOIN statut_bassin sb ON b.id_statut_actuel = sb.id
        WHERE cba.est_cloture = FALSE 
          AND sb.code IN ('ACTIF', 'EN_TRAITEMENT')
    LOOP
        -- 1. SÉLECTION DE L'ALIMENT AUTOMATIQUE
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

        -- 2. VÉRIFICATION ET INSERTION
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
                
                v_heure_calcul_prevue := CASE creneau_rec.libelle
                    WHEN 'MATIN' THEN '06:00:00'::TIME
                    WHEN 'MIDI'  THEN '11:00:00'::TIME
                    WHEN 'SOIR'  THEN '17:00:00'::TIME
                    WHEN 'NUIT'  THEN '22:00:00'::TIME
                    ELSE '06:00:00'::TIME
                END;

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

    -- 3. RETOUR DU RÉSULTAT CORRIGÉ AVEC CALCUL DYNAMIQUE DU RETARD
    RETURN QUERY
    WITH planning_brut AS (
        SELECT 
            b.id AS b_id, b.code AS b_code, b.notes AS b_notes,
            ch.id AS ch_id, ch.libelle AS ch_libelle, ch.ordre AS ch_ordre,
            dn.id AS dn_id, dn.date_distribution AS dn_date, 
            dn.heure_nourrissage AS dn_heure_reelle,
            dn.quantite_prevue_kg AS dn_prevu, dn.quantite_donnee_kg AS dn_donne,
            dn.statut AS dn_statut, dn.est_valide AS dn_valide,
            CASE ch.libelle
                WHEN 'MATIN' THEN '00:00:00'::INTERVAL
                WHEN 'MIDI'  THEN '05:00:00'::INTERVAL
                WHEN 'SOIR'  THEN '06:00:00'::INTERVAL
                WHEN 'NUIT'  THEN '05:00:00'::INTERVAL
            END AS decalage
        FROM bassin b
        LEFT JOIN cycle_bassin_assoc cba ON b.id = cba.id_bassin AND cba.est_cloture = FALSE
        CROSS JOIN creneau_horaire ch
        LEFT JOIN distribution_nourriture dn ON dn.id_cycle_bassin_assoc = cba.id 
            AND dn.id_creneau = ch.id 
            AND dn.date_distribution = CURRENT_DATE
    ),
    repas_prev_reels AS (
        SELECT 
            p1.b_id, p1.ch_id,
            (SELECT p2.dn_heure_reelle FROM planning_brut p2 
             WHERE p2.b_id = p1.b_id AND p2.ch_ordre = p1.ch_ordre - 1) AS heure_reelle_precedente
        FROM planning_brut p1
    ),
    planning_avec_heures AS (
        SELECT 
            p.b_id, p.b_code, p.b_notes, p.ch_id, p.ch_libelle, p.ch_ordre, p.dn_id, p.dn_date,
            CASE 
                WHEN p.ch_libelle = 'MATIN' THEN '06:00:00'::TIME
                WHEN r.heure_reelle_precedente IS NOT NULL THEN (r.heure_reelle_precedente + p.decalage)::TIME
                ELSE 
                    CASE p.ch_libelle
                        WHEN 'MIDI' THEN '11:00:00'::TIME
                        WHEN 'SOIR' THEN '17:00:00'::TIME
                        WHEN 'NUIT' THEN '22:00:00'::TIME
                    END
            END AS calc_heure_prevue,
            p.dn_heure_reelle, p.dn_prevu, p.dn_donne, p.dn_statut, p.dn_valide
        FROM planning_brut p
        JOIN repas_prev_reels r ON p.b_id = r.b_id AND p.ch_id = r.ch_id
    )
    SELECT 
        b_id, b_code, b_notes, ch_id, ch_libelle, dn_id, dn_date,
        calc_heure_prevue AS heure_prevue,
        dn_heure_reelle, dn_prevu, dn_donne,
        -- LOGIQUE DU STATUT RETARD DYNAMIQUE :
        CASE 
            WHEN dn_statut = 'EN_ATTENTE' AND ch_libelle = 'NUIT' AND (CURRENT_TIME > calc_heure_prevue OR CURRENT_TIME <= '01:00:00'::TIME) THEN 'RETARD'
            WHEN dn_statut = 'EN_ATTENTE' AND ch_libelle != 'NUIT' AND CURRENT_TIME > calc_heure_prevue THEN 'RETARD'
            ELSE dn_statut
        END AS statut_distribution,
        dn_valide
    FROM planning_avec_heures
    ORDER BY b_code, ch_ordre;
END;
$$ LANGUAGE plpgsql;



















































CREATE OR REPLACE PROCEDURE pr_valider_nourrissage_direct(
    p_id_distribution INT,
    p_id_utilisateur INT
) AS $$
DECLARE
    v_id_aliment INT;
    v_quantite_prevue DECIMAL(10,2);
    v_quantite_a_retirer DECIMAL(10,2);
    v_stock_global_dispo DECIMAL(10,2);
    v_lot_rec RECORD;
    v_quantite_piquee DECIMAL(10,2);
    
    -- Variables pour la vérification de l'heure
    v_creneau_libelle VARCHAR(20);
    v_heure_actuelle TIME;
    v_heure_valide BOOLEAN := FALSE;
BEGIN
    v_heure_actuelle := CURRENT_TIME;

    -- 1. Récupérer l'aliment, la quantité PRÉVUE et le LIBELLÉ du créneau
    -- Autorise la sélection si le statut est 'EN_ATTENTE' OU 'RETARD'
    SELECT dn.id_aliment, dn.quantite_prevue_kg, ch.libelle 
    INTO v_id_aliment, v_quantite_prevue, v_creneau_libelle
    FROM distribution_nourriture dn
    JOIN creneau_horaire ch ON ch.id = dn.id_creneau
    WHERE dn.id = p_id_distribution AND dn.statut IN ('EN_ATTENTE', 'RETARD');

    IF NOT FOUND THEN
        RAISE EXCEPTION 'Distribution introuvable, déjà validée ou non éligible (ID: %)', p_id_distribution;
    END IF;

    -- 2. VÉRIFICATION DE LA PLAGE HORAIRE DE DÉBUT (Empêche la validation anticipée)
    -- On autorise la validation si l'heure actuelle est supérieure à l'heure d'ouverture du créneau
    CASE v_creneau_libelle
        WHEN 'MATIN' THEN
            IF v_heure_actuelle >= '06:00:00'::TIME THEN v_heure_valide := TRUE; END IF;
        WHEN 'MIDI' THEN
            IF v_heure_actuelle >= '11:00:00'::TIME THEN v_heure_valide := TRUE; END IF;
        WHEN 'SOIR' THEN
            IF v_heure_actuelle >= '17:00:00'::TIME THEN v_heure_valide := TRUE; END IF;
        WHEN 'NUIT' THEN
            -- Le créneau de NUIT commence à 22h00 et reste actif jusqu'au lendemain matin (ex: avant le MATIN à 06h00)
            IF v_heure_actuelle >= '22:00:00'::TIME OR v_heure_actuelle < '06:00:00'::TIME THEN v_heure_valide := TRUE; END IF;
    END CASE;

    IF NOT v_heure_valide THEN
        RAISE EXCEPTION 'Action refusée : Impossible de valider le repas du % de manière anticipée. Heure actuelle : %', 
            v_creneau_libelle, TO_CHAR(v_heure_actuelle, 'HH24:MI');
    END IF;

    -- 3. VÉRIFICATION STRICTE DU STOCK GLOBAL DISPONIBLE
    SELECT COALESCE(SUM(quantite_restante_kg), 0) INTO v_stock_global_dispo
    FROM entree_stock_aliment
    WHERE id_aliment = v_id_aliment AND quantite_restante_kg > 0;

    IF v_stock_global_dispo < v_quantite_prevue THEN
        RAISE EXCEPTION 'Action impossible : Stock insuffisant pour cet aliment. Requis : % kg, Disponible : % kg.', 
            v_quantite_prevue, v_stock_global_dispo;
    END IF;

    -- 4. Mettre à jour l'entête : Le statut passe systématiquement à 'NOURRI'
    UPDATE distribution_nourriture
    SET 
        quantite_donnee_kg = v_quantite_prevue,
        heure_nourrissage = v_heure_actuelle, 
        statut = 'NOURRI',
        est_valide = TRUE,
        id_responsable = p_id_utilisateur
    WHERE id = p_id_distribution;

    -- 5. Déstockage Multi-Lots (FEFO)
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













CREATE OR REPLACE PROCEDURE pr_enregistrer_entree_stock(
    p_id_aliment INT,
    p_quantite_kg DECIMAL(10,2),
    p_prix_unitaire_ar DECIMAL(15,2),
    p_date_reception DATE,
    p_date_expiration DATE, -- Requis par les contraintes de votre table db_v3
    p_id_utilisateur INT
) AS $$
BEGIN
    -- 1. Vérifications basiques de sécurité sur les données du formulaire
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

    -- 2. Insertion en base de données
    INSERT INTO entree_stock_aliment (
        id_aliment,
        quantite_kg,
        quantite_restante_kg, -- Initialement égale à la quantité reçue
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
    -- 1. Récupérer le cycle actif pour le bassin ciblé
    SELECT cba.id INTO v_id_cycle_bassin_assoc
    FROM cycle_bassin_assoc cba
    JOIN bassin b ON b.id = cba.id_bassin
    WHERE b.code = p_code_bassin AND cba.est_cloture = FALSE;

    IF v_id_cycle_bassin_assoc IS NULL THEN
        RAISE EXCEPTION 'Impossible d''enregistrer : Aucun cycle actif trouvé pour le bassin %.', p_code_bassin;
    END IF;

    -- 2. RECHERCHE DIRECTE DU CRÉNEAU LE PLUS PROCHE
    -- On trie par la différence absolue en valeur absolue entre l'heure reçue et l'heure théorique du créneau
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

    -- 3. APPLICATION DES RÈGLES MÉTIER
    IF v_id_distribution IS NOT NULL THEN
        -- Si la ligne la plus proche trouvée est déjà validée
        IF v_statut_actuel = 'NOURRI' THEN
            RAISE EXCEPTION 'Erreur : Le repas identifié (%) a déjà été validé et distribué sur le terrain. Modification interdite.', 
                v_creneau_libelle;
        ELSE
            -- Si elle est 'EN_ATTENTE' ou 'RETARD' (cas de votre MIDI), on applique l'UPDATE
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







-- 1. Insertion de l'aliment
INSERT INTO aliment (id, libelle) 
VALUES (1, 'Granulés Croissance Élevée')
ON CONFLICT (id) DO UPDATE SET libelle = EXCLUDED.libelle;

-- 2. Nettoyage des anciens stocks de test pour cet aliment
DELETE FROM entree_stock_aliment WHERE id_aliment = 1;

-- 3. Insertion des lots avec la colonne 'quantite_kg' complétée
INSERT INTO entree_stock_aliment (id, id_aliment, quantite_kg, quantite_restante_kg, date_expiration, prix_unitaire_ar, id_responsable)
VALUES 
(101, 1, 100.00, 100.00,  '2026-09-01'::DATE, 200, 1),  -- Reçu 100kg, il reste 50kg (Expire en 1er)
(102, 1, 200.00, 200.00, '2026-12-31'::DATE,200,1),  -- Reçu 200kg, il reste 200kg (Expire en 2e)
(103, 1, 150.00, 150.00, '2027-03-15'::DATE,200,1);  -- Reçu 150kg, il reste 150kg (Expire en dernier)