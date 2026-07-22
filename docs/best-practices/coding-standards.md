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
- Utiliser `Stream`/`Optional`/lambdas plutôt que des boucles impératives et des `null` non protégés, sans sacrifier la lisibilité (pas de chaînes de streams illisibles pour l'unique plaisir du style fonctionnel).
- `var` autorisé quand le type est évident au site d'appel, sinon type explicite.
- **`this.` obligatoire** pour tout accès à un champ ou un appel à une méthode d'instance depuis l'intérieur de la classe (ex : `this.simulatedDeviceRepository.save(...)`, pas `simulatedDeviceRepository.save(...)`). Objectif : lever toute ambiguïté à la relecture entre un champ, une variable locale et un paramètre. S'applique au code de production comme aux tests. Ne s'applique pas aux appels de méthodes/champs statiques (`MonEnum.valueOf(...)`, pas `this.` sur du statique) ni à l'intérieur d'un `record`/enum sans champ mutable propre.

## Nommage

- Classes : `PascalCase`, noms métier explicites (`ArmSystemUseCase`, pas `Service1`).
- **Suffixe systématique par rôle**, pour identifier la nature d'une classe sans avoir à lire son contenu :

  | Rôle | Suffixe | Exemple |
  |---|---|---|
  | Interface de port d'entrée (use-case exposé) | `UseCase` | `TriggerDeviceEventUseCase` |
  | Implémentation d'un ou plusieurs `UseCase` | `Service` | `RegisterSimulatedDeviceService` |
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
- Packages : toujours en minuscules, `snake_case` uniquement pour le nom du module dans le package racine (`fr.cerbere.component.cerbere_devices_mock`), le reste des sous-packages en minuscules simples (`domain`, `usecase`, `mongo`...).
- Pas d'abréviations obscures (`repo` toléré, `mgr`/`svc`/`impl` à éviter comme suffixe systématique).

## Contrats partagés

- Un module Maven `contract` partagé (ex : `cerbere-contract`) **est autorisé** pour porter les enveloppes d'événements et DTOs communs à plusieurs services — voir [ADR 0010](../adr/0010-module-contract-partage-autorise.md). Il ne contient que des `record`/enums (aucune logique métier, aucune dépendance Spring/Mongo/Kafka au-delà de Jackson).
- Règle d'extraction : ne pas créer ce module par anticipation. Chaque service porte son propre DTO tant qu'il est seul consommateur ; l'extraction vers le module partagé n'intervient que lorsqu'un second service a réellement besoin du même contrat.

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
