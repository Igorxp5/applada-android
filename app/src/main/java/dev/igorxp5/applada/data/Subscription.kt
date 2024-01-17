package dev.igorxp5.applada.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import dev.igorxp5.applada.data.roomconverters.DateConverter
import java.util.Date


@Entity(tableName = "subscriptions")
@TypeConverters(DateConverter::class)
data class Subscription(
    @ColumnInfo("id")
    @JsonProperty("_id", access = JsonProperty.Access.WRITE_ONLY)
    val id: String,

    @PrimaryKey
    @ColumnInfo("match_id")
    @JsonProperty("match_id")
    val matchId: String,

    @ColumnInfo("subscription_date")
    @JsonProperty("subscription_date")
    val subscriptionDate: Date = Date(),

    @ColumnInfo("_cache_updated_date")
    @JsonIgnore
    val cacheUpdatedDate: Date = Date()
)