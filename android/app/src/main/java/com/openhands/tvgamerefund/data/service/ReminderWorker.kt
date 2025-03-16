package com.openhands.tvgamerefund.data.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.openhands.tvgamerefund.MainActivity
import com.openhands.tvgamerefund.R
import com.openhands.tvgamerefund.data.models.Game
import com.openhands.tvgamerefund.data.models.UserParticipation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Worker pour envoyer les notifications de rappel
 */
class ReminderWorker(
    private val context: Context,
    private val params: WorkerParameters
) : CoroutineWorker(context, params) {
    
    private val TAG = "ReminderWorker"
    
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val participationId = inputData.getString("participationId") ?: return@withContext Result.failure()
            val gameId = inputData.getString("gameId") ?: return@withContext Result.failure()
            val notificationType = inputData.getString("notificationType") ?: return@withContext Result.failure()
            val gameTitle = inputData.getString("gameTitle") ?: "Jeu inconnu"
            
            Log.d(TAG, "Traitement du rappel pour la participation $participationId, jeu $gameId, type $notificationType")
            
            // Envoyer la notification appropriée
            when (notificationType) {
                "INVOICE_REMINDER" -> sendInvoiceReminder(gameTitle, participationId)
                "REIMBURSEMENT_REMINDER" -> sendReimbursementReminder(gameTitle, participationId)
                else -> Log.w(TAG, "Type de notification inconnu: $notificationType")
            }
            
            return@withContext Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors de l'envoi du rappel", e)
            return@withContext Result.failure()
        }
    }
    
    /**
     * Envoie une notification de rappel pour la facture
     */
    private fun sendInvoiceReminder(gameTitle: String, participationId: String) {
        // Créer le canal de notification si nécessaire
        createNotificationChannel()
        
        // Créer l'intent pour ouvrir l'application
        val intent = Intent(context, MainActivity::class.java).apply {
            putExtra("participationId", participationId)
            putExtra("screen", "invoices")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )
        
        // Construire la notification
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Facture bientôt disponible")
            .setContentText("Votre facture pour le jeu $gameTitle sera bientôt disponible")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        
        // Afficher la notification
        with(NotificationManagerCompat.from(context)) {
            notify(participationId.hashCode(), notification)
        }
        
        Log.d(TAG, "Notification de rappel de facture envoyée pour la participation $participationId")
    }
    
    /**
     * Envoie une notification de rappel pour le remboursement
     */
    private fun sendReimbursementReminder(gameTitle: String, participationId: String) {
        // Créer le canal de notification si nécessaire
        createNotificationChannel()
        
        // Créer l'intent pour ouvrir l'application
        val intent = Intent(context, MainActivity::class.java).apply {
            putExtra("participationId", participationId)
            putExtra("screen", "reimbursements")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )
        
        // Construire la notification
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Remboursement à demander")
            .setContentText("N'oubliez pas de demander le remboursement pour le jeu $gameTitle")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        
        // Afficher la notification
        with(NotificationManagerCompat.from(context)) {
            notify(participationId.hashCode() + 1000, notification)
        }
        
        Log.d(TAG, "Notification de rappel de remboursement envoyée pour la participation $participationId")
    }
    
    /**
     * Crée le canal de notification
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Rappels de remboursement"
            val descriptionText = "Notifications pour les rappels de remboursement"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            
            // Enregistrer le canal auprès du système
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    companion object {
        private const val CHANNEL_ID = "reminders_channel"
    }
}