# Objectifs

App Android perso pour suivre des objectifs **hebdomadaires** (rond) et **mensuels** (carré),
avec un widget pour l'écran d'accueil / de verrouillage.

- Kotlin + Jetpack Compose, widget Jetpack Glance
- Données locales (SharedPreferences), aucun serveur

## Utilisation

- **Suivi jour par jour** : 1 clic = vert (réussi), 2e clic = rouge (raté), 3e clic = vide. Pas de clic = oubli. Les jours futurs (pointillés) ne sont pas cliquables.
- **Objectifs** : onglet *Semaine* (7 ronds L→D par objectif) et onglet *Mois* (carré du jour + compteur « x / 30 j » + calendrier du mois). Flèches pour changer de semaine / de mois, toucher le sous-titre pour revenir à aujourd'hui. `+` pour ajouter, appui long sur une carte pour supprimer.
- **Historique** : par objectif, calendrier mensuel coloré + bilan par semaine (« x / 7 »).
- **Widget** (écran de verrouillage) : ronds de la semaine pour les objectifs hebdo, puis pour les objectifs du mois : compteur, barre et carré du jour aligné sous le « D ». 4 objectifs max par type.
- Thème clair ou sombre selon le réglage du téléphone.

## Écran de verrouillage (Samsung)

Installer **Good Lock** → module **LockStar**, puis ajouter le widget « Objectifs » sur l'écran de verrouillage.

## Compilation

Chaque push déclenche GitHub Actions (`.github/workflows/build.yml`) :

- APK publié dans les **Releases** (`build-N`) et sur la branche **`apk`** (`Objectifs.apk`)
- log de compilation sur la branche **`ci-log`** (`build.log`, `STATUS.txt`)

L'APK est signé avec une clé fixe (`app/debug.keystore`) : chaque nouvelle version s'installe
par-dessus la précédente sans perdre les données.

En local (Android Studio ou SDK installé) : `./gradlew assembleRelease`.
