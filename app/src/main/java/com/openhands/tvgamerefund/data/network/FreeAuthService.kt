package com.openhands.tvgamerefund.data.network

import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implémentation du service d'authentification Free utilisant OkHttp directement
 * car l'authentification Free utilise des cookies et des redirections spécifiques
 */
@Singleton
class FreeAuthManager @Inject constructor(
    val okHttpClient: OkHttpClient,
    @ApplicationContext private val context: Context
) {
    private val TAG = "FreeAuthManager"
    
    /**
     * Récupère les cookies sauvegardés pour Free Mobile
     * @return Cookies sauvegardés ou null si aucun cookie n'est disponible
     */
    fun getSavedCookies(): String? {
        val sharedPreferences = context.getSharedPreferences("auth_cookies", Context.MODE_PRIVATE)
        return sharedPreferences.getString("FREE", null)
    }
    
    /**
     * Authentifie l'utilisateur sur Free Mobile et retourne le cookie de session
     * @param login Identifiant Free Mobile
     * @param password Mot de passe Free Mobile
     * @return Cookie de session si l'authentification réussit, null sinon
     */
    suspend fun authenticate(login: String, password: String): String? = withContext(Dispatchers.IO) {
        // Vérifier d'abord si nous avons des cookies sauvegardés
        val savedCookies = getSavedCookies()
        if (savedCookies != null) {
            Log.d(TAG, "Utilisation des cookies sauvegardés")
            // Vérifier si les cookies sont toujours valides
            if (isAuthenticated(savedCookies)) {
                Log.d(TAG, "Cookies sauvegardés valides")
                return@withContext savedCookies
            } else {
                Log.d(TAG, "Cookies sauvegardés invalides, tentative d'authentification standard")
            }
        }
        try {
            Log.d(TAG, "Tentative d'authentification pour l'utilisateur: $login")
            
            // Étape 1: Récupérer la page de login pour obtenir le token CSRF
            val csrfToken = getCsrfToken()
            if (csrfToken == null) {
                Log.e(TAG, "Impossible de récupérer le token CSRF")
                return@withContext null
            }
            
            // Étape 2: Soumettre le formulaire de connexion avec le token CSRF
            val formattedLogin = login.trim() // Supprimer les espaces éventuels
            
            // Construire le corps de la requête
            val formBody = FormBody.Builder()
                .add("login", formattedLogin)
                .add("pwd", password)
                .add("csrf-token", csrfToken)
                .build()

            // Construire la requête avec tous les en-têtes nécessaires
            val loginRequest = Request.Builder()
                .url("https://mobile.free.fr/account/v2/login")
                .post(formBody)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("Referer", "https://mobile.free.fr/account/v2/login")
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
                .header("Accept-Language", "fr,fr-FR;q=0.8,en-US;q=0.5,en;q=0.3")
                .header("Origin", "https://mobile.free.fr")
                .build()

            Log.d(TAG, "Envoi de la requête d'authentification...")
            val loginResponse = okHttpClient.newCall(loginRequest).execute()
            
            // Vérifier si l'authentification a réussi
            Log.d(TAG, "Code de réponse: ${loginResponse.code}")
            Log.d(TAG, "Message: ${loginResponse.message}")
            
            // Récupérer les cookies
            val cookies = loginResponse.headers("Set-Cookie")
            Log.d(TAG, "Cookies reçus: ${cookies.joinToString(", ")}")
            
            // Vérifier la redirection
            val location = loginResponse.header("Location")
            Log.d(TAG, "Redirection vers: $location")
            
            if (loginResponse.isSuccessful || (loginResponse.code in 300..399 && !location.isNullOrEmpty() && !location.contains("login"))) {
                // Authentification réussie si:
                // - Réponse 200 OK, ou
                // - Redirection vers une page autre que login
                
                if (cookies.isNotEmpty()) {
                    val sessionCookie = cookies.joinToString("; ") { it.split(";")[0] }
                    Log.d(TAG, "Authentification réussie, cookie: $sessionCookie")
                    return@withContext sessionCookie
                } else {
                    // Si pas de cookies mais redirection vers une page autre que login
                    if (!location.isNullOrEmpty() && !location.contains("login")) {
                        // Suivre la redirection pour obtenir les cookies
                        val redirectRequest = Request.Builder()
                            .url(if (location.startsWith("http")) location else "https://mobile.free.fr$location")
                            .get()
                            .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                            .build()
                            
                        val redirectResponse = okHttpClient.newCall(redirectRequest).execute()
                        val redirectCookies = redirectResponse.headers("Set-Cookie")
                        
                        if (redirectCookies.isNotEmpty()) {
                            val sessionCookie = redirectCookies.joinToString("; ") { it.split(";")[0] }
                            Log.d(TAG, "Cookies obtenus après redirection: $sessionCookie")
                            return@withContext sessionCookie
                        }
                    }
                }
            } else {
                // Essayons de lire le corps de la réponse pour comprendre l'erreur
                val responseBody = loginResponse.body?.string()
                if (!responseBody.isNullOrEmpty()) {
                    val previewLength = minOf(responseBody.length, 500)
                    Log.e(TAG, "Corps de la réponse d'erreur: ${responseBody.substring(0, previewLength)}")
                    
                    // Vérifier si le corps contient un message d'erreur
                    if (responseBody.contains("identifiant ou mot de passe incorrect", ignoreCase = true) ||
                        responseBody.contains("incorrect login or password", ignoreCase = true)) {
                        Log.e(TAG, "Identifiants incorrects")
                    }
                }
                
                Log.e(TAG, "Échec de l'authentification: ${loginResponse.code}")
            }
            
            return@withContext null
        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors de l'authentification", e)
            return@withContext null
        }
    }
    
    /**
     * Récupère le token CSRF de la page de login
     * @return Token CSRF si trouvé, null sinon
     */
    private suspend fun getCsrfToken(): String? = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("https://mobile.free.fr/account/v2/login")
                .get()
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                .build()
                
            val response = okHttpClient.newCall(request).execute()
            
            if (response.isSuccessful) {
                val responseBody = response.body?.string() ?: ""
                
                // Enregistrer les premiers 1000 caractères pour le débogage
                val previewLength = minOf(responseBody.length, 1000)
                Log.d(TAG, "Début de la page de login: ${responseBody.substring(0, previewLength)}")
                
                // Recherche du token CSRF dans le HTML - essayons plusieurs patterns
                val csrfPatterns = listOf(
                    "name=\"csrf-token\"\\s+content=\"([^\"]+)\"",
                    "meta\\s+name=\"csrf-token\"\\s+content=\"([^\"]+)\"",
                    "input\\s+type=\"hidden\"\\s+name=\"csrf-token\"\\s+value=\"([^\"]+)\"",
                    "csrf-token\"\\s*:\\s*\"([^\"]+)\"",
                    "name=\"_csrf\"\\s+value=\"([^\"]+)\"",
                    "_csrf\\s*=\\s*\"([^\"]+)\"",
                    "name=\"csrfToken\"\\s+value=\"([^\"]+)\"",
                    "csrfToken\\s*=\\s*\"([^\"]+)\""
                )
                
                for (pattern in csrfPatterns) {
                    val regex = pattern.toRegex()
                    val matchResult = regex.find(responseBody)
                    
                    if (matchResult != null && matchResult.groupValues.size > 1) {
                        val csrfToken = matchResult.groupValues[1]
                        Log.d(TAG, "Token CSRF trouvé avec pattern '$pattern': $csrfToken")
                        return@withContext csrfToken
                    }
                }
                
                // Si nous n'avons pas trouvé de token avec les patterns, cherchons manuellement
                val metaTags = responseBody.split("<meta").filter { it.contains("csrf-token") }
                if (metaTags.isNotEmpty()) {
                    Log.d(TAG, "Meta tags contenant csrf-token: $metaTags")
                    
                    // Essayons d'extraire le token manuellement
                    val contentPattern = "content=\"([^\"]+)\"".toRegex()
                    for (tag in metaTags) {
                        val contentMatch = contentPattern.find(tag)
                        if (contentMatch != null && contentMatch.groupValues.size > 1) {
                            val csrfToken = contentMatch.groupValues[1]
                            Log.d(TAG, "Token CSRF extrait manuellement: $csrfToken")
                            return@withContext csrfToken
                        }
                    }
                }
                
                Log.e(TAG, "Token CSRF non trouvé dans la page")
            } else {
                Log.e(TAG, "Échec de la récupération de la page de login: ${response.code}")
                Log.e(TAG, "Message d'erreur: ${response.message}")
                
                // Essayons de lire le corps de la réponse même en cas d'erreur
                val errorBody = response.body?.string()
                if (!errorBody.isNullOrEmpty()) {
                    Log.e(TAG, "Corps de la réponse d'erreur: $errorBody")
                }
            }
            
            return@withContext null
        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors de la récupération du token CSRF", e)
            return@withContext null
        }
    }
    
    /**
     * Vérifie si l'utilisateur est authentifié
     * @param sessionCookie Cookie de session
     * @return true si l'utilisateur est authentifié, false sinon
     */
    suspend fun isAuthenticated(sessionCookie: String): Boolean = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Vérification de l'authentification avec cookie: $sessionCookie")
            
            val request = Request.Builder()
                .url("https://mobile.free.fr/account/v2/")
                .header("Cookie", sessionCookie)
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                .get()
                .build()
                
            val response = okHttpClient.newCall(request).execute()
            
            Log.d(TAG, "Code de réponse: ${response.code}")
            Log.d(TAG, "URL finale: ${response.request.url}")
            
            // Vérifier si nous sommes redirigés vers la page de login
            val isRedirectedToLogin = response.request.url.toString().contains("/login")
            
            if (isRedirectedToLogin) {
                Log.e(TAG, "Redirection vers la page de login, l'utilisateur n'est pas authentifié")
                return@withContext false
            }
            
            // Vérifier le contenu de la page pour confirmer l'authentification
            if (response.isSuccessful) {
                val responseBody = response.body?.string() ?: ""
                
                // Rechercher des éléments qui confirment l'authentification
                val isAuthenticated = responseBody.contains("Déconnexion", ignoreCase = true) || 
                                     responseBody.contains("Mon compte", ignoreCase = true) ||
                                     responseBody.contains("Mes factures", ignoreCase = true)
                
                Log.d(TAG, "Authentification vérifiée: $isAuthenticated")
                return@withContext isAuthenticated
            }
            
            Log.e(TAG, "Échec de la vérification d'authentification: ${response.code}")
            return@withContext false
        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors de la vérification de l'authentification", e)
            return@withContext false
        }
    }
    
    /**
     * Tente une authentification directe sans utiliser le token CSRF
     * Cette méthode est une alternative si l'authentification standard échoue
     */
    suspend fun authenticateDirect(login: String, password: String): String? = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Tentative d'authentification directe pour l'utilisateur: $login")
            
            // D'abord, récupérer les cookies de la page de login
            val initialRequest = Request.Builder()
                .url("https://mobile.free.fr/account/v2/login")
                .get()
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
                .header("Accept-Language", "fr,fr-FR;q=0.8,en-US;q=0.5,en;q=0.3")
                .build()
                
            val initialResponse = okHttpClient.newCall(initialRequest).execute()
            val initialCookies = initialResponse.headers("Set-Cookie")
            val initialCookieString = if (initialCookies.isNotEmpty()) {
                initialCookies.joinToString("; ") { it.split(";")[0] }
            } else {
                ""
            }
            
            Log.d(TAG, "Cookies initiaux: $initialCookieString")
            
            // Construire le corps de la requête
            val formBody = FormBody.Builder()
                .add("login", login.trim())
                .add("pwd", password)
                .build()

            // Construire la requête avec tous les en-têtes nécessaires
            val loginRequest = Request.Builder()
                .url("https://mobile.free.fr/account/v2/login")
                .post(formBody)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
                .header("Accept-Language", "fr,fr-FR;q=0.8,en-US;q=0.5,en;q=0.3")
                .header("Origin", "https://mobile.free.fr")
                .header("Referer", "https://mobile.free.fr/account/v2/login")
                .apply {
                    if (initialCookieString.isNotEmpty()) {
                        header("Cookie", initialCookieString)
                    }
                }
                .build()

            val loginResponse = okHttpClient.newCall(loginRequest).execute()
            
            // Vérifier si l'authentification a réussi
            Log.d(TAG, "Code de réponse (auth directe): ${loginResponse.code}")
            
            // Récupérer les cookies
            val cookies = loginResponse.headers("Set-Cookie")
            Log.d(TAG, "Cookies reçus (auth directe): ${cookies.joinToString(", ")}")
            
            // Vérifier la redirection
            val location = loginResponse.header("Location")
            Log.d(TAG, "Redirection vers (auth directe): $location")
            
            // Vérifier le corps de la réponse
            val responseBody = loginResponse.body?.string()
            if (!responseBody.isNullOrEmpty()) {
                val previewLength = minOf(responseBody.length, 500)
                Log.d(TAG, "Corps de la réponse (auth directe): ${responseBody.substring(0, previewLength)}")
            }
            
            if (loginResponse.isSuccessful || (loginResponse.code in 300..399 && !location.isNullOrEmpty() && !location.contains("login"))) {
                // Authentification réussie si:
                // - Réponse 200 OK, ou
                // - Redirection vers une page autre que login
                
                if (cookies.isNotEmpty()) {
                    val sessionCookie = cookies.joinToString("; ") { it.split(";")[0] }
                    Log.d(TAG, "Authentification directe réussie, cookie: $sessionCookie")
                    return@withContext sessionCookie
                } else if (!location.isNullOrEmpty() && !location.contains("login")) {
                    // Suivre la redirection pour obtenir les cookies
                    val redirectRequest = Request.Builder()
                        .url(if (location.startsWith("http")) location else "https://mobile.free.fr$location")
                        .get()
                        .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                        .apply {
                            if (initialCookieString.isNotEmpty()) {
                                header("Cookie", initialCookieString)
                            }
                        }
                        .build()
                        
                    val redirectResponse = okHttpClient.newCall(redirectRequest).execute()
                    val redirectCookies = redirectResponse.headers("Set-Cookie")
                    
                    if (redirectCookies.isNotEmpty()) {
                        val sessionCookie = redirectCookies.joinToString("; ") { it.split(";")[0] }
                        Log.d(TAG, "Cookies obtenus après redirection (auth directe): $sessionCookie")
                        return@withContext sessionCookie
                    }
                }
                
                // Si nous n'avons pas de cookies mais que l'authentification semble réussie,
                // retournons les cookies initiaux comme fallback
                if (initialCookieString.isNotEmpty()) {
                    Log.d(TAG, "Utilisation des cookies initiaux comme fallback: $initialCookieString")
                    return@withContext initialCookieString
                }
            }
            
            return@withContext null
        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors de l'authentification directe", e)
            return@withContext null
        }
    }
}