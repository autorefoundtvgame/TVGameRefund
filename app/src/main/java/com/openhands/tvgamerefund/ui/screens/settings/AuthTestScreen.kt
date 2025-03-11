package com.openhands.tvgamerefund.ui.screens.settings

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.openhands.tvgamerefund.data.network.FreeAuthManager
import kotlinx.coroutines.launch
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import javax.inject.Inject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthTestScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    // Injecter FreeAuthManager via Hilt
    val authManagerViewModel = hiltViewModel<FreeAuthManagerViewModel>()
    val authManager = authManagerViewModel.authManager
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val coroutineScope = rememberCoroutineScope()
    
    var isLoading by remember { mutableStateOf(false) }
    var testResults by remember { mutableStateOf("") }
    var csrfToken by remember { mutableStateOf("") }
    var sessionCookie by remember { mutableStateOf("") }
    var isAuthenticated by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Test d'authentification") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Cet écran permet de tester l'authentification à Free Mobile",
                style = MaterialTheme.typography.bodyLarge
            )
            
            OutlinedTextField(
                value = uiState.username,
                onValueChange = viewModel::onUsernameChanged,
                label = { Text("Identifiant") },
                modifier = Modifier.fillMaxWidth()
            )
            
            OutlinedTextField(
                value = uiState.password,
                onValueChange = viewModel::onPasswordChanged,
                label = { Text("Mot de passe") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            isLoading = true
                            testResults = "Test en cours...\n"
                            
                            try {
                                // Test 1: Récupération du token CSRF
                                testResults += "1. Récupération du token CSRF...\n"
                                val token = getCsrfToken(authManager.okHttpClient)
                                csrfToken = token ?: "Non trouvé"
                                testResults += "Token CSRF: $csrfToken\n\n"
                                
                                // Test 2: Authentification standard
                                testResults += "2. Authentification standard...\n"
                                val cookie = authManager.authenticate(uiState.username, uiState.password)
                                sessionCookie = cookie ?: "Non obtenu"
                                testResults += "Cookie de session: $sessionCookie\n\n"
                                
                                // Test 3: Authentification directe
                                if (cookie == null) {
                                    testResults += "3. Authentification directe...\n"
                                    val directCookie = authManager.authenticateDirect(uiState.username, uiState.password)
                                    sessionCookie = directCookie ?: "Non obtenu"
                                    testResults += "Cookie de session (direct): $sessionCookie\n\n"
                                }
                                
                                // Test 4: Vérification de l'authentification
                                if (sessionCookie != "Non obtenu") {
                                    testResults += "4. Vérification de l'authentification...\n"
                                    isAuthenticated = authManager.isAuthenticated(sessionCookie)
                                    testResults += "Authentifié: $isAuthenticated\n\n"
                                }
                                
                                testResults += "Tests terminés."
                            } catch (e: Exception) {
                                testResults += "Erreur: ${e.message}\n"
                                e.printStackTrace()
                            } finally {
                                isLoading = false
                            }
                        }
                    },
                    modifier = Modifier.weight(1f),
                    enabled = !isLoading && uiState.username.isNotBlank() && uiState.password.isNotBlank()
                ) {
                    Text("Tester l'authentification")
                }
            }
            
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
            
            Divider()
            
            Text(
                text = "Résultats du test",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = testResults,
                style = MaterialTheme.typography.bodyMedium
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Résumé",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Token CSRF: ${if (csrfToken.isNotBlank()) "Obtenu" else "Non obtenu"}")
                Text("Cookie de session: ${if (sessionCookie.isNotBlank() && sessionCookie != "Non obtenu") "Obtenu" else "Non obtenu"}")
                Text("Authentifié: $isAuthenticated")
            }
        }
    }
}

suspend fun getCsrfToken(okHttpClient: OkHttpClient): String? {
    val request = Request.Builder()
        .url("https://mobile.free.fr/account/v2/login")
        .get()
        .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
        .build()
        
    val response = okHttpClient.newCall(request).execute()
    
    if (response.isSuccessful) {
        val responseBody = response.body?.string() ?: ""
        
        // Recherche du token CSRF dans le HTML - essayons plusieurs patterns
        val csrfPatterns = listOf(
            "name=\"csrf-token\"\\s+content=\"([^\"]+)\"",
            "meta\\s+name=\"csrf-token\"\\s+content=\"([^\"]+)\"",
            "input\\s+type=\"hidden\"\\s+name=\"csrf-token\"\\s+value=\"([^\"]+)\"",
            "csrf-token\"\\s*:\\s*\"([^\"]+)\""
        )
        
        for (pattern in csrfPatterns) {
            val regex = pattern.toRegex()
            val matchResult = regex.find(responseBody)
            
            if (matchResult != null && matchResult.groupValues.size > 1) {
                return matchResult.groupValues[1]
            }
        }
        
        // Si nous n'avons pas trouvé de token avec les patterns, cherchons manuellement
        val metaTags = responseBody.split("<meta").filter { it.contains("csrf-token") }
        if (metaTags.isNotEmpty()) {
            // Essayons d'extraire le token manuellement
            val contentPattern = "content=\"([^\"]+)\"".toRegex()
            for (tag in metaTags) {
                val contentMatch = contentPattern.find(tag)
                if (contentMatch != null && contentMatch.groupValues.size > 1) {
                    return contentMatch.groupValues[1]
                }
            }
        }
    }
    
    return null
}