package com.openhands.tvgamerefund.ui.screens.settings

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.openhands.tvgamerefund.data.models.Operator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OperatorDropdown(
    selectedOperator: Operator,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onOperatorSelected: (Operator) -> Unit,
    modifier: Modifier = Modifier
) {
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = onExpandedChange,
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selectedOperator.displayName,
            onValueChange = { },
            readOnly = true,
            label = { Text("Opérateur") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )

        if (expanded) {
            androidx.compose.material3.DropdownMenu(
                expanded = expanded,
                onDismissRequest = { onExpandedChange(false) }
            ) {
                Operator.values().forEach { operator ->
                    DropdownMenuItem(
                        text = { Text(operator.displayName) },
                        onClick = {
                            onOperatorSelected(operator)
                            onExpandedChange(false)
                        },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                    )
                }
            }
        }
    }
}

@Composable
fun ErrorMessage(
    error: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = error,
        color = MaterialTheme.colorScheme.error,
        modifier = modifier.fillMaxWidth()
    )
}

@Composable
fun SuccessMessage(
    modifier: Modifier = Modifier
) {
    Text(
        text = "Connexion réussie !",
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier.fillMaxWidth()
    )
}