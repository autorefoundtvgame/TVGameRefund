require('dotenv').config();

module.exports = {
  port: process.env.PORT || 3000,
  mongoUri: process.env.MONGO_URI || 'mongodb://localhost:27017/tvgamerefund',
  tmdbApiKey: process.env.TMDB_API_KEY || '',
  tmdbAccessToken: process.env.TMDB_ACCESS_TOKEN || '',
  logLevel: process.env.LOG_LEVEL || 'info',
  environment: process.env.NODE_ENV || 'development'
};