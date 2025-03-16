package com.openhands.tvgamerefund.data.repository

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.openhands.tvgamerefund.data.models.Operator
import com.openhands.tvgamerefund.data.models.OperatorCredentials
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OperatorCredentialsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)

    private val sharedPreferences = EncryptedSharedPreferences.create(
        "operator_credentials",
        masterKeyAlias,
        context,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun saveCredentials(credentials: OperatorCredentials) {
        sharedPreferences.edit().apply {
            putString("${credentials.operator.name}_username", credentials.username)
            putString("${credentials.operator.name}_password", credentials.password)
            putBoolean("${credentials.operator.name}_active", credentials.isActive)
        }.apply()
    }

    fun getCredentials(operator: Operator): OperatorCredentials? {
        val username = sharedPreferences.getString("${operator.name}_username", null) ?: return null
        val password = sharedPreferences.getString("${operator.name}_password", null) ?: return null
        val isActive = sharedPreferences.getBoolean("${operator.name}_active", false)

        return OperatorCredentials(
            operator = operator,
            username = username,
            password = password,
            isActive = isActive
        )
    }

    fun clearCredentials(operator: Operator) {
        sharedPreferences.edit().apply {
            remove("${operator.name}_username")
            remove("${operator.name}_password")
            remove("${operator.name}_active")
        }.apply()
    }
}