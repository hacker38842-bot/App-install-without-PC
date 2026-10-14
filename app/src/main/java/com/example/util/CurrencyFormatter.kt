package com.example.util

import java.text.NumberFormat
import java.util.Locale

object CurrencyFormatter {
    /**
     * Converts paise (Long) to formatted INR string.
     * e.g., 5000L -> "₹50" or "₹50.50"
     */
    fun formatPaise(paise: Long, showDecimalsIfZero: Boolean = false): String {
        val rupees = paise / 100.0
        val inrLocale = Locale("en", "IN")
        val formatter = NumberFormat.getCurrencyInstance(inrLocale)
        formatter.maximumFractionDigits = 2
        formatter.minimumFractionDigits = if (showDecimalsIfZero || (paise % 100 != 0L)) 2 else 0
        val formatted = formatter.format(rupees)
        // Standardize Indian Rupee symbol prefix
        return formatted.replace("INR", "₹").trim()
    }

    /**
     * Parses user INR input to integer paise.
     */
    fun inrToPaise(inrText: String): Long {
        val cleaned = inrText.replace("₹", "").replace(",", "").trim()
        val inr = cleaned.toDoubleOrNull() ?: 0.0
        return (inr * 100).toLong()
    }
}
