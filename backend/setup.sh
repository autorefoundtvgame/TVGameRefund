#!/bin/bash

# Script d'installation du backend TVGameRefund
echo "Installation du backend TVGameRefund..."

# Créer le répertoire pour le backend
mkdir -p ~/tvgamerefund-backend
cd ~/tvgamerefund-backend

# Copier les fichiers
echo "Copie des fichiers de configuration..."
cat > package.json << 'EOL'
{
  "name": "tvgamerefund-backend",
  "version": "1.0.0",
  "description": "Backend pour l'application TVGameRefund",
  "main": "server.js",
  "scripts": {
    "start": "node server.js",
    "dev": "nodemon server.js"
  },
  "dependencies": {
    "axios": "^1.6.2",
    "cors": "^2.8.5",
    "dotenv": "^16.3.1",
    "express": "^4.18.2",
    "helmet": "^7.1.0",
    "morgan": "^1.10.0"
  },
  "devDependencies": {
    "nodemon": "^3.0.2"
  }
}
EOL

cat > .env << 'EOL'
# Configuration du serveur
PORT=3000

# Clés API TMDb
TMDB_API_KEY=your_tmdb_api_key_here
TMDB_ACCESS_TOKEN=your_tmdb_access_token_here
EOL

cat > server.js << 'EOL'
require('dotenv').config();
const express = require('express');
const cors = require('cors');
const helmet = require('helmet');
const morgan = require('morgan');
const axios = require('axios');

// Configuration
const PORT = process.env.PORT || 3000;
const TMDB_API_KEY = process.env.TMDB_API_KEY;
const TMDB_ACCESS_TOKEN = process.env.TMDB_ACCESS_TOKEN;
const TMDB_BASE_URL = 'https://api.themoviedb.org/3';

// Initialisation de l'application Express
const app = express();

// Middleware
app.use(helmet()); // Sécurité
app.use(cors()); // Autoriser les requêtes cross-origin
app.use(express.json()); // Parser le JSON
app.use(morgan('combined')); // Logging

// Route de test
app.get('/', (req, res) => {
  res.json({ message: 'API TVGameRefund opérationnelle' });
});

// Proxy pour les recherches TMDb
app.get('/api/tmdb/search/tv', async (req, res) => {
  try {
    const query = req.query.query;
    
    if (!query) {
      return res.status(400).json({ error: 'Le paramètre query est requis' });
    }
    
    const response = await axios.get(`${TMDB_BASE_URL}/search/tv`, {
      params: {
        query: query
      },
      headers: {
        'Authorization': `Bearer ${TMDB_ACCESS_TOKEN}`
      }
    });
    
    res.json(response.data);
  } catch (error) {
    console.error('Erreur lors de la recherche TMDb:', error.message);
    res.status(error.response?.status || 500).json({
      error: 'Erreur lors de la recherche TMDb',
      details: error.response?.data || error.message
    });
  }
});

// Proxy pour les détails d'une émission TMDb
app.get('/api/tmdb/tv/:id', async (req, res) => {
  try {
    const id = req.params.id;
    
    const response = await axios.get(`${TMDB_BASE_URL}/tv/${id}`, {
      headers: {
        'Authorization': `Bearer ${TMDB_ACCESS_TOKEN}`
      }
    });
    
    res.json(response.data);
  } catch (error) {
    console.error('Erreur lors de la récupération des détails TMDb:', error.message);
    res.status(error.response?.status || 500).json({
      error: 'Erreur lors de la récupération des détails TMDb',
      details: error.response?.data || error.message
    });
  }
});

// Démarrage du serveur
app.listen(PORT, '0.0.0.0', () => {
  console.log(`Serveur démarré sur le port ${PORT}`);
});
EOL

cat > Dockerfile << 'EOL'
FROM node:18-alpine

WORKDIR /app

COPY package*.json ./

RUN npm install --production

COPY . .

EXPOSE 3000

CMD ["node", "server.js"]
EOL

cat > docker-compose.yml << 'EOL'
version: '3'

services:
  tvgamerefund-api:
    build: .
    container_name: tvgamerefund-api
    restart: always
    ports:
      - "3000:3000"
    environment:
      - PORT=3000
      - TMDB_API_KEY=${TMDB_API_KEY}
      - TMDB_ACCESS_TOKEN=${TMDB_ACCESS_TOKEN}
    volumes:
      - ./logs:/app/logs
EOL

# Créer le répertoire pour les logs
mkdir -p logs

# Démarrer le conteneur Docker
echo "Démarrage du conteneur Docker..."
docker-compose up -d

# Vérifier que le conteneur est bien démarré
echo "Vérification du statut du conteneur..."
docker ps | grep tvgamerefund-api

echo "Installation terminée. Le backend est accessible sur le port 3000."
echo "Pour vérifier le fonctionnement, exécutez: curl http://localhost:3000"