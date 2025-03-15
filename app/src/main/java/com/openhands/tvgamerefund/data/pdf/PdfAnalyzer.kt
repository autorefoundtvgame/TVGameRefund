package com.openhands.tvgamerefund.data.pdf

import android.content.Context
import android.util.Log
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfReader
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor
import com.openhands.tvgamerefund.data.models.Game
import com.openhands.tvgamerefund.data.models.Invoice
import com.openhands.tvgamerefund.data.models.InvoiceGameFee
import com.openhands.tvgamerefund.data.repository.GameRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Date
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service pour analyser les PDF de factures et détecter les frais de jeu
 */
@Singleton
class PdfAnalyzer @Inject constructor(
    @ApplicationContext private val context: Context,
    private val gameRepository: GameRepository
) {
    private val TAG = "PdfAnalyzer"
    
    // Mots-clés pour détecter les frais de jeu dans les factures
    private val gameKeywords = listOf(
        "jeu", "sms+", "audiotel", "3680", "3280", "71414", "72525", "73838",
        "koh lanta", "the voice", "12 coups", "les 12 coups", "tf1", "m6", "france 2"
    )
    
    /**
     * Analyse une facture pour détecter les frais de jeu
     * @param invoice Facture à analyser
     * @return Liste des frais de jeu détectés
     */
    suspend fun analyzeInvoice(invoice: Invoice): Result<List<InvoiceGameFee>> = withContext(Dispatchers.IO) {
        try {
            if (invoice.localPdfPath == null) {
                return@withContext Result.failure(Exception("La facture n'a pas été téléchargée"))
            }
            
            val pdfFile = File(invoice.localPdfPath)
            if (!pdfFile.exists()) {
                return@withContext Result.failure(Exception("Le fichier PDF n'existe pas"))
            }
            
            Log.d(TAG, "Analyse de la facture: ${pdfFile.absolutePath}")
            
            // Extraire le texte du PDF
            val pdfText = extractTextFromPdf(pdfFile)
            
            // Rechercher les frais de jeu dans le texte
            val gameFees = findGameFees(pdfText, invoice)
            
            Log.d(TAG, "Frais de jeu détectés: ${gameFees.size}")
            
            return@withContext Result.success(gameFees)
        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors de l'analyse de la facture", e)
            return@withContext Result.failure(e)
        }
    }
    
    /**
     * Extrait le texte d'un fichier PDF
     * @param pdfFile Fichier PDF
     * @return Texte extrait
     */
    private fun extractTextFromPdf(pdfFile: File): String {
        return try {
            val reader = PdfReader(pdfFile)
            val pdfDocument = PdfDocument(reader)
            
            val text = StringBuilder()
            for (i in 1..pdfDocument.numberOfPages) {
                val page = pdfDocument.getPage(i)
                val pageText = PdfTextExtractor.getTextFromPage(page)
                text.append(pageText)
            }
            
            pdfDocument.close()
            reader.close()
            
            text.toString()
        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors de l'extraction du texte du PDF", e)
            ""
        }
    }
    
    /**
     * Recherche les frais de jeu dans le texte d'une facture
     * @param text Texte de la facture
     * @param invoice Facture
     * @return Liste des frais de jeu détectés
     */
    private suspend fun findGameFees(text: String, invoice: Invoice): List<InvoiceGameFee> {
        val gameFees = mutableListOf<InvoiceGameFee>()
        
        // Récupérer tous les jeux connus
        val games = gameRepository.getAllGamesSync()
        
        // Rechercher les numéros de téléphone des jeux
        for (game in games) {
            if (game.phoneNumber.isNotEmpty() && text.contains(game.phoneNumber)) {
                // Rechercher le montant associé au numéro
                val amount = findAmountForPhoneNumber(text, game.phoneNumber)
                if (amount > 0) {
                    val gameFee = InvoiceGameFee(
                        id = UUID.randomUUID().toString(),
                        invoiceId = invoice.id,
                        gameId = game.id,
                        amount = amount,
                        date = invoice.date,
                        phoneNumber = game.phoneNumber,
                        createdAt = Date(),
                        updatedAt = Date()
                    )
                    gameFees.add(gameFee)
                }
            }
        }
        
        // Si aucun jeu connu n'a été trouvé, rechercher des mots-clés génériques
        if (gameFees.isEmpty()) {
            for (keyword in gameKeywords) {
                if (text.contains(keyword, ignoreCase = true)) {
                    // Rechercher les numéros courts à proximité du mot-clé
                    val shortNumbers = findShortNumbersNearKeyword(text, keyword)
                    
                    for (number in shortNumbers) {
                        val amount = findAmountForPhoneNumber(text, number)
                        if (amount > 0) {
                            // Créer un jeu temporaire pour ce numéro
                            val tempGame = findOrCreateGameForNumber(number, keyword)
                            
                            val gameFee = InvoiceGameFee(
                                id = UUID.randomUUID().toString(),
                                invoiceId = invoice.id,
                                gameId = tempGame.id,
                                amount = amount,
                                date = invoice.date,
                                phoneNumber = number,
                                createdAt = Date(),
                                updatedAt = Date()
                            )
                            gameFees.add(gameFee)
                        }
                    }
                }
            }
        }
        
        return gameFees
    }
    
    /**
     * Recherche le montant associé à un numéro de téléphone dans le texte
     * @param text Texte de la facture
     * @param phoneNumber Numéro de téléphone
     * @return Montant trouvé ou 0 si aucun montant n'est trouvé
     */
    private fun findAmountForPhoneNumber(text: String, phoneNumber: String): Double {
        // Rechercher le numéro dans le texte
        val index = text.indexOf(phoneNumber)
        if (index == -1) return 0.0
        
        // Rechercher un montant à proximité du numéro
        val surroundingText = text.substring(maxOf(0, index - 100), minOf(text.length, index + 100))
        
        // Rechercher un pattern de montant (ex: 0,99€, 0.99€, 0,99 €, etc.)
        val amountPattern = "(\\d+[.,]\\d{2})\\s*€".toRegex()
        val matches = amountPattern.findAll(surroundingText)
        
        for (match in matches) {
            val amountStr = match.groupValues[1].replace(',', '.')
            return amountStr.toDoubleOrNull() ?: 0.0
        }
        
        return 0.0
    }
    
    /**
     * Recherche les numéros courts à proximité d'un mot-clé
     * @param text Texte de la facture
     * @param keyword Mot-clé
     * @return Liste des numéros courts trouvés
     */
    private fun findShortNumbersNearKeyword(text: String, keyword: String): List<String> {
        val numbers = mutableListOf<String>()
        
        // Rechercher le mot-clé dans le texte
        val index = text.indexOf(keyword, ignoreCase = true)
        if (index == -1) return numbers
        
        // Rechercher des numéros courts à proximité du mot-clé
        val surroundingText = text.substring(maxOf(0, index - 100), minOf(text.length, index + 100))
        
        // Rechercher des numéros courts (3-5 chiffres)
        val shortNumberPattern = "\\b(\\d{3,5})\\b".toRegex()
        val matches = shortNumberPattern.findAll(surroundingText)
        
        for (match in matches) {
            numbers.add(match.groupValues[1])
        }
        
        return numbers
    }
    
    /**
     * Trouve ou crée un jeu pour un numéro de téléphone
     * @param phoneNumber Numéro de téléphone
     * @param keyword Mot-clé associé
     * @return Jeu trouvé ou créé
     */
    private suspend fun findOrCreateGameForNumber(phoneNumber: String, keyword: String): Game {
        // Rechercher un jeu existant avec ce numéro
        val existingGames = gameRepository.getGamesByPhoneNumber(phoneNumber)
        if (existingGames.isNotEmpty()) {
            return existingGames.first()
        }
        
        // Créer un nouveau jeu temporaire
        val game = Game(
            id = "temp_${UUID.randomUUID()}",
            showId = "unknown",
            title = "Jeu détecté - $keyword",
            description = "Jeu détecté automatiquement sur une facture",
            type = com.openhands.tvgamerefund.data.models.GameType.SMS,
            startDate = Date(),
            endDate = null,
            rules = "",
            imageUrl = null,
            participationMethod = "Numéro court: $phoneNumber",
            reimbursementMethod = "À déterminer",
            reimbursementDeadline = 60,
            cost = 0.0,
            phoneNumber = phoneNumber,
            refundAddress = "",
            isLiked = false
        )
        
        // Sauvegarder le jeu
        gameRepository.insertGame(game)
        
        return game
    }
    
    /**
     * Génère un PDF annoté avec les frais de jeu mis en évidence
     * @param invoice Facture à annoter
     * @param gameFees Frais de jeu à mettre en évidence
     * @return Chemin du fichier PDF annoté
     */
    suspend fun generateAnnotatedPdf(invoice: Invoice, gameFees: List<InvoiceGameFee>): Result<String> = withContext(Dispatchers.IO) {
        try {
            if (invoice.localPdfPath == null) {
                return@withContext Result.failure(Exception("La facture n'a pas été téléchargée"))
            }
            
            val pdfFile = File(invoice.localPdfPath)
            if (!pdfFile.exists()) {
                return@withContext Result.failure(Exception("Le fichier PDF n'existe pas"))
            }
            
            // Créer le répertoire pour les PDF annotés
            val annotatedDir = File(context.filesDir, "annotated_invoices")
            if (!annotatedDir.exists()) {
                annotatedDir.mkdirs()
            }
            
            // Créer le fichier de destination
            val fileName = "annotated_${invoice.id}.pdf"
            val annotatedFile = File(annotatedDir, fileName)
            
            // TODO: Implémenter l'annotation du PDF avec iText
            // Pour l'instant, on copie simplement le fichier original
            pdfFile.copyTo(annotatedFile, overwrite = true)
            
            return@withContext Result.success(annotatedFile.absolutePath)
        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors de la génération du PDF annoté", e)
            return@withContext Result.failure(e)
        }
    }
}