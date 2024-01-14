package dev.igorxp5.applada.data

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import dev.igorxp5.applada.data.MatchStatus
import dev.igorxp5.applada.data.converters.MatchCategoryConverter
import dev.igorxp5.applada.data.converters.DateConverter
import java.util.Date


@Entity(tableName = "matches")
@TypeConverters(MatchCategoryConverter::class, DateConverter::class)
data class Match(
    @PrimaryKey
    val id: String,

    @ColumnInfo(name = "title")
    val title: String,

    @ColumnInfo(name = "description")
    val description: String?,

    @ColumnInfo(name = "limit_participants")
    val limitParticipants: Int?,

    @Embedded
    val location: Location,

    @ColumnInfo(name = "date")
    val date: Date,

    @ColumnInfo(name = "duration")
    val duration: Int,

    @ColumnInfo(name = "category")
    val category: String,

    @ColumnInfo(name = "owner")
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
    @ColumnInfo(name = "latitude")
    val latitude: Double,

    @ColumnInfo(name = "longitude")
    val longitude: Double
)
