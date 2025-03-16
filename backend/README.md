# Backend TVGameRefund

Ce backend sert de proxy pour les requêtes à l'API TMDb et fournit une API pour l'application mobile TVGameRefund.

## Installation sur le serveur

1. Connectez-vous à votre serveur via SSH :
   ```
   ssh user@example.com
   ```

2. Créez un répertoire pour le backend :
   ```
   mkdir -p ~/tvgamerefund-backend
   cd ~/tvgamerefund-backend
   ```

3. Copiez tous les fichiers du répertoire `/workspace/backend-setup` vers le serveur.

4. Rendez le script d'installation exécutable :
   ```
   chmod +x setup.sh
   ```

5. Exécutez le script d'installation :
   ```
   ./setup.sh
   ```

## Configuration manuelle (si le script ne fonctionne pas)

1. Installez les dépendances :
   ```
   npm install
   ```

2. Démarrez le serveur :
   ```
   npm start
   ```

3. Pour utiliser Docker (recommandé) :
   ```
   docker-compose up -d
   ```

## Vérification

Pour vérifier que le serveur fonctionne correctement :
```
curl http://localhost:3000
```

Vous devriez recevoir une réponse JSON : `{"message":"API TVGameRefund opérationnelle"}`

## Configuration de l'application mobile

Dans le fichier `local.properties` de l'application Android, mettez à jour l'URL du backend :
```
backend.url=http://example.com:3000
```

## Endpoints API

- `GET /` - Vérification de l'état du serveur
- `GET /api/tmdb/search/tv?query=QUERY` - Recherche d'émissions TV
- `GET /api/tmdb/tv/:id` - Détails d'une émission TV
- `GET /api/games/rules/:channel` - Liste des règlements pour une chaîne
- `GET /api/games/rules/:channel/details` - Détails d'un règlement
- `GET /api/games/refundable` - Vérification de la remboursabilité d'un jeu

### Nouveaux endpoints (Calendrier)

- `GET /api/calendar/events` - Obtient les événements du calendrier
- `POST /api/calendar/events` - Crée un événement dans le calendrier
- `PUT /api/calendar/events/:id` - Met à jour un événement du calendrier
- `DELETE /api/calendar/events/:id` - Supprime un événement du calendrier

### Nouveaux endpoints (Questions et votes)

- `GET /api/questions` - Obtient les questions pour un jeu
- `POST /api/questions` - Crée une question pour un jeu
- `PUT /api/questions/:id` - Met à jour une question
- `POST /api/questions/:id/vote` - Ajoute un vote pour une option
- `POST /api/questions/:id/archive` - Archive une question
- `POST /api/questions/:id/correct-option` - Définit la réponse correcte pour une question