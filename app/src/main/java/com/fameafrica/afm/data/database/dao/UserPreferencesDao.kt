package com.fameafrica.afm.data.database.dao

import androidx.room.*
import com.fameafrica.afm.data.database.entities.UserPreferencesEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserPreferencesDao {

    // ============ BASIC CRUD ============

    @Query("SELECT * FROM user_preferences ORDER BY preference_key")
    fun getAll(): Flow<List<UserPreferencesEntity>>

    @Query("SELECT * FROM user_preferences WHERE id = :id")
    suspend fun getById(id: Int): UserPreferencesEntity?

    @Query("SELECT * FROM user_preferences WHERE preference_key = :key")
    suspend fun getByKey(key: String): UserPreferencesEntity?

    @Query("SELECT preference_value FROM user_preferences WHERE preference_key = :key")
    suspend fun getPreferenceValue(key: String): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(preference: UserPreferencesEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(preferences: List<UserPreferencesEntity>)

    @Update
    suspend fun update(preference: UserPreferencesEntity)

    @Delete
    suspend fun delete(preference: UserPreferencesEntity)

    @Query("DELETE FROM user_preferences WHERE preference_key = :key")
    suspend fun deleteByKey(key: String)

    @Query("DELETE FROM user_preferences")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM user_preferences")
    suspend fun getCount(): Int

    @Query("SELECT * FROM user_preferences")
    suspend fun getAllStatic(): List<UserPreferencesEntity>

    // ============ TYPED GETTERS ============

    @Query("SELECT preference_value FROM user_preferences WHERE preference_key = :key")
    suspend fun getString(key: String): String?

    @Transaction
    suspend fun getBoolean(key: String): Boolean? {
        val value = getPreferenceValue(key) ?: return null
        return value.toBooleanStrictOrNull()
    }

    @Transaction
    suspend fun getInt(key: String): Int? {
        val value = getPreferenceValue(key) ?: return null
        return value.toIntOrNull()
    }

    @Transaction
    suspend fun getFloat(key: String): Float? {
        val value = getPreferenceValue(key) ?: return null
        return value.toFloatOrNull()
    }

    @Transaction
    suspend fun getLong(key: String): Long? {
        val value = getPreferenceValue(key) ?: return null
        return value.toLongOrNull()
    }

    // ============ TYPED SETTERS ============

    @Transaction
    suspend fun setString(key: String, value: String) {
        val existing = getByKey(key)
        if (existing != null) {
            update(existing.copy(preferenceValue = value, preferenceType = "string"))
        } else {
            insert(UserPreferencesEntity(
                preferenceKey = key,
                preferenceValue = value,
                preferenceType = "string"
            ))
        }
    }

    @Transaction
    suspend fun setBoolean(key: String, value: Boolean) {
        val existing = getByKey(key)
        val stringValue = value.toString()
        if (existing != null) {
            update(existing.copy(preferenceValue = stringValue, preferenceType = "boolean"))
        } else {
            insert(UserPreferencesEntity(
                preferenceKey = key,
                preferenceValue = stringValue,
                preferenceType = "boolean"
            ))
        }
    }

    @Transaction
    suspend fun setInt(key: String, value: Int) {
        val existing = getByKey(key)
        val stringValue = value.toString()
        if (existing != null) {
            update(existing.copy(preferenceValue = stringValue, preferenceType = "integer"))
        } else {
            insert(UserPreferencesEntity(
                preferenceKey = key,
                preferenceValue = stringValue,
                preferenceType = "integer"
            ))
        }
    }

    @Transaction
    suspend fun setFloat(key: String, value: Float) {
        val existing = getByKey(key)
        val stringValue = value.toString()
        if (existing != null) {
            update(existing.copy(preferenceValue = stringValue, preferenceType = "float"))
        } else {
            insert(UserPreferencesEntity(
                preferenceKey = key,
                preferenceValue = stringValue,
                preferenceType = "float"
            ))
        }
    }

    @Transaction
    suspend fun setLong(key: String, value: Long) {
        val existing = getByKey(key)
        val stringValue = value.toString()
        if (existing != null) {
            update(existing.copy(preferenceValue = stringValue, preferenceType = "integer"))
        } else {
            insert(UserPreferencesEntity(
                preferenceKey = key,
                preferenceValue = stringValue,
                preferenceType = "integer"
            ))
        }
    }

    // ============ TYPE-BASED QUERIES ============

    @Query("SELECT * FROM user_preferences WHERE preference_type = :type")
    fun getByType(type: String): Flow<List<UserPreferencesEntity>>

    @Query("SELECT preference_key FROM user_preferences WHERE preference_type = :type")
    suspend fun getKeysByType(type: String): List<String>

    // ============ BULK OPERATIONS ============

    @Transaction
    suspend fun setPreferences(preferences: Map<String, Any>) {
        preferences.forEach { (key, value) ->
            when (value) {
                is String -> setString(key, value)
                is Boolean -> setBoolean(key, value)
                is Int -> setInt(key, value)
                is Float -> setFloat(key, value)
                is Long -> setLong(key, value)
            }
        }
    }

    @Query("SELECT preference_key, preference_value FROM user_preferences")
    suspend fun getAllAsList(): List<PreferenceKeyValue>

    suspend fun getAllAsMap(): Map<String, String> {
        return getAllAsList().associate { it.key to it.value }
    }
}

// ============ DATA CLASSES ============

data class PreferenceKeyValue(
    @ColumnInfo(name = "preference_key")
    val key: String,
    @ColumnInfo(name = "preference_value")
    val value: String
)

// ============ EXTENSION FUNCTIONS ============

fun String.toBooleanStrictOrNull(): Boolean? {
    return when (lowercase()) {
        "true" -> true
        "false" -> false
        else -> null
    }
}
