package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.local.DemoDataLoader
import com.example.data.local.entity.AccountType
import com.example.data.repository.BusinessRepository
import com.example.util.CurrencyUtils
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun testAppNameString() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("SmartBiz", appName)
    }

    @Test
    fun testCurrencyUtilsPrecision() {
        // Cents parsing without floating-point inaccuracies
        val cents1 = CurrencyUtils.parseToCents("1250.75", 2)
        assertEquals(125075L, cents1)

        val formatted = CurrencyUtils.formatCents(125075L, "$", 2)
        assertEquals("$1,250.75", formatted)

        val zeroParsed = CurrencyUtils.parseToCents("", 2)
        assertEquals(0L, zeroParsed)
    }

    @Test
    fun testDemoDataAndFinancialMetrics() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = AppDatabase.getDatabase(context)
        DemoDataLoader.populateDemoData(db)
        val repository = BusinessRepository(db)

        val accounts = repository.allAccounts.first()
        val availableLiquidity = accounts.filter { !it.isRestricted }.sumOf { it.balanceCents }
        val restrictedReserve = accounts.filter { it.isRestricted }.sumOf { it.balanceCents }

        // Unrestricted liquidity must exclude restricted accounts ($5,000.00 = 500,000 cents)
        assertEquals(500000L, restrictedReserve)
        assertTrue(availableLiquidity > 0L)

        val products = repository.allProducts.first()
        assertTrue(products.isNotEmpty())

        val sales = repository.allSales.first()
        assertTrue(sales.isNotEmpty())

        val customers = repository.allCustomers.first()
        assertTrue(customers.isNotEmpty())
    }
}
