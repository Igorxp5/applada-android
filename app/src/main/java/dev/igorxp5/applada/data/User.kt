package dev.igorxp5.applada.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import dev.igorxp5.applada.data.converters.DateConverter
import java.util.Date


@Entity(tableName = "users")
@TypeConverters(DateConverter::class)
data class User(
    @PrimaryKey
    @ColumnInfo(name = "username")
    val username: String,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "email")
    val email: String,

    @ColumnInfo(name = "level")
    val level: Int,

    @ColumnInfo(name = "registered_date")
    val registeredDate: Date
)
