package com.openhands.tvgamerefund.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Paramètres") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            var expanded by remember { mutableStateOf(false) }
            
            OperatorDropdown(
                selectedOperator = uiState.selectedOperator,
                expanded = expanded,
                onExpandedChange = { expanded = it },
                onOperatorSelected = viewModel::onOperatorSelected,
                modifier = Modifier.fillMaxWidth()
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
                    onClick = { viewModel.testConnection() },
                    modifier = Modifier.weight(1f),
                    enabled = !uiState.isLoading
                ) {
                    Text("Tester la connexion")
                }

                Button(
                    onClick = { viewModel.saveCredentials() },
                    modifier = Modifier.weight(1f),
                    enabled = !uiState.isLoading && uiState.isTestSuccessful
                ) {
                    Text("Enregistrer")
                }
            }

            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }

            uiState.error?.let { error ->
                ErrorMessage(error = error)
            }

            if (uiState.isTestSuccessful) {
                SuccessMessage()
            }
        }
    }
}