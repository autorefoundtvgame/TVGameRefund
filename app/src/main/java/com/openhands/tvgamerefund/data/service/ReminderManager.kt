package com.openhands.tvgamerefund.data.service

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.openhands.tvgamerefund.data.models.UserParticipation
import com.openhands.tvgamerefund.data.repository.GameRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Gestionnaire des rappels pour les remboursements
 */
@Singleton
class ReminderManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val gameRepository: GameRepository
) {
    private val TAG = "ReminderManager"
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    
    /**
     * Planifie un rappel pour une participation
     */
    fun scheduleReminder(participation: UserParticipation) {
        val invoiceExpectedDate = participation.invoiceExpectedDate
        
        // Calculer le délai avant la notification
        val currentDate = Calendar.getInstance()
        val reminderDate = Calendar.getInstance()
        reminderDate.time = invoiceExpectedDate
        
        // Notification 3 jours avant la date prévue de la facture
        reminderDate.add(Calendar.DAY_OF_MONTH, -3)
        
        // Si la date de rappel est déjà passée, ne pas planifier
        if (reminderDate.before(currentDate)) {
            Log.d(TAG, "La date de rappel est déjà passée pour la participation ${participation.id}")
            return
        }
        
        // Calculer le délai en millisecondes
        val delayMillis = reminderDate.timeInMillis - currentDate.timeInMillis
        
        // Créer les contraintes (exécution uniquement lorsque l'appareil est connecté)
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        
        coroutineScope.launch {
            // Récupérer le titre du jeu
            val game = gameRepository.getGameById(participation.gameId)
            val gameTitle = game?.title ?: "Jeu inconnu"
            
            // Créer les données d'entrée
            val inputData = Data.Builder()
                .putString("participationId", participation.id)
                .putString("gameId", participation.gameId)
                .putString("gameTitle", gameTitle)
                .putString("notificationType", "INVOICE_REMINDER")
                .build()
            
            // Créer la requête de travail
            val reminderRequest = OneTimeWorkRequestBuilder<ReminderWorker>()
                .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
                .setConstraints(constraints)
                .setInputData(inputData)
                .build()
            
            // Enregistrer la requête avec WorkManager
            WorkManager.getInstance(context).enqueueUniqueWork(
                "reminder_${participation.id}",
                ExistingWorkPolicy.REPLACE,
                reminderRequest
            )
            
            Log.d(TAG, "Rappel planifié pour la participation ${participation.id} le ${reminderDate.time}")
        }
    }
    
    /**
     * Annule un rappel pour une participation
     */
    fun cancelReminder(participationId: String) {
        WorkManager.getInstance(context).cancelUniqueWork("reminder_$participationId")
        Log.d(TAG, "Rappel annulé pour la participation $participationId")
    }
}