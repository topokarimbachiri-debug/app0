package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "customers")
data class Customer(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val phone: String = "",
    val email: String = "",
    val address: String = "",
    val creditLimitCents: Long = 100000, // e.g. $1000.00
    val currentBalanceCents: Long = 0, // Positive means customer owes business (Receivable)
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "suppliers")
data class Supplier(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val phone: String = "",
    val email: String = "",
    val address: String = "",
    val currentBalanceCents: Long = 0, // Positive means business owes supplier (Payable)
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
