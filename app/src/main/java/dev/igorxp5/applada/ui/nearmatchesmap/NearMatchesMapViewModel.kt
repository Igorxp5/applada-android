package dev.igorxp5.applada.ui.nearmatchesmap

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.igorxp5.applada.data.Location
import dev.igorxp5.applada.data.Match
import dev.igorxp5.applada.data.MatchCategory
import dev.igorxp5.applada.data.source.Result
import dev.igorxp5.applada.data.Subscription
import dev.igorxp5.applada.data.repositories.MatchRepository
import dev.igorxp5.applada.data.repositories.SubscriptionRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import java.util.Date
import java.util.concurrent.ThreadLocalRandom
import javax.inject.Inject
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

@HiltViewModel
class NearMatchesMapViewModel @Inject constructor(
    private val matchRepository: MatchRepository,
    private val subscriptionRepository: SubscriptionRepository
) : ViewModel() {
    val nearMatches = MutableLiveData<List<Match>?>()

    val selectedMatch = MutableLiveData<Match?>()

    private val selectedMatchSubscription = MutableLiveData<Subscription?>()
    val isSubscribedToSelectedMatch = MutableLiveData<Boolean?>()

    private var _fetchNearMatchesJob: Job? = null
    private var _fetchSelectedMatchSubscription: Job? = null

    val currentLocation = MutableLiveData<LatLng?>()

    init {
        selectedMatchSubscription.postValue(null)
        isSubscribedToSelectedMatch.postValue(null)
        selectedMatch.postValue(null)
        nearMatches.postValue(emptyList())
        currentLocation.postValue(null)
    }

    fun fetchNearMatches(location: LatLng) {
        _fetchNearMatchesJob?.cancel()
        _fetchNearMatchesJob = viewModelScope.launch {
            val result = matchRepository.getNearMatches(
                location = Location(location.latitude, location.longitude),
                radius = NEAR_MATCH_RADIUS
            )

            this.ensureActive()
            if (result is Result.Success) {
                nearMatches.postValue(result.data)
            } else if (result is Result.Error) {
                result.fallbackResult?.let {
                    if (it is Result.Success) {
                        nearMatches.postValue(it.data)
                    }
                }
                // TODO Show an error in the snackbar
                Log.e(LOG_TAG, result.exception.toString())
            }
        }
    }

    fun updateSelectedMatch(match: Match) {
        val matchSubscription = selectedMatchSubscription.value
        if (matchSubscription == null || matchSubscription.matchId != match.id) {
            selectedMatchSubscription.postValue(null)
            isSubscribedToSelectedMatch.postValue(null)
            _fetchSelectedMatchSubscription?.cancel()
            _fetchSelectedMatchSubscription = viewModelScope.launch {
                val subscriptionResult = subscriptionRepository.getMatchSubscription(match.id)
                this.ensureActive()
                if (subscriptionResult is Result.Success) {
                    selectedMatchSubscription.postValue(subscriptionResult.data)
                    isSubscribedToSelectedMatch.postValue(subscriptionResult.data != null)
                } else if (subscriptionResult is Result.Error) {
                    // TODO Show an error in the snackbar
                    Log.e(LOG_TAG, subscriptionResult.exception.toString())
                }
            }
        }
        selectedMatch.postValue(match)
    }

    fun unSelectMatch() {
        selectedMatch.postValue(null)
    }

    fun subscribeToMatch(match: Match) {
        // Foreseen the result (to be responsible for the user),
        //  if it fail the UI can bring the previous status back
        isSubscribedToSelectedMatch.postValue(true)
        viewModelScope.launch {
            val result = subscriptionRepository.createMatchSubscription(match.id)
            if (result is Result.Error<*>) { // Undo if it tails
                // TODO: Show an error in the snackbar
                isSubscribedToSelectedMatch.postValue(false)
            }
        }
    }

    fun unsubscribeToMatch(match: Match) {
        // Foreseen the result (to be responsible for the user),
        //  if it fail the UI can bring the previous status back
        isSubscribedToSelectedMatch.postValue(false)
        viewModelScope.launch {
            val result = subscriptionRepository.cancelMatchSubscription(match.id)
            if (result is Result.Error<*>) { // Undo if it tails
                // TODO: Show an error in the snackbar
                isSubscribedToSelectedMatch.postValue(true)
            }
        }
    }

    fun createRandomMatch() {
        if (currentLocation.value != null) {
            viewModelScope.launch {
                val location = currentLocation.value!!
                val earthRadius = 6371.0

                val randomRadiusRange = 2.0 // 2 Km

                val radiusRadians = randomRadiusRange / earthRadius

                val randomAngle = Math.toRadians(Math.random() * 360)

                val centerLatRadians = Math.toRadians(location.latitude)
                val centerLonRadians = Math.toRadians(location.longitude)

                val newLatRadians = asin(sin(centerLatRadians) * cos(radiusRadians) +
                        cos(centerLatRadians) * sin(radiusRadians) * cos(randomAngle))

                val newLonRadians = centerLonRadians + atan2(sin(randomAngle) * sin(radiusRadians) * cos(centerLatRadians),
                        cos(radiusRadians) - sin(centerLatRadians) * sin(newLatRadians))

                val newLatitude = Math.toDegrees(newLatRadians)
                val newLongitude = Math.toDegrees(newLonRadians)

                val matchLocation = Location(newLatitude, newLongitude)
                val fromDate = Date()
                val toDate = Date(fromDate.time + 24 * 60 * 60 * 1000) // One day ahead

                val randomInt = Random.nextInt(100)

                val match = Match(
                    id = "",
                    title = "The match #${randomInt}",
                    description = null,
                    limitParticipants = null,
                    location = matchLocation,
                    date = getRandomDate(fromDate, toDate),
                    duration = 3600,
                    category = getRandomMatchCategory(),
                    owner = "DummyOwner"
                )
                matchRepository.createMatch(match)
                fetchNearMatches(location)
            }
        } else {
            // TODO: Show an error in the snackbar
        }
    }

    companion object {
        const val LOG_TAG = "NearMatchesMapViewModel"
        const val NEAR_MATCH_RADIUS = 25.0 // Km

        private fun getRandomMatchCategory(): MatchCategory {
            val values = MatchCategory.entries.toTypedArray()
            val randomIndex = Random.nextInt(values.size)
            return values[randomIndex]
        }

        private fun getRandomDate(from: Date, to: Date): Date {
            val randomMillisSinceEpoch = ThreadLocalRandom
              .current()
              .nextLong(from.time, to.time)

            return Date(randomMillisSinceEpoch)
        }
    }
}