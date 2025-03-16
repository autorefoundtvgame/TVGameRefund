const axios = require('axios');
const cheerio = require('cheerio');
const Game = require('../models/Game');
const config = require('../config');

class TF1GameScraper {
  constructor() {
    this.baseUrl = 'https://www.tf1.fr';
    this.gamesUrl = 'https://www.tf1.fr/tf1/gagnants-reglements-remboursement-des-jeux-tv/news';
  }
  
  async scrapeGames() {
    try {
      console.log('Début du scraping des jeux TF1...');
      
      // Récupérer la page des règlements
      const response = await axios.get(this.gamesUrl);
      const $ = cheerio.load(response.data);
      
      const games = [];
      
      // Parcourir les articles de règlements
      $('.card-news').each((i, element) => {
        try {
          const title = $(element).find('.card-news__title').text().trim();
          const link = $(element).find('a').attr('href');
          const fullLink = link.startsWith('http') ? link : `${this.baseUrl}${link}`;
          
          // Vérifier si c'est un règlement de jeu
          if (title.toLowerCase().includes('règlement') || 
              title.toLowerCase().includes('reglement') || 
              title.toLowerCase().includes('jeu')) {
            
            // Extraire le nom de l'émission
            let showTitle = title.split('«')[1]?.split('»')[0]?.trim();
            if (!showTitle) {
              // Autre méthode d'extraction
              showTitle = title.replace(/règlement/i, '')
                              .replace(/reglement/i, '')
                              .replace(/jeu/i, '')
                              .replace(/interactif/i, '')
                              .trim();
            }
            
            // Créer l'objet jeu
            const game = {
              id: `tf1_${Date.now()}_${i}`,
              showId: this.normalizeShowId(showTitle),
              title: showTitle ? `${showTitle} - Question du jour` : title,
              description: `Jeu interactif de l'émission ${showTitle || title}`,
              type: 'SMS',
              startDate: new Date(),
              rules: fullLink,
              participationMethod: 'Envoyez SMS au 71414 ou appelez le 3680',
              reimbursementMethod: 'Envoyez demande par courrier',
              reimbursementDeadline: 60,
              cost: 0.99,
              phoneNumber: '71414',
              refundAddress: 'TF1 - Service Remboursement, 92100 Boulogne',
              channel: 'TF1'
            };
            
            games.push(game);
            
            // Récupérer plus de détails en visitant la page du règlement
            this.scrapeGameDetails(game, fullLink);
          }
        } catch (error) {
          console.error('Erreur lors du traitement d\'un élément:', error);
        }
      });
      
      console.log(`${games.length} jeux TF1 trouvés`);
      return games;
    } catch (error) {
      console.error('Erreur lors du scraping des jeux TF1:', error);
      return [];
    }
  }
  
  async scrapeGameDetails(game, url) {
    try {
      const response = await axios.get(url);
      const $ = cheerio.load(response.data);
      
      // Extraire le contenu du règlement
      const content = $('.article__body').text();
      
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
        const adresseExtrait = content.substring(remboursementIndex, remboursementIndex + 200);
        
        // Rechercher une adresse postale
        const adresseRegex = /\b\d{5}\s+\w+\b/;
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
            
            // Ajouter l'URL de l'image
            if (show.poster_path) {
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
        title: game.title
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
    console.log(`Scraping TF1 terminé. ${games.length} jeux traités.`);
    return games;
  }
}

module.exports = TF1GameScraper;