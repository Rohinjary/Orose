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
    statut VARCHAR(20) NOT NULL DEFAULT 'ACTIF' -- ACTIF, INACTIF
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
    code VARCHAR(30) NOT NULL UNIQUE,  -- PREPARATION, ACTIF, EN_TRAITEMENT, RECOLTE, QUARANTAINE
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
-- MODULE 2 : CYCLE & BIOLOGIQUE
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

CREATE TABLE cycle_bassin (
    id SERIAL PRIMARY KEY,
    code_unique_cycle VARCHAR(50) NOT NULL UNIQUE,  -- B01-C01-2026
    id_bassin INTEGER NOT NULL REFERENCES bassin(id),
    id_espece INTEGER NOT NULL REFERENCES espece_crevette(id),
    effectif_initial INTEGER NOT NULL,
    cout_post_larves DECIMAL(15,2) NOT NULL,
    densite_m2 DECIMAL(10,2),
    id_technicien INTEGER REFERENCES utilisateur(id),
    date_debut DATE NOT NULL,
    date_fin_prevue DATE NOT NULL,
    date_fin_reelle DATE,
    poids_moyen_actuel DECIMAL(10,2) DEFAULT 0,
    semaine_actuelle INTEGER DEFAULT 0,
    est_cloture BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Un bassin actif = un seul cycle non clôturé
CREATE UNIQUE INDEX idx_cycle_unique_actif ON cycle_bassin(id_bassin) WHERE est_cloture = FALSE;

CREATE TABLE suivi_hebdo_bassin (
    id SERIAL PRIMARY KEY,
    id_cycle INTEGER NOT NULL REFERENCES cycle_bassin(id) ON DELETE CASCADE,
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

-- Vue avec calculs additionnels : taux survie, taux mortalité, semaine calculée
CREATE VIEW v_suivi_hebdo_bassin AS
SELECT
    s.*,
    ROUND((s.nb_vivants::DECIMAL / c.effectif_initial * 100), 2) AS taux_survie_calcule,
    ROUND((s.nb_morts::DECIMAL / c.effectif_initial * 100), 2)   AS taux_mortalite_calcule
FROM suivi_hebdo_bassin s
JOIN cycle_bassin c ON c.id = s.id_cycle;

-- ------------------------------------------------------------
-- MODULE 3 : NOURRISSAGE
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
    quantite_kg DECIMAL(10,2) NOT NULL,
    quantite_restante_kg DECIMAL(10,2) NOT NULL,
    prix_unitaire_ar DECIMAL(15,2) NOT NULL,
    prix_total_ar DECIMAL(15,2) NOT NULL,
    date_reception DATE NOT NULL DEFAULT CURRENT_DATE,
    date_expiration DATE NOT NULL,
    id_responsable INTEGER NOT NULL REFERENCES utilisateur(id)
);

CREATE TABLE distribution_nourriture (
    id SERIAL PRIMARY KEY,
    id_cycle INTEGER NOT NULL REFERENCES cycle_bassin(id) ON DELETE CASCADE,
    id_entree_aliment INTEGER NOT NULL REFERENCES entree_stock_aliment(id),
    id_creneau INTEGER NOT NULL REFERENCES creneau_horaire(id),
    date_distribution DATE NOT NULL DEFAULT CURRENT_DATE,
    quantite_prevue_kg DECIMAL(10,2) NOT NULL,
    quantite_donnee_kg DECIMAL(10,2) NOT NULL,
    id_responsable INTEGER NOT NULL REFERENCES utilisateur(id),
    statut VARCHAR(20) NOT NULL DEFAULT 'EN_ATTENTE',  -- EN_ATTENTE, NOURRI, RETARD, RUPTURE
    est_valide BOOLEAN NOT NULL DEFAULT FALSE,
    UNIQUE(id_cycle, date_distribution, id_creneau)
);

CREATE TABLE mouvement_stock_aliment (
    id SERIAL PRIMARY KEY,
    id_entree_aliment INTEGER NOT NULL REFERENCES entree_stock_aliment(id) ON DELETE CASCADE,
    type_mouvement VARCHAR(20) NOT NULL,  -- PERTE, DESTRUCTION, AJUSTEMENT
    quantite_kg DECIMAL(10,2) NOT NULL,
    motif TEXT NOT NULL,
    date_mouvement TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    id_utilisateur INTEGER NOT NULL REFERENCES utilisateur(id)
);

-- ------------------------------------------------------------
-- MODULE 4 : SANITAIRE
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
    quantite DECIMAL(10,2) NOT NULL,
    quantite_restante DECIMAL(10,2) NOT NULL,
    prix_total_ar DECIMAL(15,2) NOT NULL,
    date_reception DATE NOT NULL DEFAULT CURRENT_DATE,
    date_expiration DATE NOT NULL,
    id_responsable INTEGER NOT NULL REFERENCES utilisateur(id)
);

