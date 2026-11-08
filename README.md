# Objectifs

App Android perso pour suivre des objectifs **hebdomadaires** (rond) et **mensuels** (carré),
avec un widget pour l'écran d'accueil / de verrouillage.

- Kotlin + Jetpack Compose, widget Jetpack Glance
- Données locales (SharedPreferences), aucun serveur

## Utilisation

- **Pastilles** : 1 clic = vert (réussi), 2e clic = rouge (raté), 3e clic = vide. Pas de clic = oubli.
- Les pastilles sont alignées dans la colonne du **D** (dimanche) ; une semaine appartient au mois de son dimanche.
- **Accueil** : semaines du mois (flèches pour changer de mois, toucher le titre pour revenir au mois courant), `+` pour ajouter un objectif, appui long sur un objectif pour le supprimer.
- **Historique** : grille calendaire (résumé de la semaine sous chaque dimanche) + bilan par semaine et du mois.
- **Widget** : semaine et mois en cours, 4 objectifs max par type, pastilles cliquables.

## Écran de verrouillage (Samsung)

Installer **Good Lock** → module **LockStar**, puis ajouter le widget « Objectifs » sur l'écran de verrouillage.

## Compilation

Chaque push déclenche GitHub Actions (`.github/workflows/build.yml`) :

- APK publié dans les **Releases** (`build-N`) et sur la branche **`apk`** (`Objectifs.apk`)
- log de compilation sur la branche **`ci-log`** (`build.log`, `STATUS.txt`)

L'APK est signé avec une clé fixe (`app/debug.keystore`) : chaque nouvelle version s'installe
par-dessus la précédente sans perdre les données.

En local (Android Studio ou SDK installé) : `./gradlew assembleRelease`.
