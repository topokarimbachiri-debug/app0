package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.Product
import com.example.data.local.entity.ProductCategory
import com.example.data.local.entity.StockMovement
import com.example.data.local.entity.Warehouse
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Query("SELECT * FROM products ORDER BY name ASC")
    fun getAllProducts(): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE isActive = 1 ORDER BY name ASC")
    fun getActiveProducts(): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE id = :id")
    fun getProductById(id: Long): Flow<Product?>

    @Query("SELECT * FROM products WHERE id = :id LIMIT 1")
    suspend fun getProductByIdDirect(id: Long): Product?

    @Query("SELECT * FROM products WHERE barcode = :barcode OR sku = :barcode LIMIT 1")
    suspend fun getProductByBarcode(barcode: String): Product?

    @Query("SELECT * FROM products WHERE currentStock <= minStockLevel AND isActive = 1")
    fun getLowStockProducts(): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE name LIKE '%' || :query || '%' OR sku LIKE '%' || :query || '%' OR barcode LIKE '%' || :query || '%'")
    fun searchProducts(query: String): Flow<List<Product>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: Product): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(products: List<Product>)

    @Update
    suspend fun updateProduct(product: Product)

    @Query("UPDATE products SET currentStock = :newStock WHERE id = :productId")
    suspend fun updateStock(productId: Long, newStock: Double)

    @Delete
    suspend fun deleteProduct(product: Product)

    @Query("SELECT * FROM product_categories ORDER BY name ASC")
    fun getCategories(): Flow<List<ProductCategory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: ProductCategory): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<ProductCategory>)

    @Query("SELECT * FROM warehouses ORDER BY id ASC")
    fun getWarehouses(): Flow<List<Warehouse>>

    @Query("SELECT * FROM warehouses WHERE id = :id LIMIT 1")
    suspend fun getWarehouseById(id: Long): Warehouse?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWarehouse(warehouse: Warehouse): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWarehouses(warehouses: List<Warehouse>)

    @Query("SELECT * FROM stock_movements WHERE productId = :productId ORDER BY timestamp DESC")
    fun getStockMovementsByProduct(productId: Long): Flow<List<StockMovement>>

    @Query("SELECT * FROM stock_movements ORDER BY timestamp DESC")
    fun getAllStockMovements(): Flow<List<StockMovement>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStockMovement(movement: StockMovement): Long
}