CREATE TABLE incident_sanitaire (
    id SERIAL PRIMARY KEY,
    id_cycle INTEGER NOT NULL REFERENCES cycle_bassin(id) ON DELETE CASCADE,
    date_detection DATE NOT NULL DEFAULT CURRENT_DATE,
    type_incident VARCHAR(30) NOT NULL,  -- MALADIE, ANOMALIE_EAU, MORTALITE_ANORMALE, AUTRE
    description TEXT NOT NULL,
    niveau_gravite VARCHAR(20) NOT NULL,  -- FAIBLE, MODERE, CRITIQUE
    id_responsable INTEGER NOT NULL REFERENCES utilisateur(id),
    est_resolu BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE traitement (
    id SERIAL PRIMARY KEY,
    id_incident INTEGER NOT NULL REFERENCES incident_sanitaire(id) ON DELETE CASCADE,
    id_entree_medicament INTEGER NOT NULL REFERENCES entree_stock_medicament(id),
    dosage VARCHAR(100) NOT NULL,
    duree_jours INTEGER NOT NULL,
    date_debut DATE NOT NULL,
    quantite_utilisee DECIMAL(10,2) NOT NULL,
    id_responsable INTEGER NOT NULL REFERENCES utilisateur(id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE mouvement_stock_medicament (
    id SERIAL PRIMARY KEY,
    id_entree_medicament INTEGER NOT NULL REFERENCES entree_stock_medicament(id) ON DELETE CASCADE,
    type_mouvement VARCHAR(20) NOT NULL,  -- PERTE, DESTRUCTION, AJUSTEMENT
    quantite DECIMAL(10,2) NOT NULL,
    motif TEXT NOT NULL,
    date_mouvement TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    id_responsable INTEGER NOT NULL REFERENCES utilisateur(id)
);

-- ------------------------------------------------------------
-- MODULE 5 : STOCK CREVETTES (RECOLTE) + INVENTAIRE
-- ------------------------------------------------------------

CREATE TABLE lot_crevette (
    id SERIAL PRIMARY KEY,
    numero_lot_unique VARCHAR(50) NOT NULL UNIQUE,  -- LOT-B01-2026
    id_cycle INTEGER NOT NULL REFERENCES cycle_bassin(id),
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
    quantite_kg DECIMAL(10,2) NOT NULL,
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
-- ALERTES CONSOLIDEES
-- ------------------------------------------------------------

CREATE TABLE alerte (
    id SERIAL PRIMARY KEY,
    type_alerte VARCHAR(50) NOT NULL,  -- MORTALITE_ANORMALE, PESEE_MANQUANTE, STOCK_CRITIQUE, etc.
    niveau VARCHAR(10) NOT NULL,  -- ORANGE, ROUGE
    module_source VARCHAR(30) NOT NULL,
    id_cycle_bassin INTEGER REFERENCES cycle_bassin(id),
    message TEXT NOT NULL,
    est_resolue BOOLEAN NOT NULL DEFAULT FALSE,
    date_creation TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_resolution TIMESTAMP,
    id_resolu_par INTEGER REFERENCES utilisateur(id)
);

-- ------------------------------------------------------------
-- TRIGGERS DE SECURITE
-- ------------------------------------------------------------

-- Décrémente le stock aliment après une distribution validée
CREATE OR REPLACE FUNCTION fn_decrement_stock_aliment()
RETURNS TRIGGER AS $$
BEGIN
    UPDATE entree_stock_aliment
    SET quantite_restante_kg = quantite_restante_kg - NEW.quantite_donnee_kg
    WHERE id = NEW.id_entree_aliment;

    IF (SELECT quantite_restante_kg FROM entree_stock_aliment WHERE id = NEW.id_entree_aliment) < 0 THEN
        RAISE EXCEPTION 'Stock aliment insuffisant pour cette distribution';
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_decrement_stock_aliment
    AFTER INSERT ON distribution_nourriture
    FOR EACH ROW
    WHEN (NEW.est_valide = TRUE)
    EXECUTE FUNCTION fn_decrement_stock_aliment();

-- Décrémente le stock médicament après un traitement
CREATE OR REPLACE FUNCTION fn_decrement_stock_medicament()
RETURNS TRIGGER AS $$
BEGIN
    UPDATE entree_stock_medicament
    SET quantite_restante = quantite_restante - NEW.quantite_utilisee
    WHERE id = NEW.id_entree_medicament;

    IF (SELECT quantite_restante FROM entree_stock_medicament WHERE id = NEW.id_entree_medicament) < 0 THEN
        RAISE EXCEPTION 'Stock médicament insuffisant pour ce traitement';
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_decrement_stock_medicament
    AFTER INSERT ON traitement
    FOR EACH ROW
    EXECUTE FUNCTION fn_decrement_stock_medicament();

-- Décrémente le stock crevette après un mouvement (perte/destruction)
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

-- Mise en quarantaine automatique si incident CRITIQUE
CREATE OR REPLACE FUNCTION fn_quarantaine_auto()
RETURNS TRIGGER AS $$
DECLARE
    id_statut_quarantaine INTEGER;
    id_bassin_concerne INTEGER;
BEGIN
    IF NEW.niveau_gravite = 'CRITIQUE' THEN
        SELECT id INTO id_statut_quarantaine FROM statut_bassin WHERE code = 'QUARANTAINE';
        SELECT id_bassin INTO id_bassin_concerne FROM cycle_bassin WHERE id = NEW.id_cycle;

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
-- DONNEES DE REFERENCE
-- ------------------------------------------------------------

INSERT INTO statut_bassin (code, libelle) VALUES
('VIDE', 'Vide'),
('PREPARATION', 'Préparation'),
('ACTIF', 'Actif'),
('EN_TRAITEMENT', 'En traitement'),
('RECOLTE', 'Récolté'),
('QUARANTAINE', 'Quarantaine');

INSERT INTO creneau_horaire (libelle, ordre) VALUES
('MATIN', 1), ('MIDI', 2), ('SOIR', 3), ('NUIT', 4);

INSERT INTO espece_crevette (nom_scientifique, nom_courant) VALUES
('Fenneropenaeus indicus', 'Crevette blanche');

-- Compte admin par défaut (mot de passe à hasher en bcrypt côté appli)
INSERT INTO utilisateur (nom, prenom, email, mot_de_passe, statut) VALUES
('Admin', 'OROSE', 'admin@baovola.mg', 'a_remplacer_par_hash_bcrypt', 'ACTIF');

-- ============================================================
-- FIN
-- ============================================================

