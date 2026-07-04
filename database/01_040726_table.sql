-- ============================================================
-- BASE JAONA : Schéma final consolidé OROSE (VERSION MODIFIÉE)
-- Modifications principales :
--   1) Suppression de nb_vivants et nb_morts de suivi_hebdo_bassin
--   2) Création table recolte_declaration pour déclaration récolte
--   3) Intégration calcul automatique taux survie et perte
-- ============================================================

-- Important : assurez-vous que le client psql utilise l'encodage UTF8
-- (la base "orose" doit aussi avoir été créée avec ENCODING 'UTF8').

-- ============================================================
-- MODULE 0 : UTILISATEURS & RÔLES
-- ============================================================

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


-- ============================================================
-- MODULE 1 : BASSINS
-- ============================================================

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


-- ============================================================
-- MODULE 2 : ESPÈCES
-- ============================================================

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

-- ============================================================
-- MODULE 3 : CYCLE & ASSOCIATIONS BASSINS
-- ============================================================

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


-- ============================================================
-- MODULE 4 : SUIVI HEBDOMADAIRE (MODIFIÉ)
-- ============================================================
-- MODIFICATION : Suppression de nb_vivants, nb_morts et biomasse_calculee_kg
-- Ces données sont maintenant collectées uniquement à la récolte

CREATE TABLE suivi_hebdo_bassin (
    id SERIAL PRIMARY KEY,
    id_cycle_bassin_assoc INTEGER NOT NULL REFERENCES cycle_bassin_assoc(id) ON DELETE CASCADE,
    date_suivi DATE NOT NULL DEFAULT CURRENT_DATE,
    semaine_actuelle INTEGER NOT NULL,
    poids_moyen_gramme DECIMAL(6,2) NOT NULL,
    taille_moyenne_mm DECIMAL(10,2) NOT NULL,
    id_technicien INTEGER NOT NULL REFERENCES utilisateur(id),
    notes TEXT
);

-- Vue modifiée sans calcul de taux survie (à déplacer à recolte_declaration)
CREATE OR REPLACE VIEW v_suivi_hebdo_bassin AS
SELECT
    s.*,
    cba.id_bassin,
    cba.id_cycle,
    cba.effectif_initial
FROM suivi_hebdo_bassin s
JOIN cycle_bassin_assoc cba ON cba.id = s.id_cycle_bassin_assoc;


-- ============================================================
-- MODULE 5 : RÉCOLTE & DÉCLARATION (NOUVEAU)
-- ============================================================
-- Nouvelle table pour capturer la récolte et calculer les indicateurs
-- RÈGLES DE CALCUL :
--   - recolte_estimee_kg = effectif_initial * 0.020 (20g par post-larve)
--   - perte_kg = recolte_estimee_kg - recolte_reelle_kg
--   - taux_survie_percent = (recolte_reelle_kg / recolte_estimee_kg) * 100

-- ============================================================
-- MODULE 5 : RÉCOLTE & DÉCLARATION (CORRIGÉ)
-- ============================================================


CREATE TABLE recolte_declaration (
    id SERIAL PRIMARY KEY,
    id_cycle_bassin_assoc INTEGER NOT NULL REFERENCES cycle_bassin_assoc(id),
    recolte_estimee_kg DECIMAL(10,2) NOT NULL,
    recolte_reelle_kg DECIMAL(10,2) NOT NULL,
    perte_kg DECIMAL(10,2) GENERATED ALWAYS AS (recolte_estimee_kg - recolte_reelle_kg) STORED,
    taux_survie_percent DECIMAL(5,2) GENERATED ALWAYS AS (
        CASE WHEN recolte_estimee_kg > 0 THEN (recolte_reelle_kg * 100 / recolte_estimee_kg) ELSE 0 END
    ) STORED,
    date_declaration DATE NOT NULL DEFAULT CURRENT_DATE,
    id_responsable INTEGER NOT NULL REFERENCES utilisateur(id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_recolte_cycle_bassin ON recolte_declaration(id_cycle_bassin_assoc);


-- ============================================================
-- MODULE 6 : NOURRISSAGE (MULTI-LOTS)
-- ============================================================

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


-- ============================================================
-- MODULE 7 : SANITAIRE (MULTI-LOTS)
-- ============================================================

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


-- ============================================================
-- MODULE 8 : STOCK CREVETTES (RÉCOLTE) & INVENTAIRE
-- ============================================================

CREATE TABLE lot_crevette (
    id SERIAL PRIMARY KEY,
    numero_lot_unique VARCHAR(50) NOT NULL UNIQUE,
    id_recolte_declaration INTEGER NOT NULL REFERENCES recolte_declaration(id),
    poids_moyen_final_g DECIMAL(10,2) NOT NULL DEFAULT 0,
    taille_moyenne_finale_mm DECIMAL(10,2) NOT NULL DEFAULT 0,
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


-- ============================================================
-- MODULE 9 : ALERTES CONSOLIDÉES
-- ============================================================

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

-- ============================================================
-- TRIGGERS ET MISES À JOUR AUTOMATIQUES
-- ============================================================

-- Mise à jour de cycle_bassin_assoc après nouvelle pesée
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
