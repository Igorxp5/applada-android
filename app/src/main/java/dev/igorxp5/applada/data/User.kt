package dev.igorxp5.applada.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import dev.igorxp5.applada.data.roomconverters.DateConverter
import java.util.Date


@Entity(tableName = "users")
@TypeConverters(DateConverter::class)
data class User(
    @PrimaryKey
    @ColumnInfo("username")
    val username: String,

    @ColumnInfo("name")
    val name: String,

    @ColumnInfo("email")
    val email: String,

    @ColumnInfo("level")
    val level: Int,

    @ColumnInfo("registered_date")
    val registeredDate: Date
)
