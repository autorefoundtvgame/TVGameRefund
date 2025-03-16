require('dotenv').config();
const express = require('express');
const mongoose = require('mongoose');
const cors = require('cors');
const helmet = require('helmet');
const morgan = require('morgan');
const fs = require('fs');
const path = require('path');
const axios = require('axios');
const config = require('./config');

// Configuration
const PORT = config.port;
const TMDB_API_KEY = config.tmdbApiKey;
const TMDB_ACCESS_TOKEN = config.tmdbAccessToken;
const TMDB_BASE_URL = 'https://api.themoviedb.org/3';

// Importer les contrôleurs
const rulesController = require('./controllers/rulesController');
let calendarController;
let questionController;
let gameController;
let tmdbController;

try {
    calendarController = require('./controllers/calendarController');
    console.log('Contrôleur du calendrier chargé avec succès');
} catch (error) {
    console.error('Erreur lors du chargement du contrôleur du calendrier:', error.message);
    // Créer un contrôleur de secours
    calendarController = {
        getEvents: (req, res) => {
            res.json({ events: [], count: 0 });
        },
        createEvent: (req, res) => {
            res.status(201).json(req.body);
        },
        updateEvent: (req, res) => {
            res.json(req.body);
        },
        deleteEvent: (req, res) => {
            res.json({ message: 'Événement supprimé avec succès' });
        }
    };
}

try {
    questionController = require('./controllers/questionController');
    console.log('Contrôleur des questions chargé avec succès');
} catch (error) {
    console.error('Erreur lors du chargement du contrôleur des questions:', error.message);
    // Créer un contrôleur de secours
    questionController = {
        getQuestions: (req, res) => {
            res.json({ questions: [], count: 0 });
        },
        createQuestion: (req, res) => {
            res.status(201).json(req.body);
        },
        updateQuestion: (req, res) => {
            res.json(req.body);
        },
        addVote: (req, res) => {
            res.json({ message: 'Vote ajouté avec succès' });
        },
        archiveQuestion: (req, res) => {
            res.json({ message: 'Question archivée avec succès' });
        },
        setCorrectOption: (req, res) => {
            res.json({ message: 'Réponse correcte définie avec succès' });
        }
    };
}

// Initialisation de l'application Express
const app = express();

// Middleware
app.use(helmet()); // Sécurité
app.use(cors()); // Autoriser les requêtes cross-origin
app.use(express.json()); // Parser le JSON
app.use(express.urlencoded({ extended: true }));

// Configuration des logs
const logsDir = path.join(__dirname, 'logs');
if (!fs.existsSync(logsDir)) {
  fs.mkdirSync(logsDir);
}
const accessLogStream = fs.createWriteStream(
  path.join(logsDir, 'access.log'),
  { flags: 'a' }
);
app.use(morgan('combined', { stream: accessLogStream }));

// Connexion à MongoDB
mongoose.connect(config.mongoUri)
  .then(() => console.log('Connexion à MongoDB établie'))
  .catch(err => console.error('Erreur de connexion à MongoDB:', err));

// Route de test
app.get('/', (req, res) => {
  res.json({ message: 'API TVGameRefund opérationnelle' });
});

// Routes pour TMDb
try {
  tmdbController = require('./controllers/tmdbController');
  console.log('Contrôleur TMDb chargé avec succès');
  
  app.get('/api/tmdb/search/tv', tmdbController.searchTvShows);
  app.get('/api/tmdb/tv/:id', tmdbController.getTvShowDetails);
} catch (error) {
  console.error('Erreur lors du chargement du contrôleur TMDb:', error.message);
  
  // Proxy pour les recherches TMDb (fallback)
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
  
  // Proxy pour les détails d'une émission TMDb (fallback)
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
}

// Routes pour les règlements et la remboursabilité
app.get('/api/games/rules/:channel', rulesController.getChannelRules);
app.get('/api/games/rules/:channel/details', rulesController.getRuleDetails);
app.get('/api/games/refundable', rulesController.checkGameRefundability);

