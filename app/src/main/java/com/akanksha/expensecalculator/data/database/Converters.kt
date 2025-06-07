package com.akanksha.expensecalculator.data.database

import androidx.room.TypeConverter
import com.akanksha.expensecalculator.data.models.RecurringType
import java.util.Date

class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

    @TypeConverter
    fun fromString(value: String?): List<String> {
        return value?.split(",")?.filter { it.isNotEmpty() } ?: emptyList()
    }

    @TypeConverter
    fun toString(list: List<String>): String {
        return list.joinToString(",")
    }

    @TypeConverter
    fun toRecurringType(value: String?): RecurringType? {
        return value?.let { enumValueOf<RecurringType>(it) }
    }

    @TypeConverter
    fun fromRecurringType(value: RecurringType?): String? {
        return value?.name
    }
} 