package dev.igorxp5.applada.data.source

import android.content.Context
import dev.igorxp5.applada.data.Location
import dev.igorxp5.applada.data.Match
import dev.igorxp5.applada.data.Result

interface MatchDataSource {
    suspend fun getNearMatches(location: Location, radius: Double) : Result<List<Match>>
    suspend fun createMatch(match: Match) : Result<Boolean>
}