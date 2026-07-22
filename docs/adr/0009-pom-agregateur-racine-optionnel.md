# ADR 0009 — Pom agrégateur racine (reactor) sans héritage de parent

## Statut

Accepted

## Contexte

Le repo ne contient aujourd'hui aucun pom à la racine : les 6 modules Maven sont totalement indépendants côté build (chacun a son propre `spring-boot-starter-parent` comme `<parent>`). Il devient pénible de builder/tester tous les modules en une seule commande à mesure que leur nombre grandit.

## Décision

Ajouter un `pom.xml` à la racine du repo, **packaging `pom`**, listant les modules dans `<modules>`, mais qui **n'est le `<parent>` d'aucun module** — chaque module Maven garde `spring-boot-starter-parent:4.1.0` comme parent pour l'héritage de dépendances/plugins. Le pom racine sert uniquement de reactor Maven (agrégation de build), pas de source d'héritage de version.

## Conséquences

- `mvn -f pom.xml install` (ou équivalent) depuis la racine build tous les modules dans le bon ordre, sans étape manuelle module par module.
- Chaque module reste libre de sa propre version de Spring Boot à l'avenir si besoin (pas de couplage de version imposé par un parent commun autre que `spring-boot-starter-parent`).
- Alternative écartée : faire du pom racine le `<parent>` de tous les modules (en plus ou à la place de `spring-boot-starter-parent`) — rejetée car cela recréerait un couplage de version fort entre microservices censés être indépendants, contraire à l'esprit microservices du projet.
