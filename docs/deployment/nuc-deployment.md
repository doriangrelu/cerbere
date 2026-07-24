# Déploiement sur le NUC cible

Document vivant : à mettre à jour dès que le NUC est effectivement provisionné et que les étapes ci-dessous sont vérifiées en conditions réelles (voir règle de mise à jour dans `CLAUDE.md`). **Rien dans ce document n'a encore été vérifié sur le NUC réel** — il décrit la procédure prévue, pas une procédure validée.

## Prérequis

- Docker Engine (Debian) ou Docker Desktop (Windows), avec Docker Compose v2 (`docker compose`, pas `docker-compose` v1).
- JDK 25 uniquement nécessaire si les images sont construites directement sur le NUC (`mvnw spring-boot:build-image`, voir [ADR 0008](../adr/0008-strategie-images-cloud-native-buildpacks.md)). Si les images sont construites ailleurs (poste de dev) puis transférées (`docker save`/`docker load`, ou un futur registre privé), le NUC n'a besoin que de Docker.
- Un dongle coordinateur Zigbee (déjà validé : passerelle Zigbee2MQTT — voir [docs/architecture/mqtt-zigbee-contracts.md](../architecture/mqtt-zigbee-contracts.md)), uniquement nécessaire pour le profil `hardware`.

## Séquence de déploiement

1. Construire les images (une fois par module modifié) :
   ```bash
   cd cerbere-shared-kernel && ./mvnw install -DskipTests
   cd ../cerbere-core && ./mvnw spring-boot:build-image
   cd ../cerbere-bff && ./mvnw spring-boot:build-image
   cd ../cerbere-history && ./mvnw spring-boot:build-image
   cd ../cerbere-devices-mock && ./mvnw spring-boot:build-image
   cd ../cerbere-devices-bridge && ./mvnw spring-boot:build-image
   ```
   Vérifier ensuite `docker images` : les 5 images `cerbere-*:latest` doivent apparaître (noms fixés explicitement dans chaque `pom.xml`, voir bloc `<image>` du `spring-boot-maven-plugin`).

2. Démarrer :
   - Démo/dev sans matériel réel : `docker compose --profile mock up -d` (depuis `deployment/`).
   - Matériel réel : `docker compose --profile hardware up -d`.
   - Les deux profils ne doivent **jamais** tourner simultanément : ce serait deux sources concurrentes d'événements device pour les mêmes topics Kafka.

3. `docker compose ps` pour vérifier que `mongodb`/`kafka` (et `mosquitto` en profil `hardware`) sont `healthy` avant de considérer le déploiement stable.

## Point d'attention : passthrough USB du dongle Zigbee (Debian vs Windows)

Le profil `hardware` (service `zigbee2mqtt` dans `deployment/docker-compose.yaml`) a besoin d'un accès direct au dongle coordinateur (`/dev/ttyUSB0` ou équivalent).

- **Debian : passthrough natif et simple.** Le dongle apparaît comme un périphérique série standard (`/dev/ttyUSB0`), passé au conteneur via `devices: - /dev/ttyUSB0:/dev/ttyUSB0` dans le compose (déjà en place). Aucune manipulation supplémentaire attendue.
- **Windows : pas de passthrough USB direct fiable via Docker Desktop.** Deux options :
  - (a) Faire tourner Zigbee2MQTT **nativement** hors conteneur sous Windows (installation Node.js directe), et ne containeriser que le reste (mongodb/kafka/apps/mosquitto) — le bridge `cerbere-devices-bridge` continue de parler MQTT à `localhost:1883` (mosquitto containerisé, port exposé) sans changement.
  - (b) Passer par **WSL2 + `usbipd-win`** : partager le dongle USB depuis Windows vers la distribution WSL2 (`usbipd attach`), pour que Docker Desktop (backend WSL2) puisse ensuite le voir comme un périphérique série standard.
- **Recommandation explicite : Debian est le choix le plus simple pour le profil `hardware`.** Si le NUC final tourne sous Windows, prévoir l'option (a) ou (b) ci-dessus avant la mise en service — ne pas découvrir le problème après achat/installation.

## Variables d'environnement à adapter en production

- Aucun secret n'est en dur dans le repo aujourd'hui : tous les services sont actuellement en sécurité `permitAll` (`TODO Keycloak`, voir [ADR 0007](../adr/0007-authentification-keycloak-phase-2.md)) — **rappel explicite : ne pas exposer ce déploiement sur internet en l'état**, y compris sur le NUC final tant que la phase 2 (Keycloak) n'est pas faite.
- `mosquitto.conf` (`deployment/mosquitto/mosquitto.conf`) autorise les connexions anonymes — acceptable en LAN de confiance uniquement, à durcir (utilisateur/mot de passe MQTT, TLS) avant toute exposition au-delà du réseau domestique local.
- Les URLs inter-conteneurs (`CERBERE_CORE_BASEURL`, `SPRING_KAFKA_BOOTSTRAPSERVERS`, etc.) sont déjà fixées dans `docker-compose.yaml` via variables d'environnement — rien à adapter tant que la topologie réseau compose ne change pas (pas de nom de service renommé, pas de déploiement multi-hôte).
