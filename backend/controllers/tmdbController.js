const axios = require('axios');
const config = require('../config');

/**
 * Recherche une émission TV par son nom
 * @param {Object} req Requête Express
 * @param {Object} res Réponse Express
 */
exports.searchTvShows = async (req, res) => {
    try {
        const query = req.query.query;
        const tmdbApiKey = config.tmdbApiKey;
        
        if (!tmdbApiKey) {
            return res.status(500).json({ error: 'Clé API TMDb non configurée' });
        }
        
        const response = await axios.get(
            `https://api.themoviedb.org/3/search/tv?api_key=${tmdbApiKey}&query=${encodeURIComponent(query)}`
        );
        
        res.json(response.data);
    } catch (error) {
        console.error('Erreur lors de la recherche TMDb:', error);
        res.status(500).json({ error: 'Erreur lors de la recherche TMDb' });
    }
};

/**
 * Obtient les détails d'une émission TV
 * @param {Object} req Requête Express
 * @param {Object} res Réponse Express
 */
exports.getTvShowDetails = async (req, res) => {
    try {
        const id = req.params.id;
        const tmdbApiKey = config.tmdbApiKey;
        
        if (!tmdbApiKey) {
            return res.status(500).json({ error: 'Clé API TMDb non configurée' });
        }
        
        const response = await axios.get(
            `https://api.themoviedb.org/3/tv/${id}?api_key=${tmdbApiKey}`
        );
        
        res.json(response.data);
    } catch (error) {
        console.error('Erreur lors de la récupération des détails TMDb:', error);
        res.status(500).json({ error: 'Erreur lors de la récupération des détails TMDb' });
    }
};