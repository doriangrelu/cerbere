# Bonnes pratiques — Standards de code

Référence pour tout code écrit dans les modules Cerbère. Ce document est maintenu par l'assistant IA au fil des itérations.

## SOLID

- **Single Responsibility** : une classe = une raison de changer. Un use-case (`application/usecase`) ne fait qu'une chose métier ; la traduction HTTP/Mongo/Kafka vit dans `adapter`/`infrastructure`, jamais dans `domain`/`application`.
- **Open/Closed** : privilégier de nouvelles implémentations de ports plutôt que de modifier une classe existante pour un nouveau cas.
- **Liskov Substitution** : toute implémentation d'un port (`domain/port/out`) doit être substituable sans changer le comportement attendu par le use-case appelant.
- **Interface Segregation** : les ports sont petits et ciblés (ex : `DeviceEventPublisher` ne fait que publier, pas de méthode fourre-tout).
- **Dependency Inversion** : `application` dépend d'interfaces définies dans `domain/port`, jamais d'une classe d'infrastructure concrète. L'injection de dépendance (Spring) relie les implémentations au démarrage.

## Immutabilité et style Java moderne

- `final` systématique : sur les paramètres de méthode, les variables locales, les champs de classe. Un champ qui doit changer d'état retourne une **nouvelle instance** plutôt que d'être muté (`with*`-style).
- Value objects, DTOs, événements de domaine et enveloppes Kafka → `record` Java.
- Entités avec identité et cycle de vie (ex : `AlarmSystem`, `SimulatedDevice`) → classes à champs `private final`, méthodes "métier" qui retournent une nouvelle instance plutôt que de muter `this`.
- **Exception à `final` sur la classe elle-même** : les classes que Spring doit pouvoir proxifier (ex : `@Repository` avec traduction d'exception, `@Transactional`) peuvent devoir rester non-`final` si le framework retombe sur un proxy CGLIB — contrainte du framework, pas un choix de style. Les champs de ces classes restent `private final`.
- **Verrouillage optimiste Mongo (`@Version`)** : tout agrégat persisté porte un champ `version` (`Long`, `null` tant que non persisté) sur son `Document` Mongo (`@Version` de `org.springframework.data.annotation`) **et** sur son modèle de domaine correspondant, préservé par toutes les méthodes `with*`/transitions (jamais remis à `null` après le premier `save`). Sans ce fil de bout en bout, Spring Data considère toute entité avec `version == null` comme neuve (au lieu de se baser sur l'id) dès qu'un champ `@Version` existe sur le document — une mise à jour serait alors traitée comme une insertion en doublon. Seules les fabriques `restore(...)` (reconstruction depuis la persistance) prennent `version` en paramètre ; `register()`/`initial()` (nouvelle entité) le laissent à `null`.
- Utiliser `Stream`/`Optional`/lambdas plutôt que des boucles impératives et des `null` non protégés, sans sacrifier la lisibilité (pas de chaînes de streams illisibles pour l'unique plaisir du style fonctionnel).
- **Préférer une méthode référence à une lambda équivalente** dès que la lambda ne fait qu'appeler une méthode existante sans logique propre (`.map(DeviceMapper::toDomain)` plutôt que `.map(d -> DeviceMapper.toDomain(d))`, `.filter(Device::isEnabled)` plutôt que `.filter(d -> d.isEnabled())`). Ne s'applique pas quand la lambda capture une variable supplémentaire ou contient de la logique (ex : `.orElseThrow(() -> new DeviceNotFoundException(id))` reste une lambda, `id` étant capturé).
- `var` autorisé quand le type est évident au site d'appel, sinon type explicite.
- **`this.` obligatoire** pour tout accès à un champ ou un appel à une méthode d'instance depuis l'intérieur de la classe (ex : `this.simulatedDeviceRepository.save(...)`, pas `simulatedDeviceRepository.save(...)`). Objectif : lever toute ambiguïté à la relecture entre un champ, une variable locale et un paramètre. S'applique au code de production comme aux tests. Ne s'applique pas aux appels de méthodes/champs statiques (`MonEnum.valueOf(...)`, pas `this.` sur du statique) ni à l'intérieur d'un `record`/enum sans champ mutable propre.

## Lombok

- **`@RequiredArgsConstructor`** sur toute classe dont le seul constructeur injecte des dépendances `private final` (use-cases, adapters, contrôleurs, schedulers) — remplace le constructeur écrit à la main. Ne pas utiliser `@AllArgsConstructor`/`@NoArgsConstructor` qui cassent l'immuabilité ou l'invariant "toutes les dépendances sont obligatoires".
- **`@Getter`** sur les entités de domaine (`Device`, `Zone`, `AlarmSystem`, `SimulatedDevice`...) pour remplacer les accesseurs écrits à la main. Ne génère jamais de setter (`@Setter` interdit sur ces classes, contraire à l'immuabilité).
- **`@Slf4j`** pour tout logger, plutôt que `private static final Logger LOGGER = LoggerFactory.getLogger(...)` écrit à la main.
- Ne s'applique pas aux `record` (déjà immuables et déjà pourvus d'accesseurs/constructeur par le langage) ni aux classes avec constructeur privé + fabriques statiques (`register()`/`restore()` sur les entités) : Lombok ne génère pas ce pattern, le constructeur privé reste écrit à la main, seul `@Getter` s'applique par-dessus.

## MapStruct

- Les traducteurs structurels **champ à champ** entre couches (domaine ↔ document Mongo, domaine → DTO REST) sont des interfaces MapStruct (`@Mapper(componentModel = "spring")`), pas des classes utilitaires statiques écrites à la main.
- **Aucune expression Java inline** (`@Mapping(target = "x", expression = "java(...)")` interdit) : toute conversion non triviale (`UUID` ↔ `String`, normalisation de texte...) passe par une **méthode nommée** (`@Named("...")` + `qualifiedByName`), idéalement **partagée** entre mappers via `uses = {...}` plutôt que dupliquée dans chaque interface.
- Ne s'applique **pas** aux traducteurs qui contiennent une vraie logique métier (choix conditionnel du `eventType`, construction du `payload` selon le type d'événement...) — ceux-là restent des `Factory` écrites à la main (voir tableau de suffixes), MapStruct n'étant pertinent que pour du mapping de champs, pas de la prise de décision.

## Normalisation des champs texte libre (couche présentation)

Tout champ alimenté par une saisie utilisateur libre (`label` d'un device, `name` d'une zone...) est **trimé et mis en minuscule** au moment du mapping DTO REST → domaine, dans la couche `adapter/in/web` — jamais plus profond (le domaine ne fait pas d'hypothèse sur la casse/les espaces de ce qu'on lui passe). Centraliser cette normalisation dans une méthode nommée partagée entre les mappers MapStruct concernés plutôt que de la dupliquer.

## Nommage

- Classes : `PascalCase`, noms métier explicites (`ArmSystemUseCase`, pas `Service1`).
- **Suffixe systématique par rôle**, pour identifier la nature d'une classe sans avoir à lire son contenu :

  | Rôle | Suffixe | Exemple |
  |---|---|---|
  | Interface de port d'entrée (use-case exposé) | `UseCase` | `TriggerDeviceEventUseCase` |
  | Implémentation d'un ou plusieurs `UseCase` (package `application.usecase`) | `Service` | `RegisterSimulatedDeviceService` |
  | Collaborateur interne à `application`, sans port d'entrée (package `application.service`) | `Service` | `RecomputeZoneViolationService` |
  | Port de sortie (persistance) | `Repository` | `SimulatedDeviceRepository` |
  | Port de sortie (publication d'événement) | `Publisher` | `DeviceEventPublisher` |
  | Document persisté (Mongo) | `Document` | `SimulatedDeviceDocument` |
  | Implémentation Spring Data d'un port `Repository` | `Repository`/`RepositoryAdapter` | `SimulatedDeviceMongoRepositoryAdapter` |
  | Producteur Kafka | `Producer` | `DeviceEventKafkaProducer` |
  | Enveloppe/DTO de message Kafka | `Envelope` | `DeviceEventEnvelope` |
  | Événement de domaine | `Event` (ou intégré au nom, ex : `...Occurred`) | `DeviceEventOccurred` |
  | Exception métier | `Exception` | `DeviceNotFoundException` |
  | DTO REST entrant | `Request` | `RegisterSimulatedDeviceRequest` |
  | DTO REST sortant | `Response` | `SimulatedDeviceResponse` |
  | Contrôleur REST | `Controller` | `SimulatedDeviceController` |
  | Traducteur entre couches (domaine ↔ document/DTO) | `Mapper` | `SimulatedDeviceMapper` |
  | Fabrique d'objet complexe | `Factory` | `DeviceEventEnvelopeFactory` |
  | Job planifié | `Scheduler` | `DeviceSimulationScheduler` |
  | Classe `@Configuration` Spring | `Config` | `KafkaProducerConfig` |
  | Gestionnaire d'exceptions REST | `ExceptionHandler` (ou nom explicite terminé par `Handler`) | `DevicesMockExceptionHandler` |
  | Point d'entrée Spring Boot | `Application` | `CerbereDevicesMockApplication` |

  Les entités du domaine avec identité (ex : `SimulatedDevice`) et les value objects/enums métier (ex : `DeviceType`, `ContactState`) restent nommés par leur concept métier, sans suffixe technique — ce sont les classes que ce tableau sert justement à distinguer du reste.

  Le suffixe `Service` seul ne dit pas si une classe est un vrai use-case exposé : c'est le **package** qui tranche (`application.usecase` vs `application.service`, voir ADR 0018) — un `Service` sous `application.usecase` implémente toujours au moins une interface `port/in/*UseCase` ; un `Service` sous `application.service` n'en implémente aucune, n'est jamais appelé par un adapter, et n'a donc pas d'interface `UseCase` correspondante.
- Packages : toujours en minuscules, `snake_case` uniquement pour le nom du module dans le package racine (`fr.cerbere.component.cerbere_devices_mock`), le reste des sous-packages en minuscules simples (`domain`, `usecase`, `mongo`...).
- Pas d'abréviations obscures (`repo` toléré, `mgr`/`svc`/`impl` à éviter comme suffixe systématique).

## Contrats partagés

- Le module `cerbere-shared-kernel` (`fr.cerbere.shared`) **porte tout DTO consommé par plus d'un module** — voir [ADR 0010](../adr/0010-module-contract-partage-autorise.md)/[ADR 0013](../adr/0013-module-cerbere-shared-kernel.md). Règle sans exception : ça vaut pour les enveloppes/événements Kafka comme pour les DTOs REST (request/response) échangés entre deux services Cerbère (ex : `cerbere-bff` qui appelle `cerbere-core`). **Ne jamais dupliquer une copie locale d'un DTO déjà consommé ailleurs** — sous-package `fr.cerbere.shared.dto.<agrégat>` pour les DTOs REST (ex : `fr.cerbere.shared.dto.alarm.AlarmStatusResponse`), à distinguer de `fr.cerbere.shared.event` (enveloppes Kafka).
- Règle d'extraction : ne pas créer un DTO partagé par anticipation. Chaque service porte son propre DTO tant qu'il est seul à le produire/consommer ; l'extraction vers `cerbere-shared-kernel` intervient dès qu'un **second** module a réellement besoin du même contrat (producteur+consommateur, ou deux consommateurs).

## Documentation du code

- **JavaDoc obligatoire** sur toutes les classes et méthodes publiques des packages `domain` (modèle + ports) — c'est le contrat métier, il doit être lisible sans lire l'implémentation.
- JavaDoc recommandé mais non obligatoire sur `application`/`infrastructure`/`adapter` si le nommage suffit à comprendre l'intention.
- Pas de commentaire qui répète ce que le code dit déjà. Un commentaire n'est utile que pour expliquer un POURQUOI non évident (contrainte cachée, contournement, choix de simulation).

## Tests

- Le `domain` et l'`application` doivent être testables sans contexte Spring (tests unitaires purs).
- Tests d'intégration (`@SpringBootTest`, `@WebMvcTest`, Testcontainers/embedded Mongo-Kafka le cas échéant) pour les adapters et l'infrastructure.
- Un test minimum par use-case central et par contrôleur REST exposé.

## Gestion des erreurs

- Exceptions métier dédiées dans `domain/exception`, porteuses de sens (`DeviceNotFoundException`, pas de `RuntimeException` générique).
- Pas de `try/catch` silencieux : toute exception attrapée est soit relancée sous une forme plus parlante, soit traitée explicitement avec une justification.
- **Réponses d'erreur REST en `ProblemDetail`** (RFC 9457, `org.springframework.http.ProblemDetail`), jamais un `ResponseEntity<String>` brut. Deux niveaux de `@RestControllerAdvice` :
  - `fr.cerbere.shared.web.CommonExceptionHandler` (`cerbere-shared-kernel`) gère les erreurs **communes** à tout service (`MethodArgumentNotValidException`, `HttpMessageNotReadableException`, `IllegalArgumentException`) — importé explicitement (`@Import(CommonExceptionHandler.class)`) sur chaque `*Application`, le scan de composants ne traversant pas les modules (même règle que `CommonJacksonConfig`/`PermitAllSecurityConfig`, voir ADR 0013).
  - Un `@RestControllerAdvice` **local** à chaque service (ex : `CoreExceptionHandler`, `DevicesMockExceptionHandler`) gère ses propres exceptions métier (`DeviceNotFoundException`, `ZoneNotEmptyException`, ...). Pas de chevauchement de types gérés entre les deux niveaux, donc pas de conflit d'ordre entre advices.
  - Côté BFF, un corps `ProblemDetail` reçu en erreur (via `RestClient`) se lit avec `fr.cerbere.component.cerbere_bff.adapter.support.ProblemDetailMessages.extractDetail(HttpStatusCodeException)`, qui extrait le champ `detail` plutôt que d'afficher le JSON brut à l'usager.
  - **Cas particulier du BFF** : `BffExceptionHandler` y traduit les erreurs d'appel vers `cerbere-core`/`cerbere-devices-mock` non déjà interceptées localement (service injoignable, 5xx, etc.), mais volontairement en simple `@ControllerAdvice` (pas `@RestControllerAdvice`) — ce module sert à la fois des pages complètes (navigation classique) et des fragments htmx (Ajax) depuis les mêmes contrôleurs. Chaque `@ExceptionHandler` inspecte l'en-tête `HX-Request` : requête htmx → `ProblemDetail` JSON (consommé par le script de toast) ; navigation classique → page d'erreur stylée (`templates/error.html`), jamais l'inverse. Un `@RestControllerAdvice` classique aurait forcé du JSON brut même sur une navigation de page complète.
