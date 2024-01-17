package dev.igorxp5.applada.data.source.remote

import android.util.Log
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.SphericalUtil
import dev.igorxp5.applada.data.Location
import dev.igorxp5.applada.data.Match
import dev.igorxp5.applada.data.MatchStatus
import dev.igorxp5.applada.data.source.Result
import dev.igorxp5.applada.data.source.MatchDataSource
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MatchRemoteDataSource internal constructor(
    private val api: AppLadaApi,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : MatchDataSource {

    override suspend fun getNearMatches(location: Location, radius: Double): Result<List<Match>> = withContext(ioDispatcher) {
        //Radius in Kilometers
        val centerPoint = LatLng(location.latitude, location.longitude)
        return@withContext try {
            val matches = api.getNearMatches(location.latitude, location.longitude, radius)
            // FIXME: In real-world, the API would actually filter the unfinished matches by nearest to center point.
            //  Since this project is using an Mock API service, the filtering feature is not available.
            //  By now let's filter here.
            var filteredMatches = matches.filter { it.getStatus() != MatchStatus.FINISHED }
            filteredMatches = filteredMatches.filter {
                val matchPoint = LatLng(it.location.latitude, it.location.longitude)
                SphericalUtil.computeDistanceBetween(centerPoint, matchPoint) / 1000 < radius
            }
            Result.Success(filteredMatches)
        } catch (exc: Exception) {
            Log.e(LOG_TAG, exc.stackTraceToString())
            Result.Error<List<Match>>(exc)
        }
    }

    override suspend fun createMatch(match: Match): Result<Match> = withContext(ioDispatcher) {
        return@withContext try {
            val createdSubscription = api.createMatch(match)
            Result.Success(createdSubscription)
        } catch (exc: Exception) {
            Log.e(LOG_TAG, exc.stackTraceToString())
            Result.Error<Match>(exc)
        }
    }

    companion object {
        const val LOG_TAG = "MatchRemoteDataSource"
    }
}