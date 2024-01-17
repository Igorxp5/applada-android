package dev.igorxp5.applada.data.source.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import dev.igorxp5.applada.data.Subscription

@Dao
interface SubscriptionDao {
    @Query("SELECT * FROM subscriptions")
    suspend fun getSubscriptions(): List<Subscription>

    @Query("SELECT * FROM subscriptions WHERE match_id = :matchId")
    suspend fun getSubscriptionByMatchId(matchId: String): Subscription?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateSubscription(subscription: Subscription)

    @Query("DELETE FROM subscriptions WHERE match_id = :matchId")
    suspend fun deleteSubscriptionByMatchId(matchId: String)
}
