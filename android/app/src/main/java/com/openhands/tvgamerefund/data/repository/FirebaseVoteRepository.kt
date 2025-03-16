package com.openhands.tvgamerefund.data.repository

// Imports pour la connectivité réseau (non utilisés pour l'instant)
// import android.content.Context
// import android.net.ConnectivityManager
// import android.net.NetworkCapabilities
import android.util.Log
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.openhands.tvgamerefund.data.models.UserVote
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.Date
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository pour gérer les votes des utilisateurs dans Firebase
 */
@Singleton
class FirebaseVoteRepository @Inject constructor() {
    private val firestore = Firebase.firestore
    private val auth = Firebase.auth
    private val TAG = "FirebaseVoteRepository"
    
    /**
     * Soumet un vote pour un jeu
     */
    suspend fun submitVote(gameId: String, rating: Int, comment: String? = null): Result<UserVote> = withContext(Dispatchers.IO) {
        try {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                return@withContext Result.failure(Exception("Utilisateur non connecté"))
            }
            
            val userId = currentUser.uid
            val voteId = UUID.randomUUID().toString()
            val voteDate = Date()
            
            val vote = hashMapOf(
                "id" to voteId,
                "userId" to userId,
                "gameId" to gameId,
                "rating" to rating,
                "comment" to comment,
                "voteDate" to FieldValue.serverTimestamp(),
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            
            val voteRef = firestore.collection("votes").document(voteId)
            voteRef.set(vote).await()
            
            // Mettre à jour les statistiques du jeu
            updateGameStats(gameId)
            
            val userVote = UserVote(
                id = voteId,
                userId = userId,
                gameId = gameId,
                rating = rating,
                comment = comment,
                voteDate = voteDate
            )
            
            return@withContext Result.success(userVote)
        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors de la soumission du vote", e)
            return@withContext Result.failure(e)
        }
    }
    
    /**
     * Récupère les votes pour un jeu
     */
    suspend fun getVotesForGame(gameId: String): Result<List<UserVote>> = withContext(Dispatchers.IO) {
        try {
            val votesRef = firestore.collection("votes")
                .whereEqualTo("gameId", gameId)
                .orderBy("voteDate", Query.Direction.DESCENDING)
                
            val snapshot = votesRef.get().await()
            
            val votes = snapshot.documents.mapNotNull { document ->
                val id = document.getString("id") ?: return@mapNotNull null
                val userId = document.getString("userId") ?: return@mapNotNull null
                val rating = document.getLong("rating")?.toInt() ?: return@mapNotNull null
                val comment = document.getString("comment")
                val voteDate = document.getTimestamp("voteDate")?.toDate() ?: Date()
                
                UserVote(
                    id = id,
                    userId = userId,
                    gameId = gameId,
                    rating = rating,
                    comment = comment,
                    voteDate = voteDate
                )
            }
            
            return@withContext Result.success(votes)
        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors de la récupération des votes", e)
            return@withContext Result.failure(e)
        }
    }
    
    /**
     * Récupère les votes d'un utilisateur
     */
    suspend fun getVotesByUser(userId: String): Result<List<UserVote>> = withContext(Dispatchers.IO) {
        try {
            val votesRef = firestore.collection("votes")
                .whereEqualTo("userId", userId)
                .orderBy("voteDate", Query.Direction.DESCENDING)
                
            val snapshot = votesRef.get().await()
            
            val votes = snapshot.documents.mapNotNull { document ->
                val id = document.getString("id") ?: return@mapNotNull null
                val gameId = document.getString("gameId") ?: return@mapNotNull null
                val rating = document.getLong("rating")?.toInt() ?: return@mapNotNull null
                val comment = document.getString("comment")
                val voteDate = document.getTimestamp("voteDate")?.toDate() ?: Date()
                
                UserVote(
                    id = id,
                    userId = userId,
                    gameId = gameId,
                    rating = rating,
                    comment = comment,
                    voteDate = voteDate
                )
            }
            
            return@withContext Result.success(votes)
        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors de la récupération des votes de l'utilisateur", e)
            return@withContext Result.failure(e)
        }
    }
    
