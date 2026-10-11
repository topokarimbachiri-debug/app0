package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.data.local.dao.ContactDao
import com.example.data.local.dao.FinanceDao
import com.example.data.local.dao.ProductDao
import com.example.data.local.dao.SaleDao
import com.example.data.local.dao.SettingsDao
import com.example.data.local.entity.AccountTransfer
import com.example.data.local.entity.AuditLog
import com.example.data.local.entity.BusinessSettings
import com.example.data.local.entity.Customer
import com.example.data.local.entity.CustomerPayment
import com.example.data.local.entity.DailyCashClosing
import com.example.data.local.entity.Expense
import com.example.data.local.entity.FinancialAccount
import com.example.data.local.entity.Product
import com.example.data.local.entity.ProductCategory
import com.example.data.local.entity.Purchase
import com.example.data.local.entity.PurchaseItem
import com.example.data.local.entity.Sale
import com.example.data.local.entity.SaleItem
import com.example.data.local.entity.SalesReturn
import com.example.data.local.entity.StockMovement
import com.example.data.local.entity.Supplier
import com.example.data.local.entity.SupplierPayment
import com.example.data.local.entity.Warehouse

@Database(
    entities = [
        Product::class,
        ProductCategory::class,
        Warehouse::class,
        StockMovement::class,
        Sale::class,
        SaleItem::class,
        SalesReturn::class,
        Purchase::class,
        PurchaseItem::class,
        FinancialAccount::class,
        AccountTransfer::class,
        Expense::class,
        CustomerPayment::class,
        SupplierPayment::class,
        DailyCashClosing::class,
        Customer::class,
        Supplier::class,
        AuditLog::class,
        BusinessSettings::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(AppTypeConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun saleDao(): SaleDao
    abstract fun financeDao(): FinanceDao
    abstract fun contactDao(): ContactDao
    abstract fun settingsDao(): SettingsDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "smartbiz_database.db"
                )
                    .fallbackToDestructiveMigration(false)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
