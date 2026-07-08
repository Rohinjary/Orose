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