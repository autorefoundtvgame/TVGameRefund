package com.openhands.tvgamerefund.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.openhands.tvgamerefund.data.models.Operator

@Composable
fun OperatorDropdown(
    selectedOperator: Operator,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onOperatorSelected: (Operator) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        OutlinedCard(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onExpandedChange(true) }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Opérateur : ${selectedOperator.displayName}",
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Sélectionner un opérateur"
                )
            }
        }
        
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandedChange(false) },
            modifier = Modifier.fillMaxWidth(0.9f)
        ) {
            Operator.values().forEach { operator ->
                DropdownMenuItem(
                    text = { Text(operator.displayName) },
                    onClick = {
                        onOperatorSelected(operator)
                        onExpandedChange(false)
                    }
                )
            }
        }
    }
}

@Composable
fun ErrorMessage(error: String) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.errorContainer)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = "Erreur",
                tint = MaterialTheme.colorScheme.error
            )
            Text(
                text = error,
                color = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier
                    .padding(start = 8.dp)
                    .weight(1f)
            )
        }
    }
}

@Composable
fun SuccessMessage(message: String = "Opération réussie") {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primaryContainer)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Succès",
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = message,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier
                    .padding(start = 8.dp)
                    .weight(1f)
            )
        }
    }
}