package com.openhands.tvgamerefund.data.scraper

import android.util.Log
import com.openhands.tvgamerefund.data.models.Game
import com.openhands.tvgamerefund.data.models.GameType
import com.openhands.tvgamerefund.data.models.Show
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Scraper pour les jeux TF1 utilisant Jsoup
 */
@Singleton
class JsoupTF1GameScraper @Inject constructor() {
    private val TAG = "JsoupTF1GameScraper"
    
    /**
     * Récupère la liste des jeux TF1
     */
    suspend fun scrapeTF1Games(): Result<List<Game>> = withContext(Dispatchers.IO) {
        try {
            val games = mutableListOf<Game>()
            
            // Récupérer la page principale
            val mainPageUrl = "https://www.tf1.fr/tf1/gagnants-reglements-remboursement-des-jeux-tv/news"
            val mainPageDoc = Jsoup.connect(mainPageUrl)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                .get()
            
            // Extraire les liens vers les pages de règlement
            val gameLinks = extractGameLinks(mainPageDoc)
            
            Log.d(TAG, "Liens de jeux trouvés: ${gameLinks.size}")
            
            // Pour chaque lien, récupérer les détails du jeu
            for (link in gameLinks) {
                try {
                    val gamePageDoc = Jsoup.connect(link)
                        .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                        .get()
                    
                    val game = extractGameDetails(gamePageDoc, link)
                    games.add(game)
                    
                    Log.d(TAG, "Jeu extrait: ${game.title}")
                } catch (e: Exception) {
                    Log.e(TAG, "Erreur lors de l'extraction du jeu $link", e)
                }
            }
            
            return@withContext Result.success(games)
        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors du scraping des jeux TF1", e)
            return@withContext Result.failure(e)
        }
    }
    
    /**
     * Extrait les liens vers les pages de règlement
     */
    private fun extractGameLinks(doc: Document): List<String> {
        val links = mutableListOf<String>()
        
        // Sélectionner tous les liens des articles de jeux
        val newsItems = doc.select("h3 a")
        for (item in newsItems) {
            val href = item.attr("href")
            if (href.contains("gagnants-reglements-remboursement-des-jeux-tv/news")) {
                val fullUrl = if (href.startsWith("http")) href else "https://www.tf1.fr$href"
                links.add(fullUrl)
            }
        }
        
        return links
    }
    
    /**
     * Extrait les détails d'un jeu à partir de sa page
     */
    private fun extractGameDetails(doc: Document, url: String): Game {
        // Extraire le titre
        val title = doc.select("h1").firstOrNull()?.text() ?: "Jeu inconnu"
        
        // Extraire la date
        val dateText = doc.text().let {
            val pattern = "Date de dépôt : (\\d{2}/\\d{2}/\\d{4})".toRegex()
            pattern.find(it)?.groupValues?.get(1)
        }
        
        val date = if (!dateText.isNullOrEmpty()) {
            try {
                SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE).parse(dateText)
            } catch (e: Exception) {
                Date()
            }
        } else {
            Date()
        }
        
        // Extraire les montants des frais de jeu
        val htmlText = doc.html()
        val feesPattern = "(\\d+[,.]\\d+)€ par (SMS|appel)".toRegex()
        val feesMatches = feesPattern.findAll(htmlText)
        
        var cost = 0.0
        var phoneNumber = ""
        var gameType = GameType.OTHER
        
        // Déterminer le type de jeu et le coût
        if (htmlText.contains("SMS") && htmlText.contains("appel")) {
            gameType = GameType.MIXED
        } else if (htmlText.contains("SMS")) {
            gameType = GameType.SMS
        } else if (htmlText.contains("appel")) {
            gameType = GameType.PHONE_CALL
        } else if (htmlText.contains("internet") || htmlText.contains("web")) {
            gameType = GameType.WEB
        }
        
        // Extraire le coût
        feesMatches.forEach { match ->
            val amount = match.groupValues[1].replace(',', '.').toDoubleOrNull() ?: 0.0
            if (amount > cost) {
                cost = amount
            }
        }
        
        // Extraire le numéro de téléphone
        val phonePattern = "(\\d{5})".toRegex()
        val phoneMatch = phonePattern.find(htmlText)
        phoneNumber = phoneMatch?.groupValues?.get(1) ?: ""
        
        // Extraire l'adresse de remboursement
        val addressPattern = "(TF1[^<]+\\d{5}[^<]+)".toRegex()
        val addressMatch = addressPattern.find(htmlText)
        val address = addressMatch?.groupValues?.get(1)?.trim() ?: ""
        
        // Extraire le délai de remboursement
        val deadlinePattern = "dans un délai de (\\d+)".toRegex()
        val deadlineMatch = deadlinePattern.find(htmlText)
        val deadline = deadlineMatch?.groupValues?.get(1)?.toIntOrNull() ?: 60 // Par défaut 60 jours
        
        // Extraire la description
        val description = doc.select("p").firstOrNull()?.text() ?: "Aucune description disponible"
        
        // Extraire l'émission
        val showName = doc.select("meta[property=og:title]").attr("content").split("|").firstOrNull()?.trim() ?: title
        
        // Créer l'objet Show
        val show = Show(
            id = showName.hashCode().toString(),
            title = showName,
            channel = "TF1",
            description = "Émission de TF1",
            imageUrl = null
        )
        
        // Créer l'objet Game
        return Game(
            id = url.hashCode().toString(),
            showId = show.id,
            title = title,
            description = description,
            type = gameType,
            startDate = date,
            endDate = null,
            rules = url,
            imageUrl = doc.select("meta[property=og:image]").attr("content").takeIf { it.isNotEmpty() },
            participationMethod = if (gameType == GameType.SMS) "Envoyez SMS au $phoneNumber" else "Appelez le $phoneNumber",
            reimbursementMethod = "Envoyez demande par courrier à $address",
            reimbursementDeadline = deadline,
            cost = cost,
            phoneNumber = phoneNumber,
            refundAddress = address
        )
    }
}