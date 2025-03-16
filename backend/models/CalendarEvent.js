/**
 * Modèle pour les événements du calendrier
 */
class CalendarEvent {
    constructor(data = {}) {
        this.id = data.id || null;
        this.userId = data.userId || null;
        this.title = data.title || '';
        this.description = data.description || '';
        this.type = data.type || 'show'; // show, game, invoice, action
        this.date = data.date || new Date().toISOString();
        this.endDate = data.endDate || null;
        this.showId = data.showId || null;
        this.gameId = data.gameId || null;
        this.invoiceId = data.invoiceId || null;
        this.actionId = data.actionId || null;
        this.status = data.status || 'pending'; // pending, completed, cancelled
        this.color = data.color || '#4285F4';
        this.isAllDay = data.isAllDay || false;
        this.isRecurring = data.isRecurring || false;
        this.recurringPattern = data.recurringPattern || null;
        this.reminder = data.reminder || null;
        this.createdAt = data.createdAt || new Date().toISOString();
        this.updatedAt = data.updatedAt || new Date().toISOString();
    }

    /**
     * Convertit l'objet en JSON
     * @returns {Object} Objet JSON
     */
    toJSON() {
        return {
            id: this.id,
            userId: this.userId,
            title: this.title,
            description: this.description,
            type: this.type,
            date: this.date,
            endDate: this.endDate,
            showId: this.showId,
            gameId: this.gameId,
            invoiceId: this.invoiceId,
            actionId: this.actionId,
            status: this.status,
            color: this.color,
            isAllDay: this.isAllDay,
            isRecurring: this.isRecurring,
            recurringPattern: this.recurringPattern,
            reminder: this.reminder,
            createdAt: this.createdAt,
            updatedAt: this.updatedAt
        };
    }

    /**
     * Crée un événement de type émission
     * @param {Object} show Émission
     * @param {string} userId ID de l'utilisateur
     * @returns {CalendarEvent} Événement du calendrier
     */
    static createShowEvent(show, userId) {
        return new CalendarEvent({
            userId,
            title: show.name,
            description: `Diffusion de l'émission ${show.name}`,
            type: 'show',
            date: show.air_date || new Date().toISOString(),
            showId: show.id,
            color: '#4285F4', // Bleu
            isAllDay: false
        });
    }

    /**
     * Crée un événement de type jeu
     * @param {Object} game Jeu
     * @param {string} userId ID de l'utilisateur
     * @returns {CalendarEvent} Événement du calendrier
     */
    static createGameEvent(game, userId) {
        return new CalendarEvent({
            userId,
            title: `Jeu: ${game.name}`,
            description: `Participation au jeu ${game.name}`,
            type: 'game',
            date: game.date || new Date().toISOString(),
            gameId: game.id,
            color: '#0F9D58', // Vert
            isAllDay: false
        });
    }

    /**
     * Crée un événement de type facture
     * @param {Object} invoice Facture
     * @param {string} userId ID de l'utilisateur
     * @returns {CalendarEvent} Événement du calendrier
     */
    static createInvoiceEvent(invoice, userId) {
        return new CalendarEvent({
            userId,
            title: `Facture: ${invoice.provider}`,
            description: `Disponibilité de la facture ${invoice.provider}`,
            type: 'invoice',
            date: invoice.date || new Date().toISOString(),
            invoiceId: invoice.id,
            color: '#DB4437', // Rouge
            isAllDay: true
        });
    }

    /**
     * Crée un événement de type action
     * @param {Object} action Action
     * @param {string} userId ID de l'utilisateur
     * @returns {CalendarEvent} Événement du calendrier
     */
    static createActionEvent(action, userId) {
        return new CalendarEvent({
            userId,
            title: `Action: ${action.title}`,
            description: action.description,
            type: 'action',
            date: action.date || new Date().toISOString(),
            actionId: action.id,
            color: '#F4B400', // Jaune
            isAllDay: false,
            status: action.status || 'pending'
        });
    }
}

module.exports = CalendarEvent;