# ADR 0024 — Reporting périodique du Mock et simulation d'un device hors réseau

## Statut

Accepted (remplace la simulation aléatoire d'[ADR 0004](0004-module-devices-mock-et-separation-du-registre-device.md) : le Mock ne tire plus d'états au hasard)

## Contexte

`cerbere-devices-mock` n'émettait que sur action manuelle (déclenchement depuis l'écran Mode test) ou via un scheduler qui tirait des états **au hasard** pour les devices marqués `autoSimulate`. Deux problèmes.

D'abord, ce n'est pas ce que fait du vrai matériel : un capteur Zigbee rapporte périodiquement son état — le même tant que rien ne change — il n'invente pas de transitions. La simulation aléatoire produisait donc un flux irréaliste, et pouvait déclencher l'alarme sans qu'on l'ait demandé pendant une recette.

Ensuite et surtout, la supervision de vie mise en place côté `cerbere-core` (ADR 0020 : un device dont `lastSeenAt` dépasse le délai configuré est marqué injoignable et déclenche l'alarme) était **inéprouvable**. Sans émission régulière, tout device simulé finissait fatalement par paraître injoignable ; et à l'inverse, rien ne permettait de simuler volontairement un device qui tombe du réseau pour vérifier que la détection fonctionne.

## Décision

**Reporting périodique de l'état courant.** Chaque device simulé joignable republie son état courant toutes les `cerbere.devices-mock.heartbeat.fixed-delay-ms` (60 s par défaut), sur le même topic et avec les mêmes payloads que d'habitude (voir ADR 0021) — indiscernable, côté `cerbere-devices-bridge`, d'un vrai capteur qui fait son reporting. L'état publié est le **dernier état déclenché manuellement**, ou l'état initial du type (`CLOSED`/`CLEAR`/`INACTIVE`, l'état « tout va bien ») tant qu'aucun déclenchement n'a eu lieu. Aucun état n'est inventé : le device répète ce qu'il « observe ».

Ce reporting entretient le `lastSeenAt` de `cerbere-core` et rend donc la supervision de vie observable en conditions normales. Le délai par défaut (60 s) est délibérément très inférieur au `cerbere.core.device-heartbeat.timeout-ms` (5 min) pour qu'un device vivant ne soit jamais marqué injoignable à tort.

**La simulation aléatoire disparaît.** Le flag `autoSimulate`, `DeviceType.randomState()` et les fabriques `random()` des énumérations d'état sont supprimés : le reporting périodique remplit le besoin (« il se passe quelque chose sans que je clique ») de façon réaliste et prévisible, sans risque de déclencher l'alarme par surprise.

**Simulation d'un device hors réseau.** `SimulatedDevice` porte un champ persisté `online` (vrai à la création). Un device débranché (`PUT /api/devices-mock/{id}/availability`, bouton dédié sur l'écran Mode test) :

- n'est plus jamais inclus dans le reporting périodique ;
- refuse tout déclenchement manuel (`DeviceOfflineException` → 409, bouton désactivé dans l'UI) ;
- **conserve son état courant** — il ne le rapporte simplement plus, comme un capteur dont la pile est vide ou qui est hors de portée.

Rien n'est publié au moment du débranchement : un device qui disparaît du réseau ne prévient personne. C'est précisément l'absence d'émission ultérieure qui doit être détectée par `cerbere-core`, ce qui donne enfin un moyen d'éprouver de bout en bout la chaîne complète : device muet → `lastSeenAt` périmé → violation → alarme déclenchée si armée → alerte `WARNING`.

## Conséquences

- La recette de la supervision de vie devient possible sans matériel : débrancher un device, attendre le délai de timeout, constater le déclenchement. Le rebrancher fait repartir le reporting, et l'événement suivant le fait ressortir de la violation.
- Le reporting périodique republie l'état courant **y compris quand c'est une violation** (ex. un contact laissé `OPEN`). Système armé, `HandleDeviceEventService` lèvera donc une alerte à chaque cycle tant que le capteur reste en violation. C'est le comportement d'un vrai capteur (un contact ouvert continue de le signaler), mais ça alimente l'historique en alertes répétées — une déduplication côté `cerbere-core` sera peut-être à prévoir si le bruit gêne en pratique.
- Le délai de reporting est global au Mock, pas réglable par device : suffisant pour de la recette, à revoir seulement si l'on veut simuler des capteurs à cycles hétérogènes.
- Alternative écartée : publier un message d'indisponibilité (`<base-topic>/<friendly_name>/availability`, que la vraie passerelle Zigbee2MQTT sait émettre) au moment du débranchement. Ça aurait rendu la détection immédiate côté Bridge, mais aurait court-circuité exactement le mécanisme qu'on veut éprouver — la détection par absence de nouvelles. À reconsidérer plus tard comme complément, pas comme remplacement.
