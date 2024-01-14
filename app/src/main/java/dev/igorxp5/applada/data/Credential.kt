package dev.igorxp5.applada.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import dev.igorxp5.applada.data.converters.DateConverter
import java.util.Date

@Entity(tableName = "credentials")
@TypeConverters(DateConverter::class)
data class Credential(
    @PrimaryKey
    @ColumnInfo(name = "username")
    val username: String,

    @ColumnInfo(name = "access_token")
    val accessToken: String,

    @ColumnInfo(name = "refresh_token")
    val refreshToken: String,

    @ColumnInfo(name = "created_date")
    val createdDate: Date = Date(),

    @ColumnInfo(name = "updated_date")
    val updatedDate: Date = Date(),

    @ColumnInfo(name = "active")
    val active: Boolean = true
)
