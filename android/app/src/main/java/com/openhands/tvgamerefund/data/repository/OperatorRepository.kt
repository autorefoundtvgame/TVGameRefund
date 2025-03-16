package com.openhands.tvgamerefund.data.repository

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.google.gson.Gson
import com.openhands.tvgamerefund.data.models.Operator
import com.openhands.tvgamerefund.data.models.OperatorCredentials
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OperatorRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
    private val gson = Gson()

    private val sharedPreferences = EncryptedSharedPreferences.create(
        "operator_credentials",
        masterKeyAlias,
        context,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun saveCredentials(credentials: OperatorCredentials) {
        sharedPreferences.edit().apply {
            putString(credentials.operator.name, gson.toJson(credentials))
            apply()
        }
    }

    fun getCredentials(operator: Operator): OperatorCredentials? {
        val json = sharedPreferences.getString(operator.name, null)
        return json?.let { gson.fromJson(it, OperatorCredentials::class.java) }
    }

    fun getAllCredentials(): List<OperatorCredentials> {
        return Operator.values().mapNotNull { getCredentials(it) }
    }

    fun deleteCredentials(operator: Operator) {
        sharedPreferences.edit().apply {
            remove(operator.name)
            apply()
        }
    }
}