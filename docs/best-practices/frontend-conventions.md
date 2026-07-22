# Bonnes pratiques — Front interne (Thymeleaf + htmx + Beer CSS)

Référence technique pour [ADR 0015](../adr/0015-stack-front-interne-thymeleaf-htmx-beercss.md). S'applique à `cerbere-bff`.

## Arborescence des templates

```
src/main/resources/templates/
  layout/
    main.html         fragment layout(title, content) : structure HTML, nav, CDN Beer CSS
  fragments/
    nav.html          barre de navigation, un fragment par composant réutilisable
    alarm-status.html badge de statut alarme (réutilisé en page complète et en swap htmx)
  alarm/
    dashboard.html     page complète (arm/disarm/statut), invoque layout/main
```

Un dossier par écran/agrégat (`alarm/`, plus tard `devices/`, `zones/`), un dossier `fragments/` pour tout composant importé depuis plusieurs pages.

## Layout

Pas de `thymeleaf-layout-dialect` (voir ADR 0015). Pattern natif :

```html
<!-- layout/main.html -->
<div th:fragment="layout(title, content)">
  <!DOCTYPE html>
  <html>
    <head><title th:replace="${title}">Cerbère</title>...</head>
    <body>
      <nav th:replace="~{fragments/nav :: nav}"></nav>
      <main th:replace="${content}"></main>
    </body>
  </html>
</div>
```

```html
<!-- alarm/dashboard.html -->
<html th:replace="~{layout/main :: layout(~{::title}, ~{::main})}">
  <title>Alarme</title>
  <main>...</main>
</html>
```

## Composants

Deux cas, pattern différent :

- **Composant pur import** (jamais renvoyé seul par un contrôleur) : paramètres de fragment explicites, pas de dépendance implicite au modèle de la page appelante.

  ```html
  <!-- fragments/carte-device.html -->
  <div th:fragment="carteDevice(device)" class="card">
    <span th:text="${device.label}">Porte</span>
  </div>
  ```

- **Composant aussi renvoyé directement par un contrôleur** (cible d'un swap htmx) : **pas de paramètre de fragment déclaré**, il lit un attribut de `Model` directement (même nom que l'attribut ajouté par le contrôleur). Un `th:fragment="alarmStatus(status)"` ne fonctionnerait pas ici : les paramètres de fragment ne sont liés que lors d'un appel explicite (`th:insert="~{... :: alarmStatus(${x})}"`), pas quand Thymeleaf résout directement `template :: fragment` comme vue retournée par un contrôleur.

  ```html
  <!-- fragments/alarm-status.html -->
  <div th:fragment="alarmStatus" id="alarm-status" th:classappend="${alarmStatus.status()}">
    <span th:text="${alarmStatus.status()}">DISARMED</span>
  </div>
  ```

  Importable tel quel depuis une page complète (`th:replace="~{fragments/alarm-status :: alarmStatus}"`, tant que `alarmStatus` est déjà dans le `Model` de la page), et renvoyable seul par un contrôleur (`return "fragments/alarm-status :: alarmStatus";`).

## htmx

- htmx chargé en CDN, attributs `hx-*` bruts (pas de librairie Java côté serveur — `htmx-spring-boot` a été retirée, incompatible Spring Boot 4.1 pour l'instant, voir ADR 0015).
- Les boutons d'action (arm/disarm) déclenchent un `hx-post` qui retourne **uniquement le fragment concerné** (ex : le badge de statut), jamais la page entière.
- CSRF actuellement désactivé partout (`PermitAllSecurityConfig`, TODO Keycloak) donc pas de problème pour l'instant. Le jour où CSRF sera activé : soit une version compatible Boot 4 de `htmx-spring-boot` sera sortie (à réévaluer), soit injecter le token à la main via un listener `htmx:configRequest` en JS.
- Un `id` HTML stable sur l'élément remplacé (`hx-target`) à chaque interaction, pour que le swap soit prévisible.

## CSS / Beer CSS

- Chargé en CDN dans `layout/main.html`, pas de build step.
- Couleurs/thème : classes utilitaires Beer CSS (`primary`, `secondary`, `surface`...) plutôt que du CSS custom — n'écrire du CSS maison qu'en dernier recours.
