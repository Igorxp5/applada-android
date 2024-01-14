package dev.igorxp5.applada.data.converters

import androidx.room.TypeConverter
import dev.igorxp5.applada.data.MatchCategory


class MatchCategoryConverter {
    @TypeConverter
    fun fromString(value: String): MatchCategory {
        return MatchCategory.valueOf(value)
    }

    @TypeConverter
    fun toString(category: MatchCategory): String {
        return category.name
    }
}
