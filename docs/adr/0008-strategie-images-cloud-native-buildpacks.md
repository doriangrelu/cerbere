# ADR 0008 — Construction des images via Cloud Native Buildpacks

## Statut

Accepted

## Contexte

Le déploiement complet du système passera par `deployment/docker-compose.yaml`, qui devra à terme construire/lancer une image par microservice Java. Il faut décider comment ces images sont produites : Dockerfile maintenu manuellement par module, ou outillage intégré à Spring Boot.

## Décision

Utiliser **Cloud Native Buildpacks** via le goal Maven `spring-boot:build-image`, déjà disponible sans configuration supplémentaire car `spring-boot-maven-plugin` est présent dans chaque `pom.xml`. Aucun `Dockerfile` n'est écrit à la main.

## Conséquences

- Zéro fichier `Dockerfile` à maintenir/faire évoluer à la main pour 6 modules quasi identiques.
- Les images héritent automatiquement des bonnes pratiques buildpacks (utilisateur non-root, layering optimisé pour le cache Docker).
- Nécessite Docker (ou équivalent) disponible localement pour construire les images (`mvn spring-boot:build-image`).
- Alternative écartée : un `Dockerfile` multi-stage par module — plus de contrôle fin, mais maintenance dupliquée sur 6 modules pour un bénéfice marginal à ce stade du projet.
