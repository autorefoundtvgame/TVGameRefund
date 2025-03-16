/**
 * Modèle pour les questions des jeux
 */
class GameQuestion {
    constructor(data = {}) {
        this.id = data.id || null;
        this.gameId = data.gameId || null;
        this.showId = data.showId || null;
        this.question = data.question || '';
        this.options = data.options || [];
        this.correctOption = data.correctOption || null;
        this.createdBy = data.createdBy || null;
        this.createdAt = data.createdAt || new Date().toISOString();
        this.updatedAt = data.updatedAt || new Date().toISOString();
        this.updatedBy = data.updatedBy || null;
        this.status = data.status || 'pending'; // pending, approved, rejected
        this.votes = data.votes || {}; // { optionId: count }
        this.totalVotes = data.totalVotes || 0;
        this.isArchived = data.isArchived || false;
        this.broadcastDate = data.broadcastDate || new Date().toISOString();
        this.editHistory = data.editHistory || [];
    }

    /**
     * Convertit l'objet en JSON
     * @returns {Object} Objet JSON
     */
    toJSON() {
        return {
            id: this.id,
            gameId: this.gameId,
            showId: this.showId,
            question: this.question,
            options: this.options,
            correctOption: this.correctOption,
            createdBy: this.createdBy,
            createdAt: this.createdAt,
            updatedAt: this.updatedAt,
            updatedBy: this.updatedBy,
            status: this.status,
            votes: this.votes,
            totalVotes: this.totalVotes,
            isArchived: this.isArchived,
            broadcastDate: this.broadcastDate,
            editHistory: this.editHistory
        };
    }

    /**
     * Ajoute un vote pour une option
     * @param {string} optionId ID de l'option
     * @param {string} userId ID de l'utilisateur
     * @returns {boolean} Succès de l'opération
     */
    addVote(optionId, userId) {
        // Vérifier si l'option existe
        const optionExists = this.options.some(option => option.id === optionId);
        if (!optionExists) {
            return false;
        }

        // Initialiser le compteur de votes pour cette option si nécessaire
        if (!this.votes[optionId]) {
            this.votes[optionId] = 0;
        }

        // Incrémenter le compteur de votes
        this.votes[optionId]++;
        this.totalVotes++;

        // Mettre à jour la date de mise à jour
        this.updatedAt = new Date().toISOString();
        this.updatedBy = userId;

        return true;
    }

    /**
     * Édite la question
     * @param {string} question Nouvelle question
     * @param {Array} options Nouvelles options
     * @param {string} userId ID de l'utilisateur
     * @returns {boolean} Succès de l'opération
     */
    edit(question, options, userId) {
        // Sauvegarder l'état actuel dans l'historique
        this.editHistory.push({
            question: this.question,
            options: this.options,
            editedBy: userId,
            editedAt: new Date().toISOString()
        });

        // Mettre à jour la question et les options
        this.question = question;
        this.options = options;
        this.updatedAt = new Date().toISOString();
        this.updatedBy = userId;

        return true;
    }

    /**
     * Archive la question
     * @param {string} userId ID de l'utilisateur
     * @returns {boolean} Succès de l'opération
     */
    archive(userId) {
        this.isArchived = true;
        this.updatedAt = new Date().toISOString();
        this.updatedBy = userId;

        return true;
    }

    /**
     * Définit la réponse correcte
     * @param {string} optionId ID de l'option correcte
     * @param {string} userId ID de l'utilisateur
     * @returns {boolean} Succès de l'opération
     */
    setCorrectOption(optionId, userId) {
        // Vérifier si l'option existe
        const optionExists = this.options.some(option => option.id === optionId);
        if (!optionExists) {
            return false;
        }

        this.correctOption = optionId;
        this.updatedAt = new Date().toISOString();
        this.updatedBy = userId;

        return true;
    }
}

module.exports = GameQuestion;