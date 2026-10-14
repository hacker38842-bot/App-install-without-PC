package com.example

import com.example.util.AdminAuthUtil
import com.example.util.AppLanguage
import com.example.util.CurrencyFormatter
import com.example.util.DateUtils
import com.example.util.Localization
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ArenaXUnitTest {

    @Test
    fun testCurrencyFormatter_paiseToInr() {
        val resultZero = CurrencyFormatter.formatPaise(0L)
        assertTrue(resultZero.contains("0"))

        val resultFifty = CurrencyFormatter.formatPaise(5000L)
        assertTrue(resultFifty.contains("50"))

        val resultFractional = CurrencyFormatter.formatPaise(5050L)
        assertTrue(resultFractional.contains("50.50"))
    }

    @Test
    fun testCurrencyFormatter_inrToPaise() {
        assertEquals(5000L, CurrencyFormatter.inrToPaise("50"))
        assertEquals(5000L, CurrencyFormatter.inrToPaise("₹50"))
        assertEquals(10000L, CurrencyFormatter.inrToPaise("₹100.00"))
        assertEquals(2550L, CurrencyFormatter.inrToPaise("25.50"))
    }

    @Test
    fun testLocalization_strings() {
        assertEquals("ARENA X", Localization.getString("app_title", AppLanguage.ENGLISH))
        assertEquals("অ্যারেনা এক্স", Localization.getString("app_title", AppLanguage.BENGALI))
        assertEquals("Tournaments", Localization.getString("tournaments", AppLanguage.ENGLISH))
        assertEquals("টুর্নামেন্ট", Localization.getString("tournaments", AppLanguage.BENGALI))
    }

    @Test
    fun testDateUtils_countdown() {
        val futureTime = System.currentTimeMillis() + (3600 * 1000L) // 1 hour
        val countdown = DateUtils.formatCountdown(futureTime)
        assertTrue(countdown.contains("h") || countdown.contains("m"))

        val pastTime = System.currentTimeMillis() - 1000L
        val liveCountdown = DateUtils.formatCountdown(pastTime)
        assertEquals("Started / Live", liveCountdown)
    }

    @Test
    fun testAdminAuthUtil_usernameNormalization() {
        val formattedUnicode = "𝑴𝒖𝒔𝒌𝒂𝒏 𝑷𝒂𝒏𝒆𝒍 𝑺𝒕𝒐𝒓𝒆"
        val plainText = "Muskan Panel Store"

        val normFormatted = AdminAuthUtil.normalizeUsername(formattedUnicode)
        val normPlain = AdminAuthUtil.normalizeUsername(plainText)

        assertEquals("muskan panel store", normFormatted)
        assertEquals(normPlain, normFormatted)
    }

    @Test
    fun testAdminAuthUtil_pbkdf2HashAndVerify() {
        val salt = AdminAuthUtil.generateSalt()
        val saltHex = AdminAuthUtil.bytesToHex(salt)
        val password = "MuskanBot@2026"

        val hashHex = AdminAuthUtil.hashPassword(password, salt)
        assertTrue(hashHex.isNotBlank())
        assertEquals(64, hashHex.length) // 256 bits = 32 bytes = 64 hex chars

        // Positive verification
        val isCorrect = AdminAuthUtil.verifyPassword("MuskanBot@2026", saltHex, hashHex)
        assertTrue(isCorrect)

        // Negative verification with incorrect password
        val isWrong = AdminAuthUtil.verifyPassword("WrongPassword123", saltHex, hashHex)
        assertFalse(isWrong)
    }

    @Test
    fun testAdminAuthUtil_saltRandomness() {
        val salt1 = AdminAuthUtil.generateSalt()
        val salt2 = AdminAuthUtil.generateSalt()
        assertFalse(salt1.contentEquals(salt2))
    }
}
