-- Création de la base de données si elle n'existe pas déjà
CREATE DATABASE IF NOT EXISTS lastbd;

-- Utilisation de la base de données
USE lastbd;

-- Table des réservations
CREATE TABLE IF NOT EXISTS reservation (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nom_passager VARCHAR(100) NOT NULL,
    date_reservation DATE NOT NULL,
    point_depart VARCHAR(100) NOT NULL,
    point_arrivee VARCHAR(100) NOT NULL,
    statut VARCHAR(50) NOT NULL DEFAULT 'En attente',
    type_trajet VARCHAR(50) NOT NULL,
    commentaire TEXT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Table des abonnements
CREATE TABLE IF NOT EXISTS abonnement (
    id_abonnement INT AUTO_INCREMENT PRIMARY KEY,
    date_debut DATE NOT NULL,
    date_fin DATE,
    reservation_id INT NOT NULL,
    montant DECIMAL(10,2) NOT NULL,
    type_abonnement VARCHAR(50) NOT NULL,
    statut VARCHAR(50) NOT NULL DEFAULT 'Actif',
    remarques TEXT,
    FOREIGN KEY (reservation_id) REFERENCES reservation(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Insertion de quelques données de test pour les réservations
INSERT INTO reservation (nom_passager, date_reservation, point_depart, point_arrivee, statut, type_trajet, commentaire)
VALUES

    ('Leila Mansour', DATE_SUB(CURDATE(), INTERVAL 2 DAY), 'Tabarka', 'Hammamet', 'Confirmée', 'Aller-retour', 'Non-fumeur'),
    ('Jean Dupont', '2025-05-12', 'Paris', 'Lyon', 'Confirmée', 'Aller simple', 'Départ à 7h00'),
    ('Marie Martin', '2025-05-17', 'Marseille', 'Nice', 'En attente', 'Aller-retour', 'Préfère voiture climatisée'),
    ('Pierre Durand', '2025-05-22', 'Bordeaux', 'Toulouse', 'Confirmée', 'Aller simple', 'Max 2 valises'),
    ('Sophie Leroy', '2025-05-14', 'Lille', 'Paris', 'En attente', 'Aller-retour', 'Avec un enfant'),
    ('Thomas Bernard', '2025-05-19', 'Strasbourg', 'Lyon', 'Confirmée', 'Aller simple', 'Max 2 valises');

-- Insertion de quelques données de test pour les abonnements
INSERT INTO abonnement (date_debut, date_fin, reservation_id, montant, type_abonnement, statut, remarques)
VALUES
    (CURDATE(), DATE_ADD(CURDATE(), INTERVAL 30 DAY), 1, 120.00, 'Mensuel', 'Actif', 'Premier abonnement'),
    (CURDATE(), DATE_ADD(CURDATE(), INTERVAL 90 DAY), 2, 300.00, 'Trimestriel', 'Actif', 'Paiement en 3 fois'),
    (DATE_SUB(CURDATE(), INTERVAL 60 DAY), DATE_SUB(CURDATE(), INTERVAL 30 DAY), 3, 120.00, 'Mensuel', 'Expiré', 'Non renouvelé'),
    (CURDATE(), DATE_ADD(CURDATE(), INTERVAL 180 DAY), 4, 550.00, 'Semestriel', 'Actif', 'Tarif étudiant'),
    (CURDATE(), DATE_ADD(CURDATE(), INTERVAL 365 DAY), 5, 1000.00, 'Annuel', 'Actif', 'Prix spécial'); 