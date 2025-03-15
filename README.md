# TVGameRefund

Une application Android pour automatiser les demandes de remboursement des frais de participation aux jeux télévisés en France.

## Contexte

En France, la législation interdit de proposer des jeux de hasard à la télévision, à l'exception du loto officiel. Pour contourner cette limitation, les chaînes de télévision proposent le remboursement des frais de jeu à qui en fait la demande. Cependant, la procédure est souvent longue et complexe, ce qui décourage la plupart des participants.

Cette application vise à simplifier et automatiser cette procédure de remboursement, permettant aux utilisateurs de récupérer facilement les sommes dépensées pour participer à ces jeux télévisés.

## Objectifs

1. **Suivi des jeux télévisés** : Permettre aux utilisateurs de suivre les jeux télévisés, avec calendrier, descriptions et système de favoris.
2. **Gestion des participations** : Enregistrer les participations aux jeux et suivre leur statut.
3. **Automatisation des remboursements** : 
   - Récupération automatique des factures téléphoniques
   - Édition des factures pour mettre en évidence les frais de jeu
   - Génération de lettres de demande de remboursement
   - Envoi automatisé via des services comme La Poste en ligne

## État actuel du développement

### Architecture

L'application est développée en utilisant :
- **Kotlin** comme langage de programmation
- **Jetpack Compose** pour l'interface utilisateur
- **MVVM** (Model-View-ViewModel) comme pattern d'architecture
- **Hilt** pour l'injection de dépendances
- **Room** pour la persistance des données
- **Retrofit** pour les appels réseau
- **Flow** pour la gestion des données asynchrones

### Fonctionnalités implémentées

1. **Navigation principale** :
   - Barre de navigation en bas avec 5 sections : Jeux, Participations, Factures, Profil, Paramètres
   - Navigation entre les différents écrans

2. **Écran des jeux** :
   - Liste des jeux télévisés
   - Recherche et filtrage des jeux
   - Système de favoris
   - Affichage des informations essentielles (chaîne, date, coût, etc.)

3. **Écran des paramètres** :
   - Configuration des identifiants d'opérateurs téléphoniques
   - Test de connexion aux services des opérateurs

4. **Écrans de base** pour les autres sections :
   - Participations
   - Factures
   - Profil

### Prochaines étapes

1. **Écran de détail des jeux** :
   - Affichage complet des informations sur un jeu
   - Règlement et instructions de remboursement
   - Possibilité d'enregistrer une participation

2. **Récupération des factures** :
   - Connexion à l'API de Free Mobile
   - Téléchargement automatique des factures
   - Analyse des factures pour identifier les frais de jeu

3. **Gestion des participations** :
   - Enregistrement des participations
   - Suivi du statut des demandes de remboursement
   - Notifications pour les étapes importantes

4. **Automatisation des envois** :
   - Intégration avec des services d'envoi de courrier en ligne
   - Génération de lettres types adaptées à chaque jeu
   - Suivi des envois

## Installation et configuration

### Prérequis

- Android Studio Arctic Fox (2020.3.1) ou supérieur
- JDK 17 ou supérieur
- SDK Android 34 (compileSdk)
- SDK Android 26 minimum (minSdk)

### Installation

1. Clonez le dépôt :
```
git clone https://github.com/autorefoundtvgame/TVGameRefund.git
```

2. Ouvrez le projet dans Android Studio

3. Configurez votre fichier `google-services.json` avec vos propres identifiants Firebase

4. Configurez votre clé API TMDb dans le fichier `TMDbRepository.kt`

5. Synchronisez le projet avec les fichiers Gradle

6. Exécutez l'application sur un émulateur ou un appareil physique

## Contribution

Les contributions sont les bienvenues ! N'hésitez pas à ouvrir une issue ou à soumettre une pull request.

## Licence

Ce projet est sous licence MIT - voir le fichier LICENSE pour plus de détails.

## Avertissement

Cette application est fournie à titre informatif et éducatif. Les auteurs ne garantissent pas le succès des demandes de remboursement et ne sont pas responsables de l'utilisation qui pourrait être faite de cette application.