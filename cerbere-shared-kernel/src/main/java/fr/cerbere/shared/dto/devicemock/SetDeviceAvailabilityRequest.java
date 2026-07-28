package fr.cerbere.shared.dto.devicemock;

/**
 * Requête de branchement/débranchement d'un device simulé du réseau (voir
 * ADR 0024) : {@code online=false} simule un device absent (pile vide, hors de
 * portée, débranché), qui cesse toute émission MQTT. Contrat REST partagé entre
 * {@code cerbere-bff} (émetteur) et {@code cerbere-devices-mock} (récepteur).
 */
public record SetDeviceAvailabilityRequest(boolean online) {
}
