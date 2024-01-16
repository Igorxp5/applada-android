package dev.igorxp5.applada.data.repositories

import dev.igorxp5.applada.data.Location
import dev.igorxp5.applada.data.Match
import dev.igorxp5.applada.data.Result
import dev.igorxp5.applada.data.source.MatchDataSource
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import java.util.Date

class MatchRepository(
    private val remoteSource: MatchDataSource,
    private val localSource: MatchDataSource,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    suspend fun getNearMatches(location: Location, radius: Double) : Result<List<Match>> {
        var result : Result<List<Match>> = remoteSource.getNearMatches(location, radius)
        if (result is Result.Error) {
            result = localSource.getNearMatches(location, radius)
        } else if (result is Result.Success) {
            result.data.forEach {
                val cacheMatch = it.copy(cacheUpdatedDate = Date())
                localSource.createMatch(cacheMatch)
            }
        }
        return result
    }
}
