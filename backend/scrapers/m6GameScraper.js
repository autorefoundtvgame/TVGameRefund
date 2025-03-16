const axios = require('axios');
const cheerio = require('cheerio');
const Game = require('../models/Game');
const config = require('../config');

class M6GameScraper {
  constructor() {
    this.baseUrl = 'https://www.m6.fr';
    this.gamesUrl = 'https://www.m6.fr/jeux-concours/';
  }
  
  async scrapeGames() {
    try {
      console.log('Début du scraping des jeux M6...');
      
      // Récupérer la page des jeux concours
      const response = await axios.get(this.gamesUrl);
      const $ = cheerio.load(response.data);
      
      const games = [];
      
      // Parcourir les articles de jeux
      $('.game-item').each((i, element) => {
        try {
          const title = $(element).find('.game-item__title').text().trim();
          const link = $(element).find('a').attr('href');
          const fullLink = link.startsWith('http') ? link : `${this.baseUrl}${link}`;
          const imageUrl = $(element).find('img').attr('src') || $(element).find('img').attr('data-src');
          
          // Extraire le nom de l'émission
          let showTitle = title.split('-')[0]?.trim();
          if (!showTitle) {
            showTitle = title;
          }
          
          // Créer l'objet jeu
          const game = {
            id: `m6_${Date.now()}_${i}`,
            showId: this.normalizeShowId(showTitle),
            title: `${showTitle} - Question du jour`,
            description: `Jeu interactif de l'émission ${showTitle}`,
            type: 'SMS',
            startDate: new Date(),
            rules: fullLink,
            imageUrl: imageUrl,
            participationMethod: 'Envoyez SMS au 74000 ou appelez le 3626',
            reimbursementMethod: 'Envoyez demande par courrier',
            reimbursementDeadline: 60,
            cost: 0.99,
            phoneNumber: '74000',
            refundAddress: 'M6 - Service Remboursement, 89 avenue Charles de Gaulle, 92575 Neuilly-sur-Seine',
            channel: 'M6'
          };
          
          games.push(game);
          
          // Récupérer plus de détails en visitant la page du jeu
          this.scrapeGameDetails(game, fullLink);
        } catch (error) {
          console.error('Erreur lors du traitement d\'un élément:', error);
        }
      });
      
      console.log(`${games.length} jeux M6 trouvés`);
      return games;
    } catch (error) {
      console.error('Erreur lors du scraping des jeux M6:', error);
      return [];
    }
  }
  
  async scrapeGameDetails(game, url) {
    try {
      const response = await axios.get(url);
      const $ = cheerio.load(response.data);
      
      // Extraire le contenu du règlement
      const content = $('.game-rules').text() || $('.game-content').text();
      
      // Rechercher les numéros de téléphone
      const phoneRegex = /\b(3[0-9]{3}|7[0-9]{4})\b/g;
      const phones = content.match(phoneRegex);
      
      if (phones && phones.length > 0) {
        // Utiliser le premier numéro trouvé
        game.phoneNumber = phones[0];
      }
      
      // Rechercher les coûts
      const costRegex = /(\d+[.,]\d{2})\s*€/g;
      const costs = content.match(costRegex);
      
      if (costs && costs.length > 0) {
        // Utiliser le premier coût trouvé
        const cost = costs[0].replace('€', '').replace(',', '.').trim();
        game.cost = parseFloat(cost);
      }
      
      // Rechercher l'adresse de remboursement
      if (content.includes('remboursement')) {
        const remboursementIndex = content.indexOf('remboursement');
        const adresseExtrait = content.substring(remboursementIndex, remboursementIndex + 300);
        
        // Rechercher une adresse postale
        const adresseRegex = /\b\d{5}\s+\w+(-\w+)*\b/;
        const adresseMatch = adresseExtrait.match(adresseRegex);
        
        if (adresseMatch) {
          game.refundAddress = adresseMatch[0];
        }
      }
      
      // Mettre à jour le jeu dans la base de données
      await this.saveOrUpdateGame(game);
      
    } catch (error) {
      console.error(`Erreur lors du scraping des détails du jeu ${game.title}:`, error);
    }
  }
  
  normalizeShowId(showTitle) {
    if (!showTitle) return 'unknown';
    
    return showTitle.toLowerCase()
      .replace(/[^a-z0-9]/g, '-')
      .replace(/-+/g, '-')
      .replace(/^-|-$/g, '');
  }
  
  async saveOrUpdateGame(game) {
    try {
      // Rechercher les informations TMDb
      if (config.tmdbApiKey) {
        try {
          const tmdbResponse = await axios.get(
            `https://api.themoviedb.org/3/search/tv?api_key=${config.tmdbApiKey}&query=${encodeURIComponent(game.title.split(' - ')[0])}`
          );
          
          if (tmdbResponse.data.results && tmdbResponse.data.results.length > 0) {
            const show = tmdbResponse.data.results[0];
            game.tmdbId = show.id;
            
            // Ajouter l'URL de l'image si non déjà définie
            if (!game.imageUrl && show.poster_path) {
              game.imageUrl = `https://image.tmdb.org/t/p/w500${show.poster_path}`;
            }
          }
        } catch (tmdbError) {
          console.error('Erreur lors de la récupération des données TMDb:', tmdbError);
        }
      }
      
      // Vérifier si le jeu existe déjà
      const existingGame = await Game.findOne({ 
        showId: game.showId,
        title: game.title,
        channel: 'M6'
      });
      
      if (existingGame) {
        // Mettre à jour le jeu existant
        await Game.updateOne(
          { _id: existingGame._id },
          { 
            ...game,
            updatedAt: new Date()
          }
        );
        console.log(`Jeu mis à jour: ${game.title}`);
      } else {
        // Créer un nouveau jeu
        const newGame = new Game(game);
        await newGame.save();
        console.log(`Nouveau jeu créé: ${game.title}`);
      }
    } catch (error) {
      console.error(`Erreur lors de la sauvegarde du jeu ${game.title}:`, error);
    }
  }
  
  async run() {
    const games = await this.scrapeGames();
    console.log(`Scraping M6 terminé. ${games.length} jeux traités.`);
    return games;
  }
}

module.exports = M6GameScraper;