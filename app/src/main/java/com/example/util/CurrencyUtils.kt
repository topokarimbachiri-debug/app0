package com.example.util

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

object CurrencyUtils {
    fun formatCents(cents: Long, symbol: String = "$", decimals: Int = 2): String {
        val divisor = Math.pow(10.0, decimals.toDouble())
        val amount = cents.toDouble() / divisor

        val symbols = DecimalFormatSymbols(Locale.US)
        val pattern = if (decimals > 0) "#,##0." + "0".repeat(decimals) else "#,##0"
        val formatter = DecimalFormat(pattern, symbols)
        return "$symbol${formatter.format(amount)}"
    }

    fun parseToCents(input: String, decimals: Int = 2): Long {
        if (input.isBlank()) return 0L
        val clean = input.replace("[^0-9.]".toRegex(), "").trim()
        if (clean.isBlank()) return 0L

        return try {
            val parts = clean.split(".")
            val integerPart = parts[0].toLongOrNull() ?: 0L
            val fractionalPart = if (parts.size > 1) {
                parts[1].take(decimals).padEnd(decimals, '0').toLongOrNull() ?: 0L
            } else 0L

            val multiplier = Math.pow(10.0, decimals.toDouble()).toLong()
            (integerPart * multiplier) + fractionalPart
        } catch (e: Exception) {
            0L
        }
    }

    fun centsToDecimalString(cents: Long, decimals: Int = 2): String {
        val divisor = Math.pow(10.0, decimals.toDouble())
        val amount = cents.toDouble() / divisor
        return String.format(Locale.US, "%.${decimals}f", amount)
    }
}
