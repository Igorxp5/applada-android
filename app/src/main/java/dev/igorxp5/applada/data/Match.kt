package dev.igorxp5.applada.data

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.google.gson.annotations.SerializedName
import dev.igorxp5.applada.data.MatchStatus
import dev.igorxp5.applada.data.converters.MatchCategoryConverter
import dev.igorxp5.applada.data.converters.DateConverter
import java.util.Date


@Entity(tableName = "matches")
@TypeConverters(MatchCategoryConverter::class, DateConverter::class)
data class Match(
    @PrimaryKey
    @SerializedName("_id")
    val id: String,

    @ColumnInfo("title")
    @SerializedName("title")
    val title: String,

    @ColumnInfo("description")
    @SerializedName("description")
    val description: String?,

    @ColumnInfo("limit_participants")
    @SerializedName("limit_participants")
    val limitParticipants: Int?,

    @Embedded
    @SerializedName("location")
    val location: Location,

    @ColumnInfo("date")
    @SerializedName("date")
    val date: Date,

    @ColumnInfo("duration")
    @SerializedName("duration")
    val duration: Int,

    @ColumnInfo("category")
    @SerializedName("category")
    val category: MatchCategory,

    @ColumnInfo("owner")
    @SerializedName("owner")
    val owner: String
) {
    fun getStatus(currentDate: Date = Date()): MatchStatus {
        val endTime = date.time + duration * 1000 // convert duration to milliseconds
        val currentTime = currentDate.time

        return when {
            currentTime < date.time -> MatchStatus.ON_HOLD
            currentTime < endTime -> MatchStatus.ON_GOING
            else -> MatchStatus.FINISHED
        }
    }
}

data class Location(
    @ColumnInfo("latitude")
    @SerializedName("latitude")
    val latitude: Double,

    @ColumnInfo("longitude")
    @SerializedName("longitude")
    val longitude: Double
)
