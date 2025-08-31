package com.myservers.backend.servers.entities;

public enum RequestStatus {
    PENDING,    // En attente d'approbation admin
    APPROVED,   // Approuvée par l'admin
    REJECTED,   // Rejetée par l'admin (remboursement automatique)
    CANCELLED   // Annulée par l'utilisateur
}
