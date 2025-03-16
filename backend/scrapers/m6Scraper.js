const axios = require('axios');
const cheerio = require('cheerio');
const { PDFExtract } = require('pdf.js-extract');
const pdfExtract = new PDFExtract();

/**
 * Scraper pour les règlements de M6
 */
class M6Scraper {
    constructor() {
        this.baseUrl = 'https://etvous.m6.fr/jeux-concours-antenne-reglements';
        this.rulesUrl = 'https://etvous.m6.fr/jeux-concours-antenne-reglements';
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
            $('a[href*="reglement"]').each((index, element) => {
                const title = $(element).text().trim();
                const link = $(element).attr('href');
                
                // Ne garder que les règlements (pas les listes de gagnants)
                if (title.toLowerCase().includes('règlement') || title.toLowerCase().includes('reglement')) {
                    rules.push({
                        title,
                        link,
                        channel: 'm6'
                    });
                }
            });
            
            return rules;
        } catch (error) {
            console.error('Erreur lors de la récupération des règlements M6:', error.message);
            return [];
        }
    }

    /**
     * Récupère les détails d'un règlement spécifique
     * @param {string} ruleUrl URL du règlement (PDF)
     * @returns {Promise<Object>} Détails du règlement
     */
    async getRuleDetails(ruleUrl) {
        try {
            // Télécharger le PDF
            const response = await axios.get(ruleUrl, { responseType: 'arraybuffer' });
            const pdfBuffer = Buffer.from(response.data);
            
            // Extraire le texte du PDF
            const pdfData = await pdfExtract.extractBuffer(pdfBuffer, {});
            const content = pdfData.pages.map(page => page.content.map(item => item.str).join(' ')).join('\n');
            
            // Extraire les informations de remboursement
            const refundInfo = this.extractRefundInfo(content);
            
            return {
                url: ruleUrl,
                content,
                refundInfo,
                channel: 'm6'
            };
        } catch (error) {
            console.error('Erreur lors de la récupération des détails du règlement M6:', error.message);
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

module.exports = M6Scraper;