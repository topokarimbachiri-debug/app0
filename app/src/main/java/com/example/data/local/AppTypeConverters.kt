package com.example.data.local

import androidx.room.TypeConverter
import com.example.data.local.entity.AccountType
import com.example.data.local.entity.ExpenseCategory
import com.example.data.local.entity.PaymentMethod
import com.example.data.local.entity.PaymentStatus
import com.example.data.local.entity.StockMovementType
import com.example.data.local.entity.UserRole

class AppTypeConverters {
    @TypeConverter
    fun fromStockMovementType(value: StockMovementType?): String? = value?.name

    @TypeConverter
    fun toStockMovementType(value: String?): StockMovementType? =
        value?.let { runCatching { StockMovementType.valueOf(it) }.getOrDefault(StockMovementType.ADJUSTMENT_ADD) }

    @TypeConverter
    fun fromPaymentStatus(value: PaymentStatus?): String? = value?.name

    @TypeConverter
    fun toPaymentStatus(value: String?): PaymentStatus? =
        value?.let { runCatching { PaymentStatus.valueOf(it) }.getOrDefault(PaymentStatus.PAID) }

    @TypeConverter
    fun fromPaymentMethod(value: PaymentMethod?): String? = value?.name

    @TypeConverter
    fun toPaymentMethod(value: String?): PaymentMethod? =
        value?.let { runCatching { PaymentMethod.valueOf(it) }.getOrDefault(PaymentMethod.CASH) }

    @TypeConverter
    fun fromAccountType(value: AccountType?): String? = value?.name

    @TypeConverter
    fun toAccountType(value: String?): AccountType? =
        value?.let { runCatching { AccountType.valueOf(it) }.getOrDefault(AccountType.CASH) }

    @TypeConverter
    fun fromExpenseCategory(value: ExpenseCategory?): String? = value?.name

    @TypeConverter
    fun toExpenseCategory(value: String?): ExpenseCategory? =
        value?.let { runCatching { ExpenseCategory.valueOf(it) }.getOrDefault(ExpenseCategory.OTHER) }

    @TypeConverter
    fun fromUserRole(value: UserRole?): String? = value?.name

    @TypeConverter
    fun toUserRole(value: String?): UserRole? =
        value?.let { runCatching { UserRole.valueOf(it) }.getOrDefault(UserRole.ADMINISTRATOR) }
}
