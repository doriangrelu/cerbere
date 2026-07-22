package fr.cerbere.shared.dto.devicemock;

import java.util.UUID;

/**
 * Requête d'enregistrement d'un nouveau device simulé. {@code type} doit
 * correspondre à une constante de {@code DeviceType} (CONTACT, MOTION, SIREN).
 * Contrat REST partagé entre {@code cerbere-bff} (émetteur) et
 * {@code cerbere-devices-mock} (récepteur).
 */
public record RegisterSimulatedDeviceRequest(
        String id,
        boolean autoSimulate
) {
}
