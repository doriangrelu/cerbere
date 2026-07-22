# ADR 0011 — Publication des artefacts Maven sur GitHub Packages

## Statut

Accepted

## Contexte

Cerbère est un projet open source hébergé sur GitHub. Le client veut que chaque module Maven soit publiable comme package versionné, plutôt que de rester uniquement buildable localement.

## Décision

Chaque module (`api-gateway`, `cerbere-bff`, `cerbere-core`, `cerbere-history`, `cerbere-notification`, `cerbere-devices-mock`) déclare dans son `pom.xml` un `<distributionManagement>` (et le `<repositories>` correspondant) pointant vers GitHub Packages : `https://maven.pkg.github.com/doriangrelu/cerbere`.

Un workflow GitHub Actions unique (`.github/workflows/publish-packages.yml`) publie automatiquement les 6 modules à chaque push sur `main`, via une matrice de jobs (un job par module), authentifié avec le `GITHUB_TOKEN` fourni nativement par Actions (permission `packages: write`).

## Conséquences

- Aucune configuration de credentials à gérer manuellement : `actions/setup-java` génère le `settings.xml` avec le token GitHub Actions.
- Publication indépendante par module (cohérent avec l'absence d'héritage de parent entre modules, [ADR 0009](0009-pom-agregateur-racine-optionnel.md)) : un module peut être publié sans redéployer les autres.
- Pour publier en local (hors CI), il faut un `~/.m2/settings.xml` avec un serveur d'id `github` et un token personnel ayant le scope `write:packages`.
- Alternative écartée : un registre Maven privé auto-hébergé — rejeté pour rester simple et gratuit sur un projet open source déjà hébergé sur GitHub.
