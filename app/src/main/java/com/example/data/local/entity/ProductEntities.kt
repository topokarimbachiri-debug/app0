package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class Product(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val sku: String,
    val barcode: String,
    val categoryId: Long,
    val description: String = "",
    val costPriceCents: Long, // Purchase cost in cents
    val sellingPriceCents: Long, // Retail price in cents
    val taxRatePercent: Double = 0.0,
    val unit: String = "pcs", // "pcs", "box", "kg", "liter", "meter"
    val minStockLevel: Double = 5.0,
    val currentStock: Double = 0.0,
    val warehouseId: Long = 1,
    val imageUrl: String? = null,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "product_categories")
data class ProductCategory(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val colorHex: String = "#10B981",
    val iconName: String = "Category"
)

@Entity(tableName = "warehouses")
data class Warehouse(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val location: String = "",
    val isDefault: Boolean = false
)

enum class StockMovementType {
    PURCHASE,
    SALE,
    SALE_RETURN,
    PURCHASE_RETURN,
    ADJUSTMENT_ADD,
    ADJUSTMENT_SUB,
    TRANSFER_IN,
    TRANSFER_OUT,
    DAMAGED,
    PHYSICAL_COUNT
}

@Entity(tableName = "stock_movements")
data class StockMovement(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val productId: Long,
    val warehouseId: Long,
    val movementType: StockMovementType,
    val quantity: Double, // positive or negative change
    val unitCostCents: Long,
    val referenceNumber: String, // e.g. "INV-1001", "PO-2001", "ADJ-01"
    val notes: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val performedBy: String = "Administrator"
)
