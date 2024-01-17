package dev.igorxp5.applada.data.repositories

import dev.igorxp5.applada.data.Location
import dev.igorxp5.applada.data.Match
import dev.igorxp5.applada.data.source.Result
import dev.igorxp5.applada.data.source.MatchDataSource
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

class MatchRepository(
    private val remoteSource: MatchDataSource,
    private val localSource: MatchDataSource,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    suspend fun getNearMatches(location: Location, radius: Double) : Result<List<Match>> {
        var result = remoteSource.getNearMatches(location, radius)
        if (result is Result.Error<*>) {
            result = Result.Error(result.exception, localSource.getNearMatches(location, radius))
        } else if (result is Result.Success) {
            result.data.forEach {
                localSource.createMatch(it)
            }
        }
        return result
    }

    suspend fun createMatch(match: Match) : Result<Match> {
        val result = remoteSource.createMatch(match)
        if (result is Result.Success) {
            localSource.createMatch(match)
        }
        return result
    }
}