// Routes pour les jeux
try {
  gameController = require('./controllers/gameController');
  console.log('Contrôleur des jeux chargé avec succès');
  
  app.get('/api/games', gameController.getGames);
  app.get('/api/games/:id', gameController.getGameById);
  app.post('/api/games', gameController.createGame);
  app.put('/api/games/:id', gameController.updateGame);
  app.delete('/api/games/:id', gameController.deleteGame);
} catch (error) {
  console.error('Erreur lors du chargement du contrôleur des jeux:', error.message);
}

// Routes pour le calendrier
app.get('/api/calendar/events', calendarController.getEvents);
app.post('/api/calendar/events', calendarController.createEvent);
app.put('/api/calendar/events/:id', calendarController.updateEvent);
app.delete('/api/calendar/events/:id', calendarController.deleteEvent);

// Routes pour les questions et votes
app.get('/api/questions', questionController.getQuestions);
app.post('/api/questions', questionController.createQuestion);
app.put('/api/questions/:id', questionController.updateQuestion);
app.post('/api/questions/:id/vote', questionController.addVote);
app.post('/api/questions/:id/archive', questionController.archiveQuestion);
app.post('/api/questions/:id/correct-option', questionController.setCorrectOption);

// Endpoint pour obtenir les chaînes disponibles
app.get('/api/channels', (req, res) => {
  const channels = [
    {
      id: 'tf1',
      name: 'TF1',
      logo: 'https://upload.wikimedia.org/wikipedia/commons/thumb/d/dc/TF1_logo_2013.png/800px-TF1_logo_2013.png',
      rulesUrl: 'https://www.tf1.fr/tf1/gagnants-reglements-remboursement-des-jeux-tv/news'
    },
    {
      id: 'm6',
      name: 'M6',
      logo: 'https://upload.wikimedia.org/wikipedia/commons/thumb/4/4a/Logo_M6_%282020%2C_fond_clair%29.svg/800px-Logo_M6_%282020%2C_fond_clair%29.svg.png',
      rulesUrl: 'https://etvous.m6.fr/jeux-concours-antenne-reglements'
    },
    {
      id: 'france2',
      name: 'France 2',
      logo: 'https://upload.wikimedia.org/wikipedia/commons/thumb/5/53/France_2_2018.svg/800px-France_2_2018.svg.png',
      rulesUrl: 'https://www.france.tv/france-2/jeux/reglement/'
    },
    {
      id: 'france3',
      name: 'France 3',
      logo: 'https://upload.wikimedia.org/wikipedia/commons/thumb/d/dd/France_3_2018.svg/800px-France_3_2018.svg.png',
      rulesUrl: 'https://www.france.tv/france-3/jeux/reglement/'
    },
    {
      id: 'c8',
      name: 'C8',
      logo: 'https://upload.wikimedia.org/wikipedia/commons/thumb/8/8f/Logo_C8_2016.svg/800px-Logo_C8_2016.svg.png',
      rulesUrl: 'https://www.c8.fr/reglement-jeux'
    }
  ];
  
  res.json({
    channels,
    count: channels.length
  });
});

// Démarrage du serveur
app.listen(PORT, '0.0.0.0', () => {
  console.log(`Serveur démarré sur le port ${PORT}`);
  
  // Écrire le PID dans un fichier pour pouvoir arrêter le serveur facilement
  fs.writeFileSync(path.join(__dirname, 'server.pid'), process.pid.toString());
  
  // Démarrer les scrapers de jeux
  if (config.environment === 'production') {
    console.log('Démarrage des scrapers de jeux...');
    try {
      const { runAllGameScrapers } = require('./scrapers');
      runAllGameScrapers();
    } catch (error) {
      console.error('Erreur lors du démarrage des scrapers:', error);
    }
  }
});

// Gérer l'arrêt propre du serveur
process.on('SIGINT', () => {
  console.log('Arrêt du serveur...');
  process.exit(0);
});

process.on('SIGTERM', () => {
  console.log('Arrêt du serveur...');
  process.exit(0);
});