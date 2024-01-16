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
    @ColumnInfo("username")
    val username: String,

    @ColumnInfo("access_token")
    val accessToken: String,

    @ColumnInfo("refresh_token")
    val refreshToken: String,

    @ColumnInfo("created_date")
    val createdDate: Date = Date(),

    @ColumnInfo("updated_date")
    val updatedDate: Date = Date(),

    @ColumnInfo("active")
    val active: Boolean = true
)
