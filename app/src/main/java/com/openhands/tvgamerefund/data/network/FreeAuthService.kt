package com.openhands.tvgamerefund.data.network

import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service pour l'authentification à Free Mobile
 */
interface FreeAuthService {
    @FormUrlEncoded
    @POST("https://mobile.free.fr/account/login.php")
    suspend fun login(
        @Field("login") login: String,
        @Field("pwd") password: String
    ): Response<String>
}

/**
 * Implémentation du service d'authentification Free utilisant OkHttp directement
 * car l'authentification Free utilise des cookies et des redirections spécifiques
 */
@Singleton
class FreeAuthManager @Inject constructor(
    private val okHttpClient: OkHttpClient
) {
    /**
     * Authentifie l'utilisateur sur Free Mobile et retourne le cookie de session
     * @param login Identifiant Free Mobile
     * @param password Mot de passe Free Mobile
     * @return Cookie de session si l'authentification réussit, null sinon
     */
    suspend fun authenticate(login: String, password: String): String? {
        try {
            val formBody = FormBody.Builder()
                .add("login", login)
                .add("pwd", password)
                .build()

            val request = Request.Builder()
                .url("https://mobile.free.fr/account/login.php")
                .post(formBody)
                .build()

            val response = okHttpClient.newCall(request).execute()
            
            // Vérifier si l'authentification a réussi
            if (response.isSuccessful && response.headers("Set-Cookie").isNotEmpty()) {
                // Extraire et retourner le cookie de session
                return response.headers("Set-Cookie")
                    .firstOrNull { it.startsWith("PHPSESSID") }
                    ?.split(";")
                    ?.firstOrNull()
            }
            
            return null
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }
}