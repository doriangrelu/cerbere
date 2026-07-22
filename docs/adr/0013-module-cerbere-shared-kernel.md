# ADR 0013 — Module `cerbere-shared-kernel`

## Statut

Accepted

## Contexte

[ADR 0010](0010-module-contract-partage-autorise.md) autorisait un module Maven partagé pour les enveloppes/DTOs, sous un nom provisoire (`cerbere-contract`), à créer seulement quand un second service en aurait besoin. Le client a créé ce module en avance de cerbere-core sous le nom `cerbere-shared-kernel` (groupId `fr.cerbere.shared`), et a élargi son périmètre : au-delà des DTOs/enveloppes, il doit aussi porter les **configurations Spring communes** dupliquées à l'identique entre services (mapper Jackson tolérant, sécurité `permitAll` temporaire).

## Décision

- Le module s'appelle **`cerbere-shared-kernel`**, package racine `fr.cerbere.shared` (distinct de `fr.cerbere.component.<module>`).
- Sans parent Spring Boot (comme les autres modules), mais avec un `dependencyManagement` important le BOM `spring-boot-dependencies:4.1.0` pour aligner les versions sans hériter du parent — cohérent avec [ADR 0009](0009-pom-agregateur-racine-optionnel.md).
- Contenu déplacé depuis `cerbere-devices-mock` dans cette itération :
  - `fr.cerbere.shared.event.EventEnvelope` (ex `DeviceEventEnvelope`, généralisé — c'est l'enveloppe commune à **tous** les événements Cerbère, pas seulement ceux des devices).
  - `fr.cerbere.shared.kafka.JacksonEventSerializer` (ex `DeviceEventEnvelopeSerializer`, généralisé — sérialiseur Kafka Jackson 3 réutilisable par tout producteur, contournement documenté en [ADR 0012](0012-serializer-kafka-jackson3-maison.md)).
  - `fr.cerbere.shared.config.CommonJacksonConfig` (ex `MapperConfig`) et `fr.cerbere.shared.config.PermitAllSecurityConfig` (ex `SecurityConfig`) — configurations `@Configuration` communes, à importer explicitement (`@Import(...)`) depuis chaque application (le scan de composants Spring ne traverse pas les modules).
- `cerbere-devices-mock` dépend maintenant de `fr.cerbere.shared:cerbere-shared-kernel:1.0-SNAPSHOT`. **Contrainte de build locale** : en l'absence de pom agrégateur racine, il faut `cd cerbere-shared-kernel && ./mvnw install` (ou consommer la version publiée sur GitHub Packages) avant de builder un module qui en dépend.
- Le workflow `.github/workflows/publish-packages.yml` publie `cerbere-shared-kernel` dans un job dédié, exécuté **avant** (via `needs:`) le job matriciel des 6 autres services, pour garantir que la dépendance est résolvable au moment de leur `deploy`.

## Conséquences

- Fin de la duplication du mapper Jackson et de la config sécurité `permitAll` : un seul endroit à faire évoluer (notamment lors du branchement de Keycloak, [ADR 0007](0007-authentification-keycloak-phase-2.md), qui remplacera `PermitAllSecurityConfig` module par module).
- Couplage de build assumé entre `cerbere-shared-kernel` et ses consommateurs (cf. conséquences déjà actées en [ADR 0010](0010-module-contract-partage-autorise.md)) : toute évolution incompatible nécessite de republier le module puis de mettre à jour la dépendance côté consommateurs.
- `EventEnvelope`/`JacksonEventSerializer` seront réutilisés tels quels par `cerbere-core`, `cerbere-history`, `cerbere-notification` dès leur implémentation — pas de nouvelle duplication à prévoir pour ces trois topics.
