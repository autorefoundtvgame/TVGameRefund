const mongoose = require('mongoose');

const gameSchema = new mongoose.Schema({
  id: { type: String, required: true, unique: true },
  showId: { type: String, required: true },
  title: { type: String, required: true },
  description: { type: String, default: '' },
  type: { 
    type: String, 
    enum: ['SMS', 'PHONE_CALL', 'MIXED'], 
    default: 'SMS' 
  },
  startDate: { type: Date, default: Date.now },
  endDate: { type: Date, default: null },
  rules: { type: String, default: '' },
  imageUrl: { type: String, default: null },
  participationMethod: { type: String, default: '' },
  reimbursementMethod: { type: String, default: '' },
  reimbursementDeadline: { type: Number, default: 60 },
  cost: { type: Number, default: 0.99 },
  phoneNumber: { type: String, default: '' },
  refundAddress: { type: String, default: '' },
  channel: { type: String, default: 'TF1' },
  tmdbId: { type: Number, default: null },
  createdAt: { type: Date, default: Date.now },
  updatedAt: { type: Date, default: Date.now }
});

// Index pour les recherches
gameSchema.index({ title: 'text', description: 'text' });
gameSchema.index({ showId: 1 });
gameSchema.index({ channel: 1 });

const Game = mongoose.model('Game', gameSchema);

module.exports = Game;