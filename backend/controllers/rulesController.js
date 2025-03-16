const { createScraper } = require('../scrapers');

/**
 * Contrôleur pour les règlements
 */
class RulesController {
    /**
     * Récupère la liste des règlements pour une chaîne
     * @param {Object} req Requête Express
     * @param {Object} res Réponse Express
     */
    async getChannelRules(req, res) {
        try {
            const { channel } = req.params;
            
            if (!channel) {
                return res.status(400).json({ error: 'Le paramètre channel est requis' });
            }
            
            try {
                const scraper = createScraper(channel);
                const rules = await scraper.getRulesList();
                
                res.json({
                    channel,
                    rules,
                    count: rules.length
                });
            } catch (error) {
                res.status(400).json({ 
                    error: `Scraper non disponible pour la chaîne ${channel}`,
                    availableChannels: ['tf1', 'm6']
                });
            }
        } catch (error) {
            console.error('Erreur lors de la récupération des règlements:', error.message);
            res.status(500).json({
                error: 'Erreur lors de la récupération des règlements',
                details: error.message
            });
        }
    }

    /**
     * Récupère les détails d'un règlement
     * @param {Object} req Requête Express
     * @param {Object} res Réponse Express
     */
    async getRuleDetails(req, res) {
        try {
            const { channel } = req.params;
            const { url } = req.query;
            
            if (!channel) {
                return res.status(400).json({ error: 'Le paramètre channel est requis' });
            }
            
            if (!url) {
                return res.status(400).json({ error: 'Le paramètre url est requis' });
            }
            
            try {
                const scraper = createScraper(channel);
                const ruleDetails = await scraper.getRuleDetails(url);
                
                if (!ruleDetails) {
                    return res.status(404).json({ error: 'Règlement non trouvé' });
                }
                
                res.json(ruleDetails);
            } catch (error) {
                res.status(400).json({ 
                    error: `Scraper non disponible pour la chaîne ${channel}`,
                    availableChannels: ['tf1', 'm6']
                });
            }
        } catch (error) {
            console.error('Erreur lors de la récupération des détails du règlement:', error.message);
            res.status(500).json({
                error: 'Erreur lors de la récupération des détails du règlement',
                details: error.message
            });
        }
    }

    /**
     * Vérifie si un jeu est remboursable
     * @param {Object} req Requête Express
     * @param {Object} res Réponse Express
     */
    async checkGameRefundability(req, res) {
        try {
            const { channel, gameName, date } = req.query;
            
            if (!channel || !gameName) {
                return res.status(400).json({ error: 'Les paramètres channel et gameName sont requis' });
            }
            
            // Pour l'instant, on retourne une réponse statique
            // Dans une version future, on pourrait scraper les règlements pour obtenir cette information
            const isRefundable = true;
            const refundDeadline = 60; // Jours après la diffusion
            const refundAddress = "Service Remboursement Jeux, " + channel.toUpperCase() + ", 75000 Paris";
            
            res.json({
                channel,
                gameName,
                date: date || new Date().toISOString(),
                isRefundable,
                refundDeadline,
                refundAddress,
                requiredDocuments: [
                    "Lettre de demande de remboursement",
                    "Facture téléphonique avec frais surlignés",
                    "RIB (Relevé d'Identité Bancaire)"
                ]
            });
        } catch (error) {
            console.error('Erreur lors de la vérification de remboursabilité:', error.message);
            res.status(500).json({
                error: 'Erreur lors de la vérification de remboursabilité',
                details: error.message
            });
        }
    }
    
    // Alias pour la compatibilité avec server.js
    getRules(req, res) {
        return this.getChannelRules(req, res);
    }
    
    checkRefundable(req, res) {
        return this.checkGameRefundability(req, res);
    }
}

module.exports = new RulesController();