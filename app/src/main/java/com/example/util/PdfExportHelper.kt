package com.example.util

import android.content.Context
import android.content.Intent
import com.example.data.local.entity.BusinessSettings
import com.example.data.local.entity.Customer
import com.example.data.local.entity.Sale
import com.example.data.local.entity.SaleItem

object PdfExportHelper {
    fun generateInvoiceText(
        sale: Sale,
        items: List<SaleItem>,
        settings: BusinessSettings,
        customer: Customer?
    ): String {
        val sym = settings.currencySymbol
        val dec = settings.currencyDecimals
        val sb = StringBuilder()
        sb.appendLine("==========================================")
        sb.appendLine("            ${settings.businessName.uppercase()}")
        sb.appendLine("==========================================")
        sb.appendLine("Phone: ${settings.phone} | Tax ID: ${settings.taxNumber}")
        sb.appendLine("Address: ${settings.address}")
        sb.appendLine("------------------------------------------")
        sb.appendLine("INVOICE: ${sale.invoiceNumber}")
        sb.appendLine("Date: ${DateUtils.formatDateTime(sale.saleDate)}")
        sb.appendLine("Customer: ${customer?.name ?: sale.customerName}")
        if (customer?.phone?.isNotBlank() == true) {
            sb.appendLine("Contact: ${customer.phone}")
        }
        sb.appendLine("Payment Status: ${sale.paymentStatus}")
        sb.appendLine("Payment Method: ${sale.paymentMethod}")
        sb.appendLine("------------------------------------------")
        sb.appendLine(String.format("%-18s %5s %8s %8s", "Item", "Qty", "Price", "Total"))
        sb.appendLine("------------------------------------------")
        items.forEach { item ->
            val name = if (item.productName.length > 17) item.productName.take(15) + ".." else item.productName
            val priceStr = CurrencyUtils.formatCents(item.unitPriceCents, sym, dec)
            val totalStr = CurrencyUtils.formatCents(item.totalCents, sym, dec)
            sb.appendLine(String.format("%-18s %5.1f %8s %8s", name, item.quantity, priceStr, totalStr))
        }
        sb.appendLine("------------------------------------------")
        sb.appendLine("Subtotal:  ${CurrencyUtils.formatCents(sale.subtotalCents, sym, dec)}")
        if (sale.discountCents > 0) {
            sb.appendLine("Discount: -${CurrencyUtils.formatCents(sale.discountCents, sym, dec)}")
        }
        if (sale.taxCents > 0) {
            sb.appendLine("Tax (${settings.taxRatePercent}%):  +${CurrencyUtils.formatCents(sale.taxCents, sym, dec)}")
        }
        sb.appendLine("TOTAL:     ${CurrencyUtils.formatCents(sale.totalCents, sym, dec)}")
        sb.appendLine("Paid:      ${CurrencyUtils.formatCents(sale.paidAmountCents, sym, dec)}")
        val balanceDue = sale.totalCents - sale.paidAmountCents
        if (balanceDue > 0) {
            sb.appendLine("BALANCE DUE: ${CurrencyUtils.formatCents(balanceDue, sym, dec)}")
        }
        sb.appendLine("==========================================")
        if (settings.invoiceNotes.isNotBlank()) {
            sb.appendLine(settings.invoiceNotes)
        }
        sb.appendLine("Generated with SmartBiz Management")
        return sb.toString()
    }

    fun shareText(context: Context, title: String, text: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, title)
            putExtra(Intent.EXTRA_TEXT, text)
        }
        val chooser = Intent.createChooser(intent, title)
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }
}
