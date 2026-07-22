package fr.cerbere.component.cerbere_core.infrastructure.persistence.mongo.device;

import fr.cerbere.component.cerbere_core.domain.model.Device;
import fr.cerbere.component.cerbere_core.domain.model.DeviceType;
import fr.cerbere.component.cerbere_core.infrastructure.mapper.CommonIdMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.UUID;

/**
 * Traduction entre le modèle de domaine {@link Device} et sa représentation Mongo.
 * La direction document → domaine reste une méthode par défaut : {@link Device}
 * se reconstruit via sa fabrique statique {@code restore(...)} (constructeur
 * privé), que MapStruct ne peut pas invoquer directement.
 */
@Mapper(componentModel = "spring", uses = CommonIdMapper.class)
public interface DeviceMapper {

    @Mapping(target = "id", source = "id", qualifiedByName = "uuidToString")
    @Mapping(target = "zoneId", source = "zoneId", qualifiedByName = "uuidToString")
    DeviceDocument toDocument(Device device);

    default Device toDomain(final DeviceDocument document) {
        final String zoneId = document.zoneId();
        return Device.restore(
                UUID.fromString(document.id()),
                DeviceType.valueOf(document.type()),
                document.label(),
                zoneId != null ? UUID.fromString(zoneId) : null,
                document.violation(),
                document.enabled()
        );
    }
}
