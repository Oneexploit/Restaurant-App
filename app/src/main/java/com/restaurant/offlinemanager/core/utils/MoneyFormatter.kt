package com.restaurant.offlinemanager.core.utils

object MoneyFormatter {
    const val CURRENCY = "تومان"

    fun format(amount: Long): String = "${NumberFormatter.format(amount)} $CURRENCY"

    fun parse(input: String): Long =
        NumberFormatter.normalizeDigits(input).filter { it.isDigit() }.toLongOrNull() ?: 0L
}
