/**
 * Contrôleur pour les questions des jeux
 */
class QuestionController {
    constructor() {
        // Simuler une base de données avec un tableau de questions
        this.questions = [
            {
                id: '1',
                gameId: 'KohLanta',
                showId: '58237',
                question: 'Qui sera éliminé ce soir ?',
                options: [
                    { id: '1', text: 'Claude' },
                    { id: '2', text: 'Teheiura' }
                ],
                createdBy: 'user1',
                createdAt: '2025-03-18T21:30:00.000Z',
                updatedAt: '2025-03-18T21:30:00.000Z',
                status: 'approved',
                votes: { '1': 10, '2': 5 },
                totalVotes: 15,
                isArchived: false,
                broadcastDate: '2025-03-18T21:00:00.000Z',
                editHistory: []
            },
            {
                id: '2',
                gameId: 'TheVoice',
                showId: '42178',
                question: 'Quel candidat sera sélectionné ?',
                options: [
                    { id: '1', text: 'Candidat A' },
                    { id: '2', text: 'Candidat B' }
                ],
                createdBy: 'user2',
                createdAt: '2025-03-19T20:30:00.000Z',
                updatedAt: '2025-03-19T20:30:00.000Z',
                status: 'approved',
                votes: { '1': 8, '2': 12 },
                totalVotes: 20,
                isArchived: false,
                broadcastDate: '2025-03-19T20:00:00.000Z',
                editHistory: []
            }
        ];
    }

    /**
     * Obtient les questions pour un jeu
     * @param {Object} req Requête Express
     * @param {Object} res Réponse Express
     */
    getQuestions(req, res) {
        try {
            const { gameId, showId, date, status } = req.query;
            
            // Filtrer les questions
            let filteredQuestions = this.questions;
            
            if (gameId) {
                filteredQuestions = filteredQuestions.filter(question => question.gameId === gameId);
            }
            
            if (showId) {
                filteredQuestions = filteredQuestions.filter(question => question.showId === showId);
            }
            
            if (date) {
                const targetDate = new Date(date);
                const targetDateString = targetDate.toISOString().split('T')[0];
                
                filteredQuestions = filteredQuestions.filter(question => {
                    const broadcastDate = new Date(question.broadcastDate);
                    const broadcastDateString = broadcastDate.toISOString().split('T')[0];
                    return broadcastDateString === targetDateString;
                });
            }
            
            if (status) {
                filteredQuestions = filteredQuestions.filter(question => question.status === status);
            }
            
            res.json({
                questions: filteredQuestions.map(question => question.toJSON()),
                count: filteredQuestions.length
            });
        } catch (error) {
            console.error('Erreur lors de la récupération des questions:', error.message);
            res.status(500).json({
                error: 'Erreur lors de la récupération des questions',
                details: error.message
            });
        }
    }

    /**
     * Crée une question pour un jeu
     * @param {Object} req Requête Express
     * @param {Object} res Réponse Express
     */
    createQuestion(req, res) {
        try {
            const { gameId, showId, question, options, broadcastDate, userId } = req.body;
            
            // Vérifier si les paramètres requis sont présents
            if (!gameId || !showId || !question || !options || !broadcastDate || !userId) {
                return res.status(400).json({ 
                    error: 'Paramètres manquants',
                    requiredParams: ['gameId', 'showId', 'question', 'options', 'broadcastDate', 'userId']
                });
            }
            
            // Vérifier si une question existe déjà pour ce jeu à cette date
            const existingQuestion = this.questions.find(q => 
                q.gameId === gameId && 
                new Date(q.broadcastDate).toISOString().split('T')[0] === new Date(broadcastDate).toISOString().split('T')[0]
            );
            
            if (existingQuestion) {
                return res.status(409).json({ 
                    error: 'Une question existe déjà pour ce jeu à cette date',
                    existingQuestion: existingQuestion.toJSON()
                });
            }
            
            // Créer la question
            const newQuestion = new GameQuestion({
                id: (this.questions.length + 1).toString(),
                gameId,
                showId,
                question,
                options: options.map((option, index) => ({ id: (index + 1).toString(), text: option })),
                createdBy: userId,
                status: 'pending',
                broadcastDate
            });
            
            // Ajouter la question à la liste
            this.questions.push(newQuestion);
            
            res.status(201).json(newQuestion.toJSON());
        } catch (error) {
            console.error('Erreur lors de la création de la question:', error.message);
            res.status(500).json({
                error: 'Erreur lors de la création de la question',
                details: error.message
            });
        }
    }

