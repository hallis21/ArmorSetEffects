Changelog 2.2.4

Stability (hopefully) and usability update

- Added fields "hasParticles" and "isAmbient" to all potion effects
- Added fields "consumeItem" for item effects
- Item effects now also accept and check for displayname and lore
- A much better json parser
  - Will now hopefully not cause large erros if the format is not exact
  - Gives respons through the console if the set configs are invalid / are missing fields