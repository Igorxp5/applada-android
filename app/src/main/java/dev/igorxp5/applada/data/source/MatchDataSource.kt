package dev.igorxp5.applada.data.source

import dev.igorxp5.applada.data.Location
import dev.igorxp5.applada.data.Match
import dev.igorxp5.applada.data.Result

interface MatchDataSource {
    suspend fun isAccessible() : Boolean
    suspend fun getNearMatches(location: Location, radius: Double) : Result<List<Match>>
    suspend fun getMatchesByTitle(title: String) : Result<List<Match>>
}