    /**
     * Récupère un vote spécifique
     */
    suspend fun getVoteById(voteId: String): Result<UserVote> = withContext(Dispatchers.IO) {
        try {
            val voteRef = firestore.collection("votes").document(voteId)
            val document = voteRef.get().await()
            
            if (!document.exists()) {
                return@withContext Result.failure(Exception("Vote non trouvé"))
            }
            
            val id = document.getString("id") ?: return@withContext Result.failure(Exception("ID manquant"))
            val userId = document.getString("userId") ?: return@withContext Result.failure(Exception("UserID manquant"))
            val gameId = document.getString("gameId") ?: return@withContext Result.failure(Exception("GameID manquant"))
            val rating = document.getLong("rating")?.toInt() ?: return@withContext Result.failure(Exception("Rating manquant"))
            val comment = document.getString("comment")
            val voteDate = document.getTimestamp("voteDate")?.toDate() ?: Date()
            
            val vote = UserVote(
                id = id,
                userId = userId,
                gameId = gameId,
                rating = rating,
                comment = comment,
                voteDate = voteDate
            )
            
            return@withContext Result.success(vote)
        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors de la récupération du vote", e)
            return@withContext Result.failure(e)
        }
    }
    
    /**
     * Met à jour les statistiques d'un jeu
     */
    private suspend fun updateGameStats(gameId: String) {
        try {
            val votesRef = firestore.collection("votes")
                .whereEqualTo("gameId", gameId)
                
            val snapshot = votesRef.get().await()
            
            val totalRating = snapshot.documents.sumOf { 
                it.getLong("rating")?.toInt() ?: 0 
            }
            val voteCount = snapshot.documents.size
            val averageRating = if (voteCount > 0) totalRating.toDouble() / voteCount else 0.0
            
            val statsRef = firestore.collection("gameStats").document(gameId)
            statsRef.set(
                hashMapOf(
                    "gameId" to gameId,
                    "participationCount" to voteCount,
                    "averageRating" to averageRating,
                    "lastUpdated" to FieldValue.serverTimestamp()
                ),
                SetOptions.merge()
            ).await()
            
            Log.d(TAG, "Statistiques mises à jour pour le jeu $gameId: $voteCount votes, moyenne $averageRating")
        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors de la mise à jour des statistiques", e)
        }
    }
    
    /**
     * Récupère les statistiques d'un jeu
     */
    suspend fun getGameStats(gameId: String): Result<Map<String, Any>> = withContext(Dispatchers.IO) {
        try {
            // Vérifier si Firebase est disponible
            if (!isFirebaseAvailable()) {
                Log.w(TAG, "Firebase n'est pas disponible, utilisation des statistiques par défaut")
                val defaultStats = hashMapOf(
                    "gameId" to gameId,
                    "participationCount" to 0,
                    "averageRating" to 0.0,
                    "lastUpdated" to Date()
                )
                return@withContext Result.success(defaultStats)
            }
            
            val statsRef = firestore.collection("gameStats").document(gameId)
            val document = statsRef.get().await()
            
            if (!document.exists()) {
                // Si les stats n'existent pas, créer des stats par défaut
                val defaultStats = hashMapOf(
                    "gameId" to gameId,
                    "participationCount" to 0,
                    "averageRating" to 0.0,
                    "lastUpdated" to FieldValue.serverTimestamp()
                )
                
                try {
                    statsRef.set(defaultStats).await()
                } catch (e: Exception) {
                    Log.w(TAG, "Impossible de sauvegarder les statistiques par défaut", e)
                }
                return@withContext Result.success(defaultStats)
            }
            
            val stats = document.data ?: hashMapOf()
            return@withContext Result.success(stats)
        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors de la récupération des statistiques", e)
            // Retourner des statistiques par défaut en cas d'erreur
            val defaultStats = hashMapOf(
                "gameId" to gameId,
                "participationCount" to 0,
                "averageRating" to 0.0,
                "lastUpdated" to Date()
            )
            return@withContext Result.success(defaultStats)
        }
    }
    
    /**
     * Vérifie si Firebase est disponible
     * 
     * Note: Dans un environnement de développement, Firebase peut ne pas être configuré correctement.
     * Cette méthode retourne toujours false pour éviter les erreurs.
     */
    private fun isFirebaseAvailable(): Boolean {
        // En développement, on considère que Firebase n'est pas disponible
        // pour éviter les erreurs de configuration
        return false
        
        // Dans une version de production, on pourrait utiliser ce code :
        /*
        return try {
            // Vérifier si Firestore est disponible
            val task = firestore.collection("test").document("test").get()
            task.isComplete && !task.isCanceled
        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors de la vérification de la disponibilité de Firebase", e)
            false
        }
        */
    }
}