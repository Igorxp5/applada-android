package dev.igorxp5.applada.data.source.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import dev.igorxp5.applada.data.MatchStatus
import dev.igorxp5.applada.data.Match

@Dao
interface MatchDao {
    @Query("SELECT * FROM matches")
    suspend fun getMatches(): List<Match>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateMatch(match: Match)

    @Query("SELECT * FROM matches WHERE id = :matchId")
    suspend fun getMatchById(matchId: String): Match?

    @Delete
    suspend fun deleteMatch(match: Match)

    suspend fun deleteFinishedMatches() {
        val allMatches = getMatches()
        val finishedMatches = allMatches.filter { it.getStatus() == MatchStatus.FINISHED }

        for (match in finishedMatches) {
            deleteMatch(match)
        }
    }
}