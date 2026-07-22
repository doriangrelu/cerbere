# ADR 0006 — `cerbere-notification` conserve sa propre copie des destinataires

## Statut

Accepted

## Contexte

`cerbere-notification` (itération future) doit envoyer une alerte (email/push) dès réception d'un événement `cerbere.alarm.alerts` sur Kafka. Il faut décider comment il obtient les coordonnées de contact (email) des destinataires : appel synchrone à `cerbere-core` au moment de l'envoi, ou copie locale.

## Décision

`cerbere-notification` maintient sa **propre copie locale** des destinataires (`NotificationRecipient` : email, canaux activés) dans sa base Mongo (`cerbere_notification`), plutôt que d'interroger `cerbere-core` en synchrone à chaque alerte.

Pour cette itération (backend + devices-mock), cette copie est alimentée manuellement (config/API directe). La synchronisation automatique via un événement `user.recipient.updated` est différée à une itération ultérieure, une fois `cerbere-core` implémenté.

## Conséquences

- L'envoi d'une notification reste possible même si `cerbere-core` est indisponible au moment de l'alerte — la fiabilité de la notification prime sur la fraîcheur de la donnée de contact.
- Risque accepté : désynchronisation temporaire si un email change dans `cerbere-core` sans propagation immédiate — acceptable pour un système domestique mono-utilisateur/famille où ces changements sont rares.
- Alternative écartée : appel REST synchrone à `cerbere-core` à chaque notification — rejeté car il rendrait l'envoi d'alerte dépendant de la disponibilité d'un autre service, contraire à l'objectif de fiabilité d'un système d'alarme.
