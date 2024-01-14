package dev.igorxp5.applada.data.repositories

import dev.igorxp5.applada.data.Location
import dev.igorxp5.applada.data.Match
import dev.igorxp5.applada.data.Result
import dev.igorxp5.applada.data.source.MatchDataSource
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

class MatchRepository(
    private val localSource: MatchDataSource,
    private val remoteSource: MatchDataSource,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    suspend fun getNearMatches(location: Location, radius: Double) : Result<List<Match>> {
        var result : Result<List<Match>>? = null
        if (remoteSource.isAccessible()) {
            result = remoteSource.getNearMatches(location, radius)
        }
        if (result == null || result is Result.Error) {
            result = localSource.getNearMatches(location, radius)
        }
        return result
    }
}