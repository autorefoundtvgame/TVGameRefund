const axios = require('axios');
const cheerio = require('cheerio');

/**
 * Scraper pour les règlements de TF1
 */
class TF1Scraper {
    constructor() {
        this.baseUrl = 'https://www.tf1.fr/tf1/gagnants-reglements-remboursement-des-jeux-tv/news';
        this.rulesUrl = 'https://www.tf1.fr/tf1/gagnants-reglements-remboursement-des-jeux-tv/news';
    }

    /**
     * Récupère la liste des règlements disponibles
     * @returns {Promise<Array>} Liste des règlements
     */
    async getRulesList() {
        try {
            const response = await axios.get(this.rulesUrl);
            const $ = cheerio.load(response.data);
            
            const rules = [];
            
            // Sélectionner les éléments contenant les règlements
            $('.card-news').each((index, element) => {
                const title = $(element).find('.card-news__title').text().trim();
                const link = $(element).find('a').attr('href');
                const date = $(element).find('.card-news__date').text().trim();
                
                // Ne garder que les règlements (pas les listes de gagnants)
                if (title.toLowerCase().includes('règlement') || title.toLowerCase().includes('reglement')) {
                    rules.push({
                        title,
                        link: link ? `https://www.tf1.fr${link}` : null,
                        date,
                        channel: 'tf1'
                    });
                }
            });
            
            return rules;
        } catch (error) {
            console.error('Erreur lors de la récupération des règlements TF1:', error.message);
            return [];
        }
    }

    /**
     * Récupère les détails d'un règlement spécifique
     * @param {string} ruleUrl URL du règlement
     * @returns {Promise<Object>} Détails du règlement
     */
    async getRuleDetails(ruleUrl) {
        try {
            const response = await axios.get(ruleUrl);
            const $ = cheerio.load(response.data);
            
            // Extraire le contenu du règlement
            const content = $('.article__content').text().trim();
            
            // Extraire les informations de remboursement
            const refundInfo = this.extractRefundInfo(content);
            
            return {
                url: ruleUrl,
                content,
                refundInfo,
                channel: 'tf1'
            };
        } catch (error) {
            console.error('Erreur lors de la récupération des détails du règlement TF1:', error.message);
            return null;
        }
    }

    /**
     * Extrait les informations de remboursement du contenu du règlement
     * @param {string} content Contenu du règlement
     * @returns {Object} Informations de remboursement
     */
    extractRefundInfo(content) {
        // Rechercher les informations de remboursement dans le contenu
        const refundSection = this.findRefundSection(content);
        
        if (!refundSection) {
            return {
                isRefundable: false,
                reason: 'Section de remboursement non trouvée'
            };
        }
        
        // Extraire l'adresse de remboursement
        const addressMatch = refundSection.match(/adresser\s+(?:par\s+)?(?:courrier\s+)?(?:postal\s+)?(?:à\s+)?:?\s*([^\.]+)/i);
        const address = addressMatch ? addressMatch[1].trim() : null;
        
        // Extraire le délai de remboursement
        const deadlineMatch = refundSection.match(/dans\s+(?:un\s+)?délai\s+(?:de\s+)?(\d+)(?:\s+jours)?/i);
        const deadline = deadlineMatch ? parseInt(deadlineMatch[1]) : 60; // Par défaut 60 jours
        
        // Extraire les documents requis
        const requiredDocs = [];
        
        if (refundSection.match(/facture\s+(?:détaillée|téléphonique)/i)) {
            requiredDocs.push('Facture téléphonique avec frais surlignés');
        }
        
        if (refundSection.match(/RIB|Relevé\s+d['']Identité\s+Bancaire/i)) {
            requiredDocs.push('RIB (Relevé d\'Identité Bancaire)');
        }
        
        if (refundSection.match(/demande\s+(?:écrite|manuscrite)|lettre/i)) {
            requiredDocs.push('Lettre de demande de remboursement');
        }
        
        return {
            isRefundable: true,
            address,
            deadline,
            requiredDocs,
            rawText: refundSection
        };
    }

    /**
     * Trouve la section de remboursement dans le contenu du règlement
     * @param {string} content Contenu du règlement
     * @returns {string|null} Section de remboursement ou null si non trouvée
     */
    findRefundSection(content) {
        // Rechercher les sections qui parlent de remboursement
        const remboursementMatch = content.match(/(?:ARTICLE|REMBOURSEMENT)[^\.]+(?:REMBOURSEMENT|FRAIS)[^\.]+\.(?:[^\.]|\.(?!ARTICLE))+/i);
        
        if (remboursementMatch) {
            return remboursementMatch[0].trim();
        }
        
        return null;
    }
}

module.exports = TF1Scraper;