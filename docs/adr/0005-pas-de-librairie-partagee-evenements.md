# ADR 0005 — Pas de librairie Java partagée pour les événements Kafka

## Statut

Superseded by [ADR 0010](0010-module-contract-partage-autorise.md)

## Contexte

Plusieurs services producteurs/consommateurs d'événements Kafka pourraient bénéficier d'un modèle Java commun (DTOs, enveloppe) pour éviter toute divergence de format. Mais Cerbère est composé de modules Maven totalement indépendants (pas de pom agrégateur parent commun au sens héritage — voir [ADR 0009](0009-pom-agregateur-racine-optionnel.md)), chacun avec son propre cycle de vie de déploiement.

## Décision

Ne pas créer de librairie Java partagée pour l'enveloppe d'événement ou les DTOs Kafka. Le contrat est documenté en JSON (schéma + exemples) dans [docs/best-practices/kafka-conventions.md](../best-practices/kafka-conventions.md). Chaque service implémente son **propre** record Jackson pour lire/écrire l'enveloppe, en lecture tolérante (ignore les champs qu'il ne connaît pas, ne lit que le sous-ensemble du `payload` dont il a besoin).

## Conséquences

- Aucun couplage de version entre microservices : un service peut évoluer son format de `payload` (ajout de champ) sans forcer une mise à jour synchronisée d'une librairie partagée chez tous les consommateurs.
- Risque accepté : duplication de code (l'enveloppe est réécrite dans chaque module). À revoir si cette duplication devient pénible ou source de bugs de désynchronisation — dans ce cas, envisager une librairie partagée versionnée publiée dans un dépôt Maven interne.
- Alternative écartée : module Maven partagé `cerbere-events-contract` importé par tous les services — rejeté pour cette itération car il introduirait un couplage de build entre services indépendants, prématuré tant que le nombre d'événements reste faible.

**Mise à jour** : le client a explicitement autorisé la création d'un module Maven `contract` partagé pour les enveloppes/DTOs. Voir [ADR 0010](0010-module-contract-partage-autorise.md), qui revient sur l'interdiction ci-dessus.
