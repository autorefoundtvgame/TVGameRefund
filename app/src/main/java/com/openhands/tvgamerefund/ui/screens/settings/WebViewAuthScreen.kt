package com.openhands.tvgamerefund.ui.screens.settings

import android.annotation.SuppressLint
import android.util.Log
import android.webkit.CookieManager
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import com.openhands.tvgamerefund.data.models.Operator
import com.openhands.tvgamerefund.data.models.OperatorCredentials
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WebViewAuthScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onAuthSuccess: () -> Unit = {},
    onBack: () -> Unit = {}
) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    
    var isLoading by remember { mutableStateOf(true) }
    var isAuthenticated by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var cookies by remember { mutableStateOf("") }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Authentification Web") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            
            if (isAuthenticated) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Authentification réussie !",
                        style = MaterialTheme.typography.headlineMedium
                    )
                    
                    Text(
                        text = "Les cookies ont été récupérés et vos identifiants ont été sauvegardés.",
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                    
                    Button(
                        onClick = onAuthSuccess,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Continuer")
                    }
                }
            } else if (errorMessage != null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Erreur d'authentification",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                    
                    Text(
                        text = errorMessage ?: "Une erreur inconnue s'est produite",
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                    
                    Button(
                        onClick = onBack,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Retour")
                    }
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Bouton pour terminer manuellement l'authentification
                    Button(
                        onClick = {
                            // Récupérer les cookies
                            val cookieManager = CookieManager.getInstance()
                            cookies = cookieManager.getCookie("https://mobile.free.fr") ?: ""
                            
                            if (cookies.isNotEmpty()) {
                                Log.d("WebViewAuth", "Cookies récupérés manuellement: $cookies")
                                
                                // Sauvegarder les identifiants
                                coroutineScope.launch {
                                    val credentials = OperatorCredentials(
                                        operator = Operator.FREE,
                                        username = viewModel.uiState.value.username,
                                        password = viewModel.uiState.value.password,
                                        isActive = true
                                    )
                                    
                                    viewModel.saveCredentialsWithCookies(credentials, cookies)
                                    
                                    // Attendre un peu pour que l'utilisateur voie le message de succès
                                    delay(500)
                                    isAuthenticated = true
                                }
                            } else {
                                errorMessage = "Aucun cookie n'a été récupéré. Assurez-vous d'être connecté."
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text("J'ai terminé ma connexion - Sauvegarder les cookies")
                    }
                    
                    // WebView pour l'authentification
                    WebViewAuth(
                        modifier = Modifier.weight(1f),
                        onPageFinished = { url ->
                            isLoading = false
                            
                            // Vérifier si l'authentification a réussi
                            if (url.contains("/account/v2/") && !url.contains("/login")) {
                                Log.d("WebViewAuth", "Page de compte détectée: $url")
                                
                                // Récupérer les cookies
                                val cookieManager = CookieManager.getInstance()
                                val newCookies = cookieManager.getCookie("https://mobile.free.fr") ?: ""
                                
                                if (newCookies.isNotEmpty()) {
                                    Log.d("WebViewAuth", "Cookies récupérés automatiquement: $newCookies")
                                    cookies = newCookies
                                }
                            }
                        },
                        onError = { error ->
                            isLoading = false
                            errorMessage = error
                        }
                    )
                }
            }
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebViewAuth(
    modifier: Modifier = Modifier,
    onPageFinished: (String) -> Unit,
    onError: (String) -> Unit
) {
    val context = LocalContext.current
    
    DisposableEffect(Unit) {
        // Effacer les cookies existants
        CookieManager.getInstance().removeAllCookies(null)
        CookieManager.getInstance().flush()
        
        onDispose {
            // Nettoyage si nécessaire
        }
    }
    
    AndroidView(
        factory = { context ->
            WebView(context).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.userAgentString = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36"
                
                webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                        return false
                    }
                    
                    override fun onPageFinished(view: WebView, url: String) {
                        super.onPageFinished(view, url)
                        Log.d("WebViewAuth", "Page chargée: $url")
                        onPageFinished(url)
                    }
                    
                    @Deprecated("Deprecated in Java")
                    override fun onReceivedError(view: WebView, errorCode: Int, description: String, failingUrl: String) {
                        super.onReceivedError(view, errorCode, description, failingUrl)
                        Log.e("WebViewAuth", "Erreur: $description")
                        onError("Erreur lors du chargement de la page: $description")
                    }
                    
                    override fun onReceivedError(view: WebView, request: android.webkit.WebResourceRequest, error: android.webkit.WebResourceError) {
                        super.onReceivedError(view, request, error)
                        Log.e("WebViewAuth", "Erreur: ${error.description}")
                        onError("Erreur lors du chargement de la page: ${error.description}")
                    }
                }
                
                // Charger la page de login
                loadUrl("https://mobile.free.fr/account/v2/login")
            }
        },
        modifier = modifier.fillMaxSize()
    )
}