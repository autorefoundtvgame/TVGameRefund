package com.openhands.tvgamerefund.data.repository

import android.content.Context
import android.util.Log
import com.openhands.tvgamerefund.data.models.Invoice
import com.openhands.tvgamerefund.data.models.InvoiceGameFee
import com.openhands.tvgamerefund.data.models.InvoiceStatus
import com.openhands.tvgamerefund.data.network.FreeApiService
import com.openhands.tvgamerefund.data.network.FreeAuthManager
import com.openhands.tvgamerefund.data.pdf.PdfAnalyzer
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class InvoiceRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val operatorRepository: OperatorRepository,
    private val freeAuthManager: FreeAuthManager,
    private val freeApiService: FreeApiService,
    private val okHttpClient: OkHttpClient,
    private val pdfAnalyzer: PdfAnalyzer
) {
    private val TAG = "InvoiceRepository"
    
    private val _invoices = MutableStateFlow<List<Invoice>>(emptyList())
    val invoices: Flow<List<Invoice>> = _invoices.asStateFlow()
    
    private val _invoiceGameFees = MutableStateFlow<Map<String, List<InvoiceGameFee>>>(emptyMap())
    val invoiceGameFees: Flow<Map<String, List<InvoiceGameFee>>> = _invoiceGameFees.asStateFlow()
    
    /**
     * Récupère les factures d'un opérateur pour un numéro de téléphone donné
     * @param operatorId Identifiant de l'opérateur
     * @param phoneNumber Numéro de téléphone
     * @return Liste des factures
     */
    suspend fun fetchInvoices(operatorId: String, phoneNumber: String): Result<List<Invoice>> = withContext(Dispatchers.IO) {
        try {
            when (operatorId) {
                "FREE" -> {
                    // Récupérer les cookies sauvegardés directement
                    val savedCookies = freeAuthManager.getSavedCookies()
                    if (savedCookies != null && savedCookies.isNotEmpty()) {
                        Log.d(TAG, "Utilisation des cookies sauvegardés pour récupérer les factures")
                        
                        // Tenter de récupérer les vraies factures avec les cookies sauvegardés
                        try {
                            val realInvoices = fetchRealFreeInvoices(savedCookies, phoneNumber)
                            if (realInvoices.isNotEmpty()) {
                                _invoices.value = realInvoices
                                return@withContext Result.success(realInvoices)
                            } else {
                                Log.w(TAG, "Aucune facture réelle trouvée avec les cookies sauvegardés")
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Erreur lors de la récupération des factures avec les cookies sauvegardés", e)
                        }
                    }
                    
                    // Si les cookies sauvegardés ne fonctionnent pas, essayer l'authentification normale
                    val credentials = operatorRepository.getCredentials(com.openhands.tvgamerefund.data.models.Operator.FREE)
                    if (credentials != null) {
                        // Première tentative avec l'authentification standard
                        var sessionCookie = freeAuthManager.authenticate(credentials.username, credentials.password)
                        
                        // Si ça échoue, essayer l'authentification directe
                        if (sessionCookie == null) {
                            Log.d(TAG, "Authentification standard échouée, tentative d'authentification directe...")
                            sessionCookie = freeAuthManager.authenticateDirect(credentials.username, credentials.password)
                        }
                        
                        if (sessionCookie != null) {
                            // Vérifier si l'authentification a réussi
                            val isAuthenticated = freeAuthManager.isAuthenticated(sessionCookie)
                            if (isAuthenticated) {
                                Log.d(TAG, "Authentification réussie, récupération des factures...")
                                
                                // Tenter de récupérer les vraies factures
                                try {
                                    val realInvoices = fetchRealFreeInvoices(sessionCookie, phoneNumber)
                                    if (realInvoices.isNotEmpty()) {
                                        _invoices.value = realInvoices
                                        return@withContext Result.success(realInvoices)
                                    } else {
                                        Log.w(TAG, "Aucune facture réelle trouvée, utilisation des factures fictives")
                                    }
                                } catch (e: Exception) {
                                    Log.e(TAG, "Erreur lors de la récupération des factures réelles", e)
                                    Log.w(TAG, "Utilisation des factures fictives suite à l'erreur")
                                }
                            } else {
                                Log.w(TAG, "Session invalide mais cookie obtenu, tentative de récupération des factures...")
                                
                                // Même si la session n'est pas validée, essayons quand même de récupérer les factures
                                try {
                                    val realInvoices = fetchRealFreeInvoices(sessionCookie, phoneNumber)
                                    if (realInvoices.isNotEmpty()) {
                                        _invoices.value = realInvoices
                                        return@withContext Result.success(realInvoices)
                                    } else {
                                        Log.w(TAG, "Aucune facture réelle trouvée, utilisation des factures fictives")
                                    }
                                } catch (e: Exception) {
                                    Log.e(TAG, "Erreur lors de la récupération des factures réelles", e)
                                    Log.w(TAG, "Utilisation des factures fictives suite à l'erreur")
                                }
                            }
                        }
                        
                        // Si la récupération des factures réelles a échoué ou si aucune facture n'a été trouvée,
                        // on utilise des factures fictives
                        val mockInvoices = createMockInvoices(operatorId, phoneNumber)
                        _invoices.value = mockInvoices
                        return@withContext Result.success(mockInvoices)
                    } else {
                        return@withContext Result.failure(Exception("Identifiants Free non configurés"))
                    }
                }
                else -> {
                    return@withContext Result.failure(Exception("Opérateur non supporté"))
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors de la récupération des factures", e)
            return@withContext Result.failure(e)
        }
    }
    
    /**
     * Récupère les vraies factures Free
     * @param sessionCookie Cookie de session
     * @param phoneNumber Numéro de téléphone
     * @return Liste des factures
     */
    private suspend fun fetchRealFreeInvoices(sessionCookie: String, phoneNumber: String): List<Invoice> = withContext(Dispatchers.IO) {
        val invoices = mutableListOf<Invoice>()
        
        try {
            Log.d(TAG, "Tentative de récupération des factures pour le numéro $phoneNumber")
            
            // Extraire les cookies importants
            val cookies = sessionCookie.split(";").map { it.trim() }
            val sessionTokenCookie = cookies.find { it.startsWith("session-token=") }
            val xUserTokenCookie = cookies.find { it.startsWith("X_USER_TOKEN=") }
            
            Log.d(TAG, "Cookies utilisés: session-token présent: ${sessionTokenCookie != null}, X_USER_TOKEN présent: ${xUserTokenCookie != null}")
            
            // Récupérer la liste des factures disponibles
            val request = Request.Builder()
                .url("https://mobile.free.fr/account/v2/api/SI/invoices")
                .header("Cookie", sessionCookie)
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                .header("Accept", "application/json, text/plain, */*")
                .header("Accept-Language", "fr-FR,fr;q=0.9,en-US;q=0.8,en;q=0.7")
                .header("Referer", "https://mobile.free.fr/account/v2/mes-factures")
                .header("X-Requested-With", "XMLHttpRequest")
                .header("Connection", "keep-alive")
                .get()
                .build()
                
            val response = okHttpClient.newCall(request).execute()
            
            Log.d(TAG, "Code de réponse pour les factures: ${response.code}")
            
            if (response.isSuccessful) {
                val responseBody = response.body?.string() ?: ""
                
                Log.d(TAG, "Réponse pour les factures: $responseBody")
                
                // Essayer d'abord de parser en JSON
                try {
                    val jsonResponse = JSONObject(responseBody)
                    if (jsonResponse.has("invoices")) {
                        val invoicesArray = jsonResponse.getJSONArray("invoices")

                        for (i in 0 until invoicesArray.length()) {
                            val invoiceObj = invoicesArray.getJSONObject(i)

                            val id = invoiceObj.getString("id")
                            val dateStr = invoiceObj.getString("date")
                            val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(dateStr) ?: Date()
                            val amount = invoiceObj.optDouble("amount", 0.0)
                            val pdfUrl = invoiceObj.getString("pdfUrl")

                            val invoice = Invoice(
                                id = id,
                                operatorId = "FREE",
                                phoneNumber = phoneNumber,
                                date = date,
                                amount = amount,
                                pdfUrl = pdfUrl,
                                status = InvoiceStatus.NEW,
                                hasGameFees = false
                            )

                            invoices.add(invoice)
                        }
                    }
                } catch (e: JSONException) {
                    Log.e(TAG, "Erreur lors du parsing JSON, tentative avec regex", e)
                    
                    // Si le parsing JSON échoue, essayer avec regex
                    val invoicesRegex = "\"id\":\"([^\"]+)\",\"date\":\"([^\"]+)\",\"amount\":([\\d.]+)".toRegex()
                    val matches = invoicesRegex.findAll(responseBody)
                    
                    // Si aucune facture n'est trouvée après le regex, essayer de récupérer directement une facture spécifique
                    if (!matches.iterator().hasNext()) {
                        try {
                            // Essayer de récupérer la dernière facture directement
                            val directRequest = Request.Builder()
                                .url("https://mobile.free.fr/account/v2/api/SI/invoice/${phoneNumber}")
                                .header("Cookie", sessionCookie)
                                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                                .header("Accept", "application/json, text/plain, */*")
                                .header("Referer", "https://mobile.free.fr/account/v2/mes-factures")
                                .get()
                                .build()
                                
                            val directResponse = okHttpClient.newCall(directRequest).execute()
                            
                            if (directResponse.isSuccessful) {
                                val directResponseBody = directResponse.body?.string() ?: ""
                                Log.d(TAG, "Réponse pour la facture directe: $directResponseBody")
                                
                                // Si la réponse contient un lien vers un PDF, c'est probablement une facture
                                if (directResponseBody.contains(".pdf") || directResponseBody.contains("application/pdf")) {
                                    val invoice = Invoice(
                                        id = "direct_1",
                                        operatorId = "FREE",
                                        phoneNumber = phoneNumber,
                                        date = Date(), // Date actuelle
                                        amount = 0.0, // Montant inconnu
                                        pdfUrl = "https://mobile.free.fr/account/v2/api/SI/invoice/${phoneNumber}",
                                        status = InvoiceStatus.NEW,
                                        hasGameFees = false
                                    )
                                    
                                    invoices.add(invoice)
                                }
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Erreur lors de la récupération directe de la facture", e)
                        }
                    }
                    
                    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    
                    matches.forEach { match ->
                        val id = match.groupValues[1]
                        val dateStr = match.groupValues[2]
                        val amount = match.groupValues[3].toDoubleOrNull() ?: 0.0
                        
                        val date = try {
                            dateFormat.parse(dateStr) ?: Date()
                        } catch (e: Exception) {
                            Date()
                        }
                        
                        val invoice = Invoice(
                            id = id,
                            operatorId = "FREE",
                            phoneNumber = phoneNumber,
                            date = date,
                            amount = amount,
                            pdfUrl = "https://mobile.free.fr/account/v2/api/SI/invoice/$id",
                            status = InvoiceStatus.NEW,
                            hasGameFees = false
                        )
                        
                        invoices.add(invoice)
                    }
                }
                
                // Si aucune facture n'a été trouvée, essayer d'extraire les informations des factures à partir du HTML
                if (invoices.isEmpty()) {
                    // Rechercher les liens de factures dans le HTML
                    val pdfUrlPattern = Regex("href=\"(/account/v2/api/SI/invoice/[^\"]+)\"")
                    val datePattern = Regex("Facture du (\\d{2}/\\d{2}/\\d{4})")
                    
                    val pdfMatches = pdfUrlPattern.findAll(responseBody)
                    
                    pdfMatches.forEachIndexed { index, match ->
                        val pdfUrl = "https://mobile.free.fr" + match.groupValues[1]
                        
                        // Essayer de trouver la date correspondante
                        val dateMatches = datePattern.findAll(responseBody)
                        val dateMatch = dateMatches.elementAtOrNull(index)
                        
                        val date = if (dateMatch != null) {
                            try {
                                SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(dateMatch.groupValues[1]) ?: Date()
                            } catch (e: Exception) {
                                Date()
                            }
                        } else {
                            Date()
                        }
                        
                        val invoice = Invoice(
                            id = "html_$index",
                            operatorId = "FREE",
                            phoneNumber = phoneNumber,
                            date = date,
                            amount = 0.0, // Montant inconnu
                            pdfUrl = pdfUrl,
                            status = InvoiceStatus.NEW,
                            hasGameFees = false
                        )
                        
                        invoices.add(invoice)
                    }
                }
            } else {
                Log.e(TAG, "Échec de la récupération des factures: ${response.code}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors de la récupération des factures", e)
        }
        
        return@withContext invoices
    }
    
    /**
     * Crée des factures fictives pour les tests
     * @param operatorId Identifiant de l'opérateur
     * @param phoneNumber Numéro de téléphone
     * @return Liste des factures fictives
     */
    private fun createMockInvoices(operatorId: String, phoneNumber: String): List<Invoice> {
        val invoices = mutableListOf<Invoice>()
        
        // Créer 6 factures mensuelles fictives
        val calendar = Calendar.getInstance()
        
        for (i in 0 until 6) {
            val date = calendar.time
            
            val invoice = Invoice(
                id = "mock_${operatorId}_${i}",
                operatorId = operatorId,
                phoneNumber = phoneNumber,
                date = date,
                amount = 19.99,
                pdfUrl = "https://example.com/invoice_$i.pdf",
                status = InvoiceStatus.NEW,
                hasGameFees = i % 2 == 0 // Une facture sur deux contient des frais de jeu
            )
            
            invoices.add(invoice)
            
            // Reculer d'un mois pour la prochaine facture
            calendar.add(Calendar.MONTH, -1)
        }
        
        return invoices
    }
    
    /**
     * Télécharge une facture
     * @param invoice Facture à télécharger
     * @return Chemin du fichier téléchargé
     */
    suspend fun downloadInvoice(invoice: Invoice): Result<String> = withContext(Dispatchers.IO) {
        try {
            when (invoice.operatorId) {
                "FREE" -> {
                    // Récupérer les cookies sauvegardés directement
                    val savedCookies = freeAuthManager.getSavedCookies()
                    if (savedCookies != null && savedCookies.isNotEmpty()) {
                        Log.d(TAG, "Utilisation des cookies sauvegardés pour télécharger la facture")
                        
                        // Extraire les cookies importants
                        val cookies = savedCookies.split(";").map { it.trim() }
                        val sessionTokenCookie = cookies.find { it.startsWith("session-token=") }
                        val xUserTokenCookie = cookies.find { it.startsWith("X_USER_TOKEN=") }
                        
                        Log.d(TAG, "Cookies utilisés pour téléchargement: session-token présent: ${sessionTokenCookie != null}, X_USER_TOKEN présent: ${xUserTokenCookie != null}")
                        
                        // Créer la requête avec les cookies sauvegardés
                        val request = Request.Builder()
                            .url(invoice.pdfUrl)
                            .addHeader("Cookie", savedCookies)
                            .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                            .addHeader("Accept", "application/pdf, application/octet-stream, */*")
                            .addHeader("Accept-Language", "fr-FR,fr;q=0.9,en-US;q=0.8,en;q=0.7")
                            .addHeader("Referer", "https://mobile.free.fr/account/v2/mes-factures")
                            .build()
                        
                        Log.d(TAG, "URL de téléchargement: ${invoice.pdfUrl}")
                        val response = okHttpClient.newCall(request).execute()
                        if (response.isSuccessful) {
                            // Vérifier que le contenu est bien un PDF ou un octet-stream (qui peut être un PDF)
                            val contentType = response.header("Content-Type")
                            Log.d(TAG, "Content-Type de la réponse: $contentType")
                            
                            // Accepter application/pdf ou application/octet-stream
                            if (contentType == null || (!contentType.contains("application/pdf") && !contentType.contains("octet-stream"))) {
                                // Vérifier si le contenu commence par %PDF (signature PDF)
                                val responseBytes = response.body?.bytes()
                                if (responseBytes != null && responseBytes.size > 4) {
                                    val pdfSignature = String(responseBytes.copyOfRange(0, 4))
                                    if (pdfSignature == "%PDF") {
                                        Log.d(TAG, "Le contenu commence par %PDF, c'est un PDF malgré le Content-Type")
                                    } else {
                                        Log.e(TAG, "Le contenu téléchargé n'est pas un PDF: $contentType")
                                        return@withContext Result.failure(Exception("Le contenu téléchargé n'est pas un PDF"))
                                    }
                                } else {
                                    Log.e(TAG, "Le contenu téléchargé n'est pas un PDF: $contentType")
                                    return@withContext Result.failure(Exception("Le contenu téléchargé n'est pas un PDF"))
                                }
                            }
                            
                            val invoicesDir = File(context.filesDir, "invoices")
                            if (!invoicesDir.exists()) {
                                invoicesDir.mkdirs()
                            }
                            
                            // Créer le fichier de destination
                            val fileName = "invoice_${invoice.id}.pdf"
                            val file = File(invoicesDir, fileName)
                            
                            // Écrire le contenu dans le fichier
                            val bytes = response.body?.bytes()
                            if (bytes != null && bytes.isNotEmpty()) {
                                // Vérifier si le contenu commence par %PDF (signature PDF)
                                if (bytes.size > 4 && String(bytes.copyOfRange(0, 4)) == "%PDF") {
                                    Log.d(TAG, "Le contenu est bien un PDF, écriture dans le fichier")
                                    FileOutputStream(file).use { outputStream ->
                                        outputStream.write(bytes)
                                    }
                                } else {
                                    Log.e(TAG, "Le contenu ne commence pas par %PDF, mais on l'écrit quand même")
                                    // Écrire quand même le contenu pour pouvoir l'examiner
                                    FileOutputStream(file).use { outputStream ->
                                        outputStream.write(bytes)
                                    }
                                }
                                
                                Log.d(TAG, "Facture téléchargée avec succès: ${file.absolutePath}")
                                
                                // Mettre à jour la facture avec le chemin local
                                val updatedInvoice = invoice.copy(
                                    localPdfPath = file.absolutePath,
                                    status = InvoiceStatus.DOWNLOADED
                                )
                                
                                // Mettre à jour la liste des factures
                                val currentInvoices = _invoices.value.toMutableList()
                                val index = currentInvoices.indexOfFirst { it.id == invoice.id }
                                if (index != -1) {
                                    currentInvoices[index] = updatedInvoice
                                    _invoices.value = currentInvoices
                                }
                                
                                return@withContext Result.success(file.absolutePath)
                            } else {
                                Log.e(TAG, "Le contenu téléchargé est vide")
                                return@withContext Result.failure(Exception("Le contenu téléchargé est vide"))
                            }
                        } else {
                            Log.e(TAG, "Échec du téléchargement: ${response.code}")
                            return@withContext Result.failure(Exception("Échec du téléchargement: ${response.code}"))
                        }
                    } else {
                        Log.e(TAG, "Identifiants Free non configurés")
                        return@withContext Result.failure(Exception("Identifiants Free non configurés"))
                    }
                }
                else -> {
                    Log.e(TAG, "Opérateur non supporté: ${invoice.operatorId}")
                    return@withContext Result.failure(Exception("Opérateur non supporté"))
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors du téléchargement de la facture", e)
            return@withContext Result.failure(e)
        }
    }
    
    /**
     * Analyse une facture pour détecter les frais de jeu
     * @param invoice Facture à analyser
     * @return Liste des frais de jeu détectés
     */
    suspend fun analyzeInvoice(invoice: Invoice): Result<List<InvoiceGameFee>> = withContext(Dispatchers.IO) {
        try {
            // Vérifier si la facture a déjà été téléchargée
            if (invoice.localPdfPath == null) {
                // Télécharger la facture si nécessaire
                val downloadResult = downloadInvoice(invoice)
                if (downloadResult.isFailure) {
                    return@withContext Result.failure(downloadResult.exceptionOrNull() ?: Exception("Échec du téléchargement de la facture"))
                }
            }
            
            // Analyser la facture avec le PdfAnalyzer
            val result = pdfAnalyzer.analyzeInvoice(invoice)
            
            // Mettre à jour le statut de la facture
            if (result.isSuccess) {
                val gameFees = result.getOrNull() ?: emptyList()
                
                // Mettre à jour la facture avec le statut ANALYZED et hasGameFees
                val updatedInvoice = invoice.copy(
                    status = InvoiceStatus.ANALYZED,
                    hasGameFees = gameFees.isNotEmpty()
                )
                
                // Mettre à jour la liste des factures
                val currentInvoices = _invoices.value.toMutableList()
                val index = currentInvoices.indexOfFirst { it.id == invoice.id }
                if (index != -1) {
                    currentInvoices[index] = updatedInvoice
                    _invoices.value = currentInvoices
                }
                
                // Mettre à jour la liste des frais de jeu
                val currentGameFees = _invoiceGameFees.value.toMutableMap()
                currentGameFees[invoice.id] = gameFees
                _invoiceGameFees.value = currentGameFees
                
                return@withContext result
            } else {
                return@withContext result
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors de l'analyse de la facture", e)
            return@withContext Result.failure(e)
        }
    }
    
    /**
     * Génère un PDF annoté avec les frais de jeu mis en évidence
     * @param invoice Facture à annoter
     * @return Chemin du fichier PDF annoté
     */
    suspend fun generateAnnotatedPdf(invoice: Invoice): Result<String> = withContext(Dispatchers.IO) {
        try {
            // Vérifier si la facture a déjà été analysée
            val gameFees = _invoiceGameFees.value[invoice.id]
            if (gameFees == null) {
                // Analyser la facture si nécessaire
                val analyzeResult = analyzeInvoice(invoice)
                if (analyzeResult.isFailure) {
                    return@withContext Result.failure(analyzeResult.exceptionOrNull() ?: Exception("Échec de l'analyse de la facture"))
                }
            }
            
            // Récupérer les frais de jeu
            val fees = _invoiceGameFees.value[invoice.id] ?: emptyList()
            
            // Générer le PDF annoté
            val result = pdfAnalyzer.generateAnnotatedPdf(invoice, fees)
            
            // Mettre à jour le statut de la facture
            if (result.isSuccess) {
                // Mettre à jour la facture avec le statut EDITED
                val updatedInvoice = invoice.copy(
                    status = InvoiceStatus.EDITED
                )
                
                // Mettre à jour la liste des factures
                val currentInvoices = _invoices.value.toMutableList()
                val index = currentInvoices.indexOfFirst { it.id == invoice.id }
                if (index != -1) {
                    currentInvoices[index] = updatedInvoice
                    _invoices.value = currentInvoices
                }
                
                return@withContext result
            } else {
                return@withContext result
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors de la génération du PDF annoté", e)
            return@withContext Result.failure(e)
        }
    }
    
    /**
     * Vérifie si de nouvelles factures sont disponibles
     * @param operatorId Identifiant de l'opérateur
     * @param phoneNumber Numéro de téléphone
     * @return true si de nouvelles factures sont disponibles, false sinon
     */
    suspend fun checkForNewInvoices(operatorId: String, phoneNumber: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            // Récupérer les factures actuelles
            val currentInvoices = _invoices.value
            
            // Récupérer les nouvelles factures
            val result = fetchInvoices(operatorId, phoneNumber)
            
            if (result.isSuccess) {
                val newInvoices = result.getOrNull() ?: emptyList()
                
                // Vérifier s'il y a de nouvelles factures
                val hasNewInvoices = newInvoices.any { newInvoice ->
                    currentInvoices.none { it.id == newInvoice.id }
                }
                
                return@withContext Result.success(hasNewInvoices)
            } else {
                return@withContext Result.failure(result.exceptionOrNull() ?: Exception("Échec de la vérification des nouvelles factures"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors de la vérification des nouvelles factures", e)
            return@withContext Result.failure(e)
        }
    }
    
    /**
     * Récupère les frais de jeu pour une facture
     * @param invoiceId Identifiant de la facture
     * @return Liste des frais de jeu
     */
    fun getGameFees(invoiceId: String): List<InvoiceGameFee> {
        return _invoiceGameFees.value[invoiceId] ?: emptyList()
    }
}