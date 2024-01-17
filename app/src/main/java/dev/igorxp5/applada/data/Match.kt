package dev.igorxp5.applada.data

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import dev.igorxp5.applada.data.roomconverters.MatchCategoryConverter
import dev.igorxp5.applada.data.roomconverters.DateConverter
import java.util.Date


@Entity(tableName = "matches")
@TypeConverters(MatchCategoryConverter::class, DateConverter::class)
data class Match(
    @PrimaryKey
    @ColumnInfo("id")
    @JsonProperty("_id")
    val id: String,

    @ColumnInfo("title")
    @JsonProperty("title")
    val title: String,

    @ColumnInfo("description")
    @JsonProperty("description")
    val description: String?,

    @ColumnInfo("limit_participants")
    @JsonProperty("limit_participants")
    val limitParticipants: Int?,

    @Embedded
    @JsonProperty("location")
    val location: Location,

    @ColumnInfo("date")
    @JsonProperty("date")
    val date: Date,

    @ColumnInfo("duration")
    @JsonProperty("duration")
    val duration: Int,

    @ColumnInfo("category")
    @JsonProperty("category")
    val category: MatchCategory,

    @ColumnInfo("owner")
    @JsonProperty("owner")
    val owner: String,

    @ColumnInfo("_cache_updated_date")
    @JsonIgnore
    val cacheUpdatedDate: Date = Date()
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
    @JsonProperty("latitude")
    val latitude: Double,

    @ColumnInfo("longitude")
    @JsonProperty("longitude")
    val longitude: Double
)
