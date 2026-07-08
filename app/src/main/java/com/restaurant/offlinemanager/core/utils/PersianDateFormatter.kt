package com.restaurant.offlinemanager.core.utils

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

object PersianDateFormatter {
    private val persianMonths = listOf(
        "فروردین",
        "اردیبهشت",
        "خرداد",
        "تیر",
        "مرداد",
        "شهریور",
        "مهر",
        "آبان",
        "آذر",
        "دی",
        "بهمن",
        "اسفند"
    )

    fun nowMillis(): Long = System.currentTimeMillis()

    fun todayStartMillis(): Long {
        val zone = ZoneId.systemDefault()
        return LocalDate.now(zone).atStartOfDay(zone).toInstant().toEpochMilli()
    }

    fun addDays(days: Long): Long {
        val zone = ZoneId.systemDefault()
        return LocalDate.now(zone).plusDays(days).atStartOfDay(zone).toInstant().toEpochMilli()
    }

    fun shiftDays(timestamp: Long, days: Long): Long {
        val zone = ZoneId.systemDefault()
        return Instant.ofEpochMilli(timestamp)
            .atZone(zone)
            .toLocalDate()
            .plusDays(days)
            .atStartOfDay(zone)
            .toInstant()
            .toEpochMilli()
    }

    fun format(timestamp: Long): String {
        val date = Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault()).toLocalDate()
        val jalali = gregorianToJalali(date.year, date.monthValue, date.dayOfMonth)
        return NumberFormatter.toPersianDigits("${jalali.year}/${jalali.month.toString().padStart(2, '0')}/${jalali.day.toString().padStart(2, '0')}")
    }

    fun formatLong(timestamp: Long): String {
        val date = Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault()).toLocalDate()
        val jalali = gregorianToJalali(date.year, date.monthValue, date.dayOfMonth)
        return NumberFormatter.toPersianDigits("${jalali.day} ${persianMonths[jalali.month - 1]} ${jalali.year}")
    }

    fun monthKey(timestamp: Long): String {
        val date = Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault()).toLocalDate()
        val jalali = gregorianToJalali(date.year, date.monthValue, date.dayOfMonth)
        return "${jalali.year}-${jalali.month.toString().padStart(2, '0')}"
    }

    private fun gregorianToJalali(gy: Int, gm: Int, gd: Int): JalaliDate {
        val gDaysInMonth = intArrayOf(31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
        val jDaysInMonth = intArrayOf(31, 31, 31, 31, 31, 31, 30, 30, 30, 30, 30, 29)
        var gy2 = gy - 1600
        val gm2 = gm - 1
        val gd2 = gd - 1

        var gDayNo = 365 * gy2 + (gy2 + 3) / 4 - (gy2 + 99) / 100 + (gy2 + 399) / 400
        for (i in 0 until gm2) gDayNo += gDaysInMonth[i]
        if (gm2 > 1 && isGregorianLeap(gy)) gDayNo++
        gDayNo += gd2

        var jDayNo = gDayNo - 79
        val jNp = jDayNo / 12053
        jDayNo %= 12053
        var jy = 979 + 33 * jNp + 4 * (jDayNo / 1461)
        jDayNo %= 1461
        if (jDayNo >= 366) {
            jy += (jDayNo - 1) / 365
            jDayNo = (jDayNo - 1) % 365
        }
        var jm = 0
        while (jm < 11 && jDayNo >= jDaysInMonth[jm]) {
            jDayNo -= jDaysInMonth[jm]
            jm++
        }
        return JalaliDate(jy, jm + 1, jDayNo + 1)
    }

    private fun isGregorianLeap(year: Int): Boolean =
        year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)

    private data class JalaliDate(val year: Int, val month: Int, val day: Int)
}
