package com.openhands.tvgamerefund.data.database

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.openhands.tvgamerefund.data.models.GameType
import com.openhands.tvgamerefund.data.models.ReimbursementStatus
import java.util.Date

/**
 * Convertisseurs pour Room
 */
class Converters {
    private val gson = Gson()
    
    // Date <-> Long
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }
    
    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
    
    // List<String> <-> String
    @TypeConverter
    fun fromStringList(value: List<String>?): String {
        return gson.toJson(value ?: emptyList<String>())
    }
    
    @TypeConverter
    fun toStringList(value: String): List<String> {
        val listType = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(value, listType) ?: emptyList()
    }
    
    // GameType <-> String
    @TypeConverter
    fun fromGameType(value: GameType): String {
        return value.name
    }
    
    @TypeConverter
    fun toGameType(value: String): GameType {
        return try {
            GameType.valueOf(value)
        } catch (e: IllegalArgumentException) {
            GameType.OTHER
        }
    }
    
    // ReimbursementStatus <-> String
    @TypeConverter
    fun fromReimbursementStatus(value: ReimbursementStatus): String {
        return value.name
    }
    
    @TypeConverter
    fun toReimbursementStatus(value: String): ReimbursementStatus {
        return try {
            ReimbursementStatus.valueOf(value)
        } catch (e: IllegalArgumentException) {
            ReimbursementStatus.NOT_REQUESTED
        }
    }
}