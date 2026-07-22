# ADR 0015 — Stack du front interne : Thymeleaf + htmx + Beer CSS

## Statut

Accepted

## Contexte

Le client veut commencer le BO admin (réseau interne) pour pouvoir faire de la recette visuelle sans manipuler les API à la main. Il veut un rendu simple, épuré, coloré, plutôt Material Design, avec un découpage propre en layouts et composants réutilisables. Une recherche des pratiques courantes (2026) a été menée avant de trancher.

## Décision

- **Rendu** : Thymeleaf server-side, servi par `cerbere-bff`, sans framework JS ni étape de build front (cohérent avec un projet 100% Java et un outil interne, pas une SPA grand public).
- **CSS / Material Design** : [Beer CSS](https://www.beercss.com/) — Material Design 3, zéro build, CDN-first, activement maintenu. **Materialize CSS** (le choix historique) a été écarté : aucune mise à jour vérifiable depuis 2024-2025.
- **Layouts** : fragments paramétrés **natifs** Thymeleaf (`th:fragment="layout(title, content)"` + `th:replace="~{layout/main :: layout(~{::title}, ~{::main})}"`), **pas** la librairie `thymeleaf-layout-dialect`. Cette dernière embarque un runtime Groovy (poids/démarrage, incompatible GraalVM, et un bug Groovy 5.0.4 a cassé le rendu de template chez des utilisateurs récemment) pour un bénéfice que Thymeleaf couvre nativement depuis ses versions récentes.
- **Composants réutilisables** : fragments Thymeleaf classiques sous `templates/fragments/`, un fragment par composant (ex : `th:fragment="carteDevice(device)"`), importés via `th:insert`/`th:replace` — aucune librairie supplémentaire nécessaire.
- **Interactivité** : [htmx](https://htmx.org/) en direct (CDN, attributs `hx-*` bruts). La librairie [`htmx-spring-boot`](https://github.com/wimdeblauwe/htmx-spring-boot) (+ `htmx-spring-boot-thymeleaf`, dialecte `hx:`, `@HxRequest`, injection auto du token CSRF) avait été retenue initialement, mais sa version publiée (3.1.1 / 5.0.0, mars 2026) référence encore `org.springframework.boot.autoconfigure.web.servlet.WebMvcRegistrations`, un package déplacé dans Spring Boot 4.1 — plantage au démarrage (`FileNotFoundException` sur cette classe). Retirée tant qu'elle n'a pas de version compatible Boot 4. À réintroduire dès qu'une version compatible sort, ou à réévaluer quand CSRF sera activé (phase Keycloak) : sans elle, l'injection du token CSRF dans les requêtes htmx devra être faite à la main (petit script JS d'écoute `htmx:configRequest`).

## Conséquences

- Aucun outillage npm/webpack/vite à maintenir ; le front se build avec le reste du module Java.
- `cerbere-bff` appelle `cerbere-core` (et plus tard `cerbere-history`) en REST via `RestClient`, sans passer par `api-gateway` pour l'instant (recette interne, pas d'exposition internet).
- Pas de `domain`/`application` dans `cerbere-bff` pour cette itération : c'est un agrégateur/présentateur, pas un porteur de règles métier — écart assumé, comme pour `api-gateway` (voir [ADR 0001](0001-clean-architecture-par-module.md)).
- Si un jour l'interface internet (phase 3, grand public) a besoin d'une UX plus riche/réactive, une vraie SPA pourra être envisagée spécifiquement pour cette interface, sans remettre en cause ce choix pour le BO interne.
