package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class PaymentStatus {
    PAID,
    PARTIALLY_PAID,
    UNPAID,
    OVERDUE,
    REFUNDED
}

enum class PaymentMethod {
    CASH,
    BANK_TRANSFER,
    CARD,
    CREDIT,
    SPLIT
}

@Entity(tableName = "sales")
data class Sale(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val invoiceNumber: String,
    val customerId: Long? = null,
    val customerName: String = "Walk-in Customer",
    val subtotalCents: Long,
    val discountCents: Long = 0,
    val taxCents: Long = 0,
    val totalCents: Long,
    val paidAmountCents: Long,
    val paymentStatus: PaymentStatus,
    val paymentMethod: PaymentMethod = PaymentMethod.CASH,
    val financialAccountId: Long? = 1,
    val cogsCents: Long = 0, // Cost of Goods Sold
    val warehouseId: Long = 1,
    val notes: String = "",
    val saleDate: Long = System.currentTimeMillis(),
    val dueDate: Long = System.currentTimeMillis(),
    val isReturned: Boolean = false
)

@Entity(tableName = "sale_items")
data class SaleItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val saleId: Long,
    val productId: Long,
    val productName: String,
    val sku: String = "",
    val quantity: Double,
    val unitPriceCents: Long,
    val costPriceCents: Long = 0,
    val discountCents: Long = 0,
    val totalCents: Long
)

@Entity(tableName = "sales_returns")
data class SalesReturn(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val saleId: Long,
    val returnNumber: String,
    val invoiceNumber: String,
    val reason: String,
    val refundAmountCents: Long,
    val isRestocked: Boolean = true,
    val returnDate: Long = System.currentTimeMillis(),
    val financialAccountId: Long? = 1
)

@Entity(tableName = "purchases")
data class Purchase(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val purchaseNumber: String,
    val supplierId: Long? = null,
    val supplierName: String = "Default Supplier",
    val subtotalCents: Long,
    val taxCents: Long = 0,
    val totalCents: Long,
    val paidAmountCents: Long,
    val paymentStatus: PaymentStatus,
    val paymentMethod: PaymentMethod = PaymentMethod.BANK_TRANSFER,
    val financialAccountId: Long? = 1,
    val warehouseId: Long = 1,
    val purchaseDate: Long = System.currentTimeMillis(),
    val dueDate: Long = System.currentTimeMillis(),
    val notes: String = ""
)

@Entity(tableName = "purchase_items")
data class PurchaseItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val purchaseId: Long,
    val productId: Long,
    val productName: String,
    val quantity: Double,
    val unitCostCents: Long,
    val totalCents: Long
)
