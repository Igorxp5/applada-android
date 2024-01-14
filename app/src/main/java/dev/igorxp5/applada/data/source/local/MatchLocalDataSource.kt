package dev.igorxp5.applada.data.source.local

import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.SphericalUtil
import dev.igorxp5.applada.data.Location
import dev.igorxp5.applada.data.Match
import dev.igorxp5.applada.data.Result
import dev.igorxp5.applada.data.source.MatchDataSource
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MatchLocalDataSource internal constructor(
    private val matchDao: MatchDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : MatchDataSource {

    override suspend fun isAccessible(): Boolean {
        return true
    }

    override suspend fun getNearMatches(location: Location, radius: Double): Result<List<Match>> = withContext(ioDispatcher) {
        //Radius in Kilometers
        val centerPoint = LatLng(location.latitude, location.longitude)
        return@withContext try {
            val matches = matchDao.getMatches()
            val filteredMatches = matches.filter {
                val matchPoint = LatLng(it.location.latitude, it.location.longitude)
                SphericalUtil.computeDistanceBetween(centerPoint, matchPoint) / 1000 < radius
            }
            Result.Success(filteredMatches)
        } catch (exc: Exception) {
            Result.Error(exc)
        }
    }

    override suspend fun getMatchesByTitle(title: String): Result<List<Match>> = withContext(ioDispatcher) {
        return@withContext try {
            Result.Success(matchDao.getMatchesByTitle(title))
        } catch (exc: Exception) {
            Result.Error(exc)
        }
    }
}