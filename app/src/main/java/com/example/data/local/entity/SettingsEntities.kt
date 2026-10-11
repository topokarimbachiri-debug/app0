package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "audit_logs")
data class AuditLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val action: String, // "CREATE_SALE", "CANCEL_SALE", "STOCK_ADJUSTMENT", "PRICE_CHANGE", etc.
    val entityType: String,
    val entityId: String,
    val details: String,
    val performedBy: String = "Admin",
    val timestamp: Long = System.currentTimeMillis()
)

enum class UserRole {
    ADMINISTRATOR,
    MANAGER,
    CASHIER
}

@Entity(tableName = "business_settings")
data class BusinessSettings(
    @PrimaryKey val id: Int = 1,
    val businessName: String = "SmartBiz Commercial",
    val phone: String = "+1 (555) 019-2834",
    val email: String = "contact@smartbiz.com",
    val address: String = "100 Commercial Avenue, Suite 400",
    val taxNumber: String = "TAX-88492019",
    val currencyCode: String = "USD", // "USD", "EUR", "MAD", "SAR", etc.
    val currencySymbol: String = "$", // "$", "€", "MAD", "SR", etc.
    val currencyDecimals: Int = 2,
    val taxRatePercent: Double = 5.0,
    val invoicePrefix: String = "INV-",
    val invoiceNotes: String = "Thank you for your business! Payment due within 30 days.",
    val allowNegativeStock: Boolean = false,
    val language: String = "en", // "en", "fr", "ar"
    val securityPin: String = "", // empty means no pin lock
    val requirePinOnStart: Boolean = false,
    val activeRole: UserRole = UserRole.ADMINISTRATOR,
    val isDemoLoaded: Boolean = false
)
