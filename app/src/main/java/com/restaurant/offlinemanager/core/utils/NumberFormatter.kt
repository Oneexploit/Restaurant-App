package com.restaurant.offlinemanager.core.utils

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

object NumberFormatter {
    private val englishFormatter = DecimalFormat("#,###", DecimalFormatSymbols(Locale.US))
    private val persianDigits = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')

    fun format(value: Long): String = toPersianDigits(englishFormatter.format(value))

    fun format(value: Int): String = format(value.toLong())

    fun format(value: Double, fractionDigits: Int = 1): String {
        val pattern = if (fractionDigits <= 0) "#,###" else "#,###.${"#".repeat(fractionDigits)}"
        val formatted = DecimalFormat(pattern, DecimalFormatSymbols(Locale.US)).format(value)
        return toPersianDigits(formatted)
    }

    fun toPersianDigits(input: String): String = buildString(input.length) {
        input.forEach { char ->
            append(if (char in '0'..'9') persianDigits[char - '0'] else char)
        }
    }

    fun normalizeDigits(input: String): String {
        val arabicDigits = charArrayOf('٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩')
        return buildString(input.length) {
            input.forEach { char ->
                val persianIndex = persianDigits.indexOf(char)
                val arabicIndex = arabicDigits.indexOf(char)
                when {
                    persianIndex >= 0 -> append('0' + persianIndex)
                    arabicIndex >= 0 -> append('0' + arabicIndex)
                    char == ',' || char == '٬' || char == ' ' -> Unit
                    else -> append(char)
                }
            }
        }
    }

    /** Formats a positive integer while it is being typed, without converting it to a fixed-size number. */
    fun formatMoneyInput(input: String): String {
        val digits = normalizeDigits(input).filter(Char::isDigit)
        if (digits.isEmpty()) return ""
        val normalized = digits.trimStart('0').ifEmpty { "0" }
        val grouped = normalized
            .reversed()
            .chunked(3)
            .joinToString(",")
            .reversed()
        return toPersianDigits(grouped)
    }
}
