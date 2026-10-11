package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.AuditLog
import com.example.data.local.entity.BusinessSettings
import kotlinx.coroutines.flow.Flow

@Dao
interface SettingsDao {
    @Query("SELECT * FROM business_settings WHERE id = 1 LIMIT 1")
    fun getSettings(): Flow<BusinessSettings?>

    @Query("SELECT * FROM business_settings WHERE id = 1 LIMIT 1")
    suspend fun getSettingsDirect(): BusinessSettings?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateSettings(settings: BusinessSettings)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuditLog(log: AuditLog): Long

    @Query("SELECT * FROM audit_logs ORDER BY timestamp DESC")
    fun getAllAuditLogs(): Flow<List<AuditLog>>

    @Query("SELECT * FROM audit_logs ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentAuditLogs(limit: Int): Flow<List<AuditLog>>
}
