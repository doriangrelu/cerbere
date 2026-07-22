# ADR 0007 — Authentification via Keycloak (phase 2)

## Statut

Proposed — non implémenté dans cette itération.

## Contexte

Le client veut à terme une authentification centralisée via Keycloak pour distinguer l'accès BO admin (réseau interne) de l'accès utilisateur final (internet), avec des rôles différents. Tous les modules ont déjà été scaffoldés avec `spring-boot-starter-security` et `spring-boot-starter-security-oauth2-resource-server` en dépendance, en anticipation de cette phase.

## Décision (proposée, à confirmer en phase 2)

- Chaque service métier (`cerbere-core`, `cerbere-history`, `cerbere-notification`) sera un **resource server OAuth2** validant les JWT émis par Keycloak.
- `api-gateway` fera le premier contrôle de routage par rôle (admin vs utilisateur final) avant de transmettre la requête, en s'appuyant sur les claims du token.
- `cerbere-devices-mock` restera en `permitAll` (accès interne uniquement, jamais exposé sur internet), car il n'a pas vocation à survivre au-delà de la phase de mock.
- Le registre `UserAccount` actuellement prévu dans `cerbere-core` (voir modèle de domaine) sera remplacé ou complété par l'identité Keycloak (mapping `sub` Keycloak → utilisateur applicatif).

## Conséquences (anticipées)

- Le fait d'avoir déjà les dépendances resource-server dans les poms permet d'activer la sécurité sans changement de build lors de la phase 2.
- Pour l'itération actuelle, la sécurité de tous les services reste en `permitAll` avec un commentaire `TODO Keycloak` explicite dans chaque config de sécurité — ne pas exposer ces services sur internet en l'état.
- Cet ADR devra être repassé en statut **Accepted** avec le détail exact (realm(s), clients, mapping de rôles) une fois la phase 2 démarrée.
