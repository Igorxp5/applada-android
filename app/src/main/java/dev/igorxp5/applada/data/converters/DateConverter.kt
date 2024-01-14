package dev.igorxp5.applada.data.converters

import androidx.room.TypeConverter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DateConverter {
    val dateFormat =  SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.getDefault(Locale.Category.FORMAT))

    @TypeConverter
    fun fromDate(value: Date?): String? {
        return value?.let { dateFormat.format(it) }
    }

    @TypeConverter
    fun toDate(date: String): Date? {
        return dateFormat.parse(date)
    }
}
