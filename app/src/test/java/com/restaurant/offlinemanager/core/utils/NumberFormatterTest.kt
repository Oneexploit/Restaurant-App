package com.restaurant.offlinemanager.core.utils

import org.junit.Assert.assertEquals
import org.junit.Test

class NumberFormatterTest {
    @Test
    fun moneyInputGroupsDigitsWhileTyping() {
        assertEquals("", NumberFormatter.formatMoneyInput(""))
        assertEquals("۱۲۳", NumberFormatter.formatMoneyInput("123"))
        assertEquals("۱,۲۳۴", NumberFormatter.formatMoneyInput("1234"))
        assertEquals("۱۲,۳۴۵,۶۷۸", NumberFormatter.formatMoneyInput("۱۲٬۳۴۵٬۶۷۸"))
    }

    @Test
    fun formattedMoneyRemainsParseable() {
        val formatted = NumberFormatter.formatMoneyInput("1,234,567")

        assertEquals(1_234_567L, MoneyFormatter.parse(formatted))
    }
}