    /**
     * Met à jour une question
     * @param {Object} req Requête Express
     * @param {Object} res Réponse Express
     */
    updateQuestion(req, res) {
        try {
            const { id } = req.params;
            const { question, options, userId } = req.body;
            
            // Trouver la question
            const questionIndex = this.questions.findIndex(q => q.id === id);
            
            if (questionIndex === -1) {
                return res.status(404).json({ error: 'Question non trouvée' });
            }
            
            const questionObj = this.questions[questionIndex];
            
            // Vérifier si la question est archivée
            if (questionObj.isArchived) {
                return res.status(403).json({ error: 'Impossible de modifier une question archivée' });
            }
            
            // Mettre à jour la question
            questionObj.edit(
                question || questionObj.question,
                options ? options.map((option, index) => ({ id: (index + 1).toString(), text: option })) : questionObj.options,
                userId
            );
            
            res.json(questionObj.toJSON());
        } catch (error) {
            console.error('Erreur lors de la mise à jour de la question:', error.message);
            res.status(500).json({
                error: 'Erreur lors de la mise à jour de la question',
                details: error.message
            });
        }
    }

    /**
     * Ajoute un vote pour une option
     * @param {Object} req Requête Express
     * @param {Object} res Réponse Express
     */
    addVote(req, res) {
        try {
            const { id } = req.params;
            const { optionId, userId } = req.body;
            
            // Vérifier si les paramètres requis sont présents
            if (!optionId || !userId) {
                return res.status(400).json({ 
                    error: 'Paramètres manquants',
                    requiredParams: ['optionId', 'userId']
                });
            }
            
            // Trouver la question
            const questionIndex = this.questions.findIndex(q => q.id === id);
            
            if (questionIndex === -1) {
                return res.status(404).json({ error: 'Question non trouvée' });
            }
            
            const questionObj = this.questions[questionIndex];
            
            // Vérifier si la question est archivée
            if (questionObj.isArchived) {
                return res.status(403).json({ error: 'Impossible de voter pour une question archivée' });
            }
            
            // Ajouter le vote
            const success = questionObj.addVote(optionId, userId);
            
            if (!success) {
                return res.status(400).json({ error: 'Option non valide' });
            }
            
            res.json({
                message: 'Vote ajouté avec succès',
                question: questionObj.toJSON()
            });
        } catch (error) {
            console.error('Erreur lors de l\'ajout du vote:', error.message);
            res.status(500).json({
                error: 'Erreur lors de l\'ajout du vote',
                details: error.message
            });
        }
    }

    /**
     * Archive une question
     * @param {Object} req Requête Express
     * @param {Object} res Réponse Express
     */
    archiveQuestion(req, res) {
        try {
            const { id } = req.params;
            const { userId } = req.body;
            
            // Vérifier si les paramètres requis sont présents
            if (!userId) {
                return res.status(400).json({ 
                    error: 'Paramètre manquant',
                    requiredParams: ['userId']
                });
            }
            
            // Trouver la question
            const questionIndex = this.questions.findIndex(q => q.id === id);
            
            if (questionIndex === -1) {
                return res.status(404).json({ error: 'Question non trouvée' });
            }
            
            const questionObj = this.questions[questionIndex];
            
            // Archiver la question
            questionObj.archive(userId);
            
            res.json({
                message: 'Question archivée avec succès',
                question: questionObj.toJSON()
            });
        } catch (error) {
            console.error('Erreur lors de l\'archivage de la question:', error.message);
            res.status(500).json({
                error: 'Erreur lors de l\'archivage de la question',
                details: error.message
            });
        }
    }

    /**
     * Définit la réponse correcte pour une question
     * @param {Object} req Requête Express
     * @param {Object} res Réponse Express
     */
    setCorrectOption(req, res) {
        try {
            const { id } = req.params;
            const { optionId, userId } = req.body;
            
            // Vérifier si les paramètres requis sont présents
            if (!optionId || !userId) {
                return res.status(400).json({ 
                    error: 'Paramètres manquants',
                    requiredParams: ['optionId', 'userId']
                });
            }
            
            // Trouver la question
            const questionIndex = this.questions.findIndex(q => q.id === id);
            
            if (questionIndex === -1) {
                return res.status(404).json({ error: 'Question non trouvée' });
            }
            
            const questionObj = this.questions[questionIndex];
            
            // Définir la réponse correcte
            const success = questionObj.setCorrectOption(optionId, userId);
            
            if (!success) {
                return res.status(400).json({ error: 'Option non valide' });
            }
            
            res.json({
                message: 'Réponse correcte définie avec succès',
                question: questionObj.toJSON()
            });
        } catch (error) {
            console.error('Erreur lors de la définition de la réponse correcte:', error.message);
            res.status(500).json({
                error: 'Erreur lors de la définition de la réponse correcte',
                details: error.message
            });
        }
    }
}

module.exports = new QuestionController();