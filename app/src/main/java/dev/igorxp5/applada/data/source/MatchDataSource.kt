package dev.igorxp5.applada.data.source

import dev.igorxp5.applada.data.Location
import dev.igorxp5.applada.data.Match

interface MatchDataSource {
    suspend fun getNearMatches(location: Location, radius: Double) : Result<List<Match>>
    suspend fun createMatch(match: Match) : Result<Match>
}