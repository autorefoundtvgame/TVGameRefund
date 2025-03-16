const TF1Scraper = require('./tf1Scraper');
const M6Scraper = require('./m6Scraper');
const TF1GameScraper = require('./tf1GameScraper');
const M6GameScraper = require('./m6GameScraper');
const cron = require('node-cron');

/**
 * Factory pour créer un scraper en fonction de la chaîne
 * @param {string} channel Code de la chaîne (tf1, m6, etc.)
 * @returns {Object} Instance du scraper correspondant
 */
function createScraper(channel) {
    switch (channel.toLowerCase()) {
        case 'tf1':
            return new TF1Scraper();
        case 'm6':
            return new M6Scraper();
        default:
            throw new Error(`Scraper non disponible pour la chaîne ${channel}`);
    }
}

/**
 * Factory pour créer un scraper de jeux en fonction de la chaîne
 * @param {string} channel Code de la chaîne (tf1, m6, etc.)
 * @returns {Object} Instance du scraper de jeux correspondant
 */
function createGameScraper(channel) {
    switch (channel.toLowerCase()) {
        case 'tf1':
            return new TF1GameScraper();
        case 'm6':
            return new M6GameScraper();
        default:
            throw new Error(`Scraper de jeux non disponible pour la chaîne ${channel}`);
    }
}

/**
 * Exécute tous les scrapers de jeux
 */
async function runAllGameScrapers() {
    console.log('Exécution de tous les scrapers de jeux...');
    
    try {
        // TF1
        const tf1GameScraper = new TF1GameScraper();
        await tf1GameScraper.run();
        
        // M6
        const m6GameScraper = new M6GameScraper();
        await m6GameScraper.run();
        
        console.log('Tous les scrapers de jeux ont terminé leur exécution');
    } catch (error) {
        console.error('Erreur lors de l\'exécution des scrapers de jeux:', error);
    }
}

// Planifier l'exécution des scrapers de jeux tous les jours à minuit
if (process.env.NODE_ENV === 'production') {
    cron.schedule('0 0 * * *', () => {
        console.log('Exécution planifiée des scrapers de jeux');
        runAllGameScrapers();
    });
}

module.exports = {
    createScraper,
    createGameScraper,
    runAllGameScrapers,
    TF1Scraper,
    M6Scraper,
    TF1GameScraper,
    M6GameScraper
};