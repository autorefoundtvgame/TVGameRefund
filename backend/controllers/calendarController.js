/**
 * Contrôleur pour le calendrier
 */
class CalendarController {
    constructor() {
        // Simuler une base de données avec un tableau d'événements
        this.events = [
            {
                id: '1',
                userId: 'user1',
                title: 'Koh-Lanta',
                description: 'Diffusion de l\'émission Koh-Lanta',
                type: 'show',
                date: '2025-03-18T21:00:00.000Z',
                showId: '58237',
                color: '#4285F4',
                isAllDay: false
            },
            {
                id: '2',
                userId: 'user1',
                title: 'Jeu: KohLanta',
                description: 'Participation au jeu KohLanta',
                type: 'game',
                date: '2025-03-18T21:30:00.000Z',
                gameId: 'KohLanta',
                color: '#0F9D58',
                isAllDay: false
            },
            {
                id: '3',
                userId: 'user1',
                title: 'Facture: Free Mobile',
                description: 'Disponibilité de la facture Free Mobile',
                type: 'invoice',
                date: '2025-04-05T00:00:00.000Z',
                invoiceId: 'invoice1',
                color: '#DB4437',
                isAllDay: true
            },
            {
                id: '4',
                userId: 'user1',
                title: 'Action: Envoyer demande de remboursement',
                description: 'Envoyer la demande de remboursement pour le jeu KohLanta',
                type: 'action',
                date: '2025-04-10T10:00:00.000Z',
                actionId: 'action1',
                color: '#F4B400',
                isAllDay: false,
                status: 'pending'
            }
        ];
    }

    /**
     * Obtient les événements du calendrier pour un utilisateur
     * @param {Object} req Requête Express
     * @param {Object} res Réponse Express
     */
    getEvents(req, res) {
        try {
            const { userId, startDate, endDate, type } = req.query;
            
            // Filtrer les événements par utilisateur
            let filteredEvents = this.events;
            
            if (userId) {
                filteredEvents = filteredEvents.filter(event => event.userId === userId);
            }
            
            // Filtrer les événements par date
            if (startDate) {
                const start = new Date(startDate);
                filteredEvents = filteredEvents.filter(event => new Date(event.date) >= start);
            }
            
            if (endDate) {
                const end = new Date(endDate);
                filteredEvents = filteredEvents.filter(event => new Date(event.date) <= end);
            }
            
            // Filtrer les événements par type
            if (type) {
                filteredEvents = filteredEvents.filter(event => event.type === type);
            }
            
            res.json({
                events: filteredEvents.map(event => event.toJSON()),
                count: filteredEvents.length
            });
        } catch (error) {
            console.error('Erreur lors de la récupération des événements du calendrier:', error.message);
            res.status(500).json({
                error: 'Erreur lors de la récupération des événements du calendrier',
                details: error.message
            });
        }
    }

    /**
     * Crée un événement dans le calendrier
     * @param {Object} req Requête Express
     * @param {Object} res Réponse Express
     */
    createEvent(req, res) {
        try {
            const eventData = req.body;
            
            // Générer un ID unique
            eventData.id = (this.events.length + 1).toString();
            
            // Créer l'événement
            const event = new CalendarEvent(eventData);
            
            // Ajouter l'événement à la liste
            this.events.push(event);
            
            res.status(201).json(event.toJSON());
        } catch (error) {
            console.error('Erreur lors de la création de l\'événement:', error.message);
            res.status(500).json({
                error: 'Erreur lors de la création de l\'événement',
                details: error.message
            });
        }
    }

    /**
     * Met à jour un événement du calendrier
     * @param {Object} req Requête Express
     * @param {Object} res Réponse Express
     */
    updateEvent(req, res) {
        try {
            const { id } = req.params;
            const eventData = req.body;
            
            // Trouver l'événement
            const eventIndex = this.events.findIndex(event => event.id === id);
            
            if (eventIndex === -1) {
                return res.status(404).json({ error: 'Événement non trouvé' });
            }
            
            // Mettre à jour l'événement
            const event = this.events[eventIndex];
            Object.assign(event, eventData);
            event.updatedAt = new Date().toISOString();
            
            res.json(event.toJSON());
        } catch (error) {
            console.error('Erreur lors de la mise à jour de l\'événement:', error.message);
            res.status(500).json({
                error: 'Erreur lors de la mise à jour de l\'événement',
                details: error.message
            });
        }
    }

    /**
     * Supprime un événement du calendrier
     * @param {Object} req Requête Express
     * @param {Object} res Réponse Express
     */
    deleteEvent(req, res) {
        try {
            const { id } = req.params;
            
            // Trouver l'événement
            const eventIndex = this.events.findIndex(event => event.id === id);
            
            if (eventIndex === -1) {
                return res.status(404).json({ error: 'Événement non trouvé' });
            }
            
            // Supprimer l'événement
            this.events.splice(eventIndex, 1);
            
            res.json({ message: 'Événement supprimé avec succès' });
        } catch (error) {
            console.error('Erreur lors de la suppression de l\'événement:', error.message);
            res.status(500).json({
                error: 'Erreur lors de la suppression de l\'événement',
                details: error.message
            });
        }
    }
}

module.exports = new CalendarController();