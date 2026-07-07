package com.restaurant.offlinemanager.core.utils

object Validation {
    fun required(value: String, fieldName: String): String? =
        if (value.isBlank()) "$fieldName الزامی است" else null

    fun positiveLong(value: Long, fieldName: String): String? =
        if (value <= 0) "$fieldName باید بیشتر از صفر باشد" else null

    fun positiveInt(value: Int, fieldName: String): String? =
        if (value <= 0) "$fieldName باید بیشتر از صفر باشد" else null

    fun positiveDouble(value: Double, fieldName: String): String? =
        if (value <= 0.0) "$fieldName باید بیشتر از صفر باشد" else null
}
