# TVGameRefund

Une application Android pour automatiser les demandes de remboursement des frais de participation aux jeux télévisés en France.

## Structure du projet

Le projet est organisé en deux parties principales :

- **`/android`** : Application Android (frontend)
- **`/backend`** : API REST et serveur (backend)
- **`/docs`** : Documentation du projet

## À propos du développement

Ce projet a été entièrement développé avec l'aide d'Openhands et Claude (Anthropic), sans expérience préalable en développement Android. Il s'agit d'une démonstration de la façon dont l'IA peut aider à créer des applications complexes même sans connaissances techniques préalables dans le domaine spécifique.

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

## Composants du projet

### Application Android

L'application est développée en utilisant :
- **Kotlin** comme langage de programmation
- **Jetpack Compose** pour l'interface utilisateur
- **MVVM** (Model-View-ViewModel) comme pattern d'architecture
- **Hilt** pour l'injection de dépendances
- **Room** pour la persistance des données
- **Retrofit** pour les appels réseau
- **Flow** pour la gestion des données asynchrones

### Backend API

Le backend est développé avec :
- **Node.js** comme environnement d'exécution
- **Express** comme framework web
- **MongoDB** pour la base de données
- **Mongoose** comme ODM (Object Document Mapper)
- **Axios** pour les requêtes HTTP
- **Cheerio** pour le scraping des règlements de jeux

## Installation et configuration

### Prérequis

- Node.js 18+ (pour le backend)
- MongoDB 6+ (pour le backend)
- Android Studio Arctic Fox (2020.3.1) ou supérieur (pour l'application Android)
- JDK 17 ou supérieur (pour l'application Android)
- Docker et Docker Compose (optionnel, pour le déploiement du backend)

### Installation du backend

1. Accédez au dossier backend :
```bash
cd backend
```

2. Installez les dépendances :
```bash
npm install
```

3. Configurez les variables d'environnement :
```bash
cp .env.example .env
# Modifiez le fichier .env avec vos propres valeurs
```

4. Démarrez le serveur :
```bash
npm start
```

Ou avec Docker :
```bash
docker compose up -d
```

### Installation de l'application Android

1. Accédez au dossier android :
```bash
cd android
```

2. Ouvrez le projet dans Android Studio

3. Configurez votre fichier `google-services.json` avec vos propres identifiants Firebase

4. Configurez votre clé API TMDb dans le fichier `local.properties`

5. Synchronisez le projet avec les fichiers Gradle

6. Exécutez l'application sur un émulateur ou un appareil physique

## API Endpoints

Le backend expose les endpoints suivants :

- **GET /api/games** : Récupérer tous les jeux
- **GET /api/games/:id** : Récupérer un jeu spécifique
- **GET /api/games/rules/:channel** : Récupérer les règlements d'une chaîne
- **GET /api/games/refundable** : Vérifier si un jeu est remboursable
- **GET /api/calendar/events** : Récupérer les événements du calendrier
- **GET /api/questions** : Récupérer les questions sur les jeux

## Contribution

Les contributions sont les bienvenues ! N'hésitez pas à ouvrir une issue ou à soumettre une pull request.

Si vous souhaitez contribuer, consultez notre fichier [docs/CONTRIBUTING.md](docs/CONTRIBUTING.md) pour plus d'informations sur le processus de contribution et les standards de code.

## Licence

Ce projet est sous licence MIT - voir le fichier LICENSE pour plus de détails.

## Avertissement

Cette application est fournie à titre informatif et éducatif. Les auteurs ne garantissent pas le succès des demandes de remboursement et ne sont pas responsables de l'utilisation qui pourrait être faite de cette application.