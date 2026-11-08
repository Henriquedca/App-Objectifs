# Objectifs

App Android perso pour suivre des objectifs **hebdomadaires** (rond) et **mensuels** (carré),
avec un widget pour l'écran d'accueil / de verrouillage.

- Kotlin + Jetpack Compose, widget Jetpack Glance
- Données locales (SharedPreferences), aucun serveur

## Utilisation

- **Suivi jour par jour** : 1 clic = vert (réussi), 2e clic = rouge (raté), 3e clic = vide. Pas de clic = oubli. Les jours futurs ne sont pas cliquables.
- **Un objectif vit sur une seule période** : une semaine (hebdomadaire) ou un mois (mensuel). Pour le reconduire : appui long → *Dupliquer* et choisir la semaine / le mois. L'historique est conservé.
- **Objectifs** : semaine et mois en cours uniquement (onglets *Hebdomadaire* / *Mensuel*). `+` pour créer (émoji, nom 10 caractères max, type, période). Appui long : modifier (nom, émoji), dupliquer, supprimer. L'émoji s'affiche devant le nom dans l'app et le widget.
- **Historique** : navigation par mois ; objectifs du mois (calendrier, « x / 30 j », %) et objectifs hebdomadaires de chaque semaine du mois (« x / 7 », %, ronds modifiables). Une semaine est rattachée au mois de son jeudi.
- **Widget** (écran de verrouillage) : carte claire « 🎯 Objectifs », ronds L→D avec la lettre dedans, objectif du mois avec son carré aligné sous le « D ». 6 objectifs max par type. La carte s'ajuste à la hauteur du contenu (le reste de la zone est transparent) ; si la zone est trop petite, le titre est masqué puis « +N autres objectifs » s'affiche.
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
