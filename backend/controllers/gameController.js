const Game = require('../models/Game');
const axios = require('axios');
const config = require('../config');

// Récupérer tous les jeux avec filtres optionnels
exports.getGames = async (req, res) => {
  try {
    const { 
      showId, 
      channel, 
      startDate, 
      endDate, 
      search,
      limit = 20,
      page = 1
    } = req.query;
    
    const query = {};
    
    // Appliquer les filtres
    if (showId) query.showId = showId;
    if (channel) query.channel = channel;
    
    // Filtre par date
    if (startDate || endDate) {
      query.startDate = {};
      if (startDate) query.startDate.$gte = new Date(startDate);
      if (endDate) query.startDate.$lte = new Date(endDate);
    }
    
    // Recherche textuelle
    if (search) {
      query.$text = { $search: search };
    }
    
    // Pagination
    const skip = (page - 1) * limit;
    
    // Exécuter la requête
    const games = await Game.find(query)
      .sort({ startDate: -1 })
      .skip(skip)
      .limit(parseInt(limit));
      
    const total = await Game.countDocuments(query);
    
    res.json({
      games,
      count: games.length,
      total,
      page: parseInt(page),
      totalPages: Math.ceil(total / limit)
    });
  } catch (error) {
    console.error('Erreur lors de la récupération des jeux:', error);
    res.status(500).json({ error: 'Erreur serveur lors de la récupération des jeux' });
  }
};

// Récupérer un jeu par son ID
exports.getGameById = async (req, res) => {
  try {
    const game = await Game.findOne({ id: req.params.id });
    
    if (!game) {
      return res.status(404).json({ error: 'Jeu non trouvé' });
    }
    
    res.json(game);
  } catch (error) {
    console.error('Erreur lors de la récupération du jeu:', error);
    res.status(500).json({ error: 'Erreur serveur lors de la récupération du jeu' });
  }
};

// Créer un nouveau jeu
exports.createGame = async (req, res) => {
  try {
    const gameData = req.body;
    
    // Générer un ID unique si non fourni
    if (!gameData.id) {
      gameData.id = `game_${Date.now()}_${Math.floor(Math.random() * 1000)}`;
    }
    
    // Rechercher les informations TMDb si showId est fourni mais pas tmdbId
    if (gameData.showId && !gameData.tmdbId && config.tmdbApiKey) {
      try {
        // Extraire le titre de l'émission à partir du titre du jeu
        const showTitle = gameData.title.split(' - ')[0];
        
        // Rechercher l'émission sur TMDb
        const tmdbResponse = await axios.get(
          `https://api.themoviedb.org/3/search/tv?api_key=${config.tmdbApiKey}&query=${encodeURIComponent(showTitle)}`
        );
        
        if (tmdbResponse.data.results && tmdbResponse.data.results.length > 0) {
          const show = tmdbResponse.data.results[0];
          gameData.tmdbId = show.id;
          
          // Ajouter l'URL de l'image si non fournie
          if (!gameData.imageUrl && show.poster_path) {
            gameData.imageUrl = `https://image.tmdb.org/t/p/w500${show.poster_path}`;
          }
        }
      } catch (tmdbError) {
        console.error('Erreur lors de la récupération des données TMDb:', tmdbError);
      }
    }
    
    const newGame = new Game(gameData);
    await newGame.save();
    
    res.status(201).json(newGame);
  } catch (error) {
    console.error('Erreur lors de la création du jeu:', error);
    res.status(500).json({ error: 'Erreur serveur lors de la création du jeu' });
  }
};

// Mettre à jour un jeu existant
exports.updateGame = async (req, res) => {
  try {
    const gameData = req.body;
    gameData.updatedAt = new Date();
    
    const updatedGame = await Game.findOneAndUpdate(
      { id: req.params.id },
      gameData,
      { new: true }
    );
    
    if (!updatedGame) {
      return res.status(404).json({ error: 'Jeu non trouvé' });
    }
    
    res.json(updatedGame);
  } catch (error) {
    console.error('Erreur lors de la mise à jour du jeu:', error);
    res.status(500).json({ error: 'Erreur serveur lors de la mise à jour du jeu' });
  }
};

// Supprimer un jeu
exports.deleteGame = async (req, res) => {
  try {
    const result = await Game.findOneAndDelete({ id: req.params.id });
    
    if (!result) {
      return res.status(404).json({ error: 'Jeu non trouvé' });
    }
    
    res.json({ message: 'Jeu supprimé avec succès' });
  } catch (error) {
    console.error('Erreur lors de la suppression du jeu:', error);
    res.status(500).json({ error: 'Erreur serveur lors de la suppression du jeu' });
  }
};