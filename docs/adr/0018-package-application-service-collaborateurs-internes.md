# ADR 0018 — Package `application.service` pour les collaborateurs internes sans port d'entrée

## Statut

Accepted

## Contexte

En résolvant ADR 0017 (cohérence `Zone`/`Device`) puis un bug connexe (réévaluation du déclenchement de l'alarme à la réactivation d'un device), deux classes ont été introduites : `RecomputeZoneViolationService`/`RecomputeZoneViolationUseCase` et `AlarmSystemService.reevaluate()`/`ReevaluateAlarmTriggerUseCase`. Les deux ont été câblées comme n'importe quel autre use-case : interface sous `domain/port/in/*`, implémentation `*Service` sous `application/usecase/*`, bean exposé dans `UseCaseConfig`.

Or `docs/best-practices/clean-architecture.md` définit explicitement `port/in` comme des **"driving ports"** — des interfaces destinées à être appelées par un adapter (contrôleur REST, `@KafkaListener`, scheduler). Ni `RecomputeZoneViolationUseCase.recompute()` ni `ReevaluateAlarmTriggerUseCase.reevaluate()` ne sont jamais appelés par un adapter : ils ne sont invoqués que par d'autres classes `application.usecase` (`UpdateDeviceService`, `DeleteDeviceService`, `HandleDeviceEventService`), en réaction à un effet de bord (maintien d'un invariant inter-agrégats ou re-décision suite à un changement d'état). Les traiter comme des use-cases exposés brouille la lecture de `port/in` : ce package n'est plus une liste fiable de "tout ce que l'extérieur peut demander à l'application".

## Décision

Un port `port/in/*UseCase` reste réservé aux actions réellement pilotées depuis l'extérieur de l'application (adapter → use-case). Toute logique applicative qui n'est invoquée que par une *autre* classe `application` — jamais par un adapter — n'est pas un use-case : elle vit dans un nouveau package `application.service` (sibling de `application.usecase`), sans interface `port/in` correspondante.

Ces classes :
- restent des classes concrètes `final`, sans interface (aucune frontière de hexagone n'est franchie entre deux classes `application` — la règle "jamais d'implémentation concrète en dépendance directe" ne s'applique qu'*entre couches*, pas à l'intérieur de `application`) ;
- gardent le suffixe `Service` (le suffixe seul ne distingue plus un vrai use-case d'un collaborateur interne — c'est le **package** qui tranche, voir coding-standards.md) ;
- sont câblées dans une configuration Spring dédiée, `ApplicationServiceConfig`, distincte de `UseCaseConfig` (qui ne câble que des ports `in`) ;
- sont injectées par leur **type concret** dans les use-cases qui les utilisent (`UpdateDeviceService` dépend directement de `RecomputeZoneViolationService`, pas d'une interface).

Appliqué rétroactivement : `RecomputeZoneViolationUseCase` et `ReevaluateAlarmTriggerUseCase` sont supprimées de `domain/port/in`. `RecomputeZoneViolationService` déménage vers `application.service`. La logique de `reevaluate()` est extraite de `AlarmSystemService` (qui reste un vrai use-case — `arm`/`disarm`/`getCurrentStatus`, tous pilotés par `AlarmController`) vers une nouvelle classe dédiée `application.service.AlarmTriggerReevaluationService`, partagée par `AlarmSystemService.arm()` (predicate `anyEnabledDeviceViolating()`) et par `UpdateDeviceService` (méthode `reevaluate()`), pour ne pas dupliquer la logique de déclenchement entre les deux points d'appel.

## Conséquences

- `domain/port/in/*` redevient une liste fiable de tout ce qu'un adapter peut demander à l'application — test de relecture simple : *"un contrôleur/consumer/scheduler appelle-t-il ça directement ?"*.
- Deux fichiers de configuration Spring côté `cerbere-core` (`UseCaseConfig`, `ApplicationServiceConfig`) plutôt qu'un seul — coût mineur, gain en lisibilité (on sait dans quel fichier chercher selon la nature de la classe).
- Règle générale pour la suite du projet, dans tous les modules : avant d'ajouter un `port/in` + suffixe `UseCase`, vérifier qu'un adapter l'appellera réellement ; sinon, `application.service`, classe concrète, pas de port.
- Alternative écartée : garder l'interface `port/in` pour la testabilité (mock plus facile). Écartée car (a) le projet utilise déjà l'inline-mock-maker de Mockito, qui mocke les classes `final` sans problème, et (b) ADR 0001 n'exige l'inversion de dépendance qu'à la frontière du hexagone (adapter ↔ application, application ↔ infrastructure), pas entre deux classes `application`.
