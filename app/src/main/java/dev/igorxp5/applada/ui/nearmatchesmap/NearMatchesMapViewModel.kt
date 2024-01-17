package dev.igorxp5.applada.ui.nearmatchesmap

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.igorxp5.applada.data.Location
import dev.igorxp5.applada.data.Match
import dev.igorxp5.applada.data.source.Result
import dev.igorxp5.applada.data.Subscription
import dev.igorxp5.applada.data.repositories.MatchRepository
import dev.igorxp5.applada.data.repositories.SubscriptionRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NearMatchesMapViewModel @Inject constructor(
    private val matchRepository: MatchRepository,
    private val subscriptionRepository: SubscriptionRepository
) : ViewModel() {
    private val _nearMatches = MutableLiveData<List<Match>?>()
    val nearMatches: LiveData<List<Match>?>
        get() = _nearMatches

    private val _selectedMatch = MutableLiveData<Match?>()
    val selectedMatch: LiveData<Match?>
        get() = _selectedMatch

    private val _selectedMatchSubscription = MutableLiveData<Subscription?>()
    private val _isSubscribedToSelectedMatch = MutableLiveData<Boolean?>()

    val isSubscribedToSelectedMatch: LiveData<Boolean?>
        get() = _isSubscribedToSelectedMatch

    private var _fetchNearMatchesJob: Job? = null
    private var _fetchSelectedMatchSubscription: Job? = null

    init {
        _selectedMatchSubscription.postValue(null)
        _isSubscribedToSelectedMatch.postValue(null)
        _selectedMatch.postValue(null)
        _nearMatches.postValue(emptyList())
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
                _nearMatches.postValue(result.data)
            } else if (result is Result.Error) {
                result.fallbackResult?.let {
                    if (it is Result.Success) {
                        _nearMatches.postValue(it.data)
                    }
                }
                // TODO Show an error in the snackbar
                Log.e(LOG_TAG, result.exception.toString())
            }
        }
    }

    fun updateSelectedMatch(match: Match) {
        val matchSubscription = _selectedMatchSubscription.value
        if (matchSubscription == null || matchSubscription.matchId != match.id) {
            _selectedMatchSubscription.postValue(null)
            _isSubscribedToSelectedMatch.postValue(null)
            _fetchSelectedMatchSubscription?.cancel()
            _fetchSelectedMatchSubscription = viewModelScope.launch {
                val subscriptionResult = subscriptionRepository.getMatchSubscription(match.id)
                this.ensureActive()
                if (subscriptionResult is Result.Success) {
                    _selectedMatchSubscription.postValue(subscriptionResult.data)
                    _isSubscribedToSelectedMatch.postValue(subscriptionResult.data != null)
                } else if (subscriptionResult is Result.Error) {
                    // TODO Show an error in the snackbar
                    Log.e(LOG_TAG, subscriptionResult.exception.toString())
                }
            }
        }
        _selectedMatch.postValue(match)
    }

    fun unSelectMatch() {
        _selectedMatch.postValue(null)
    }

    fun subscribeToMatch(match: Match) {
        // Foreseen the result (to be responsible for the user),
        //  if it fail the UI can bring the previous status back
        _isSubscribedToSelectedMatch.postValue(true)
        viewModelScope.launch {
            val result = subscriptionRepository.createMatchSubscription(match.id)
            if (result is Result.Error<*>) { // Undo if it tails
                // TODO: Show an error in the snackbar
                _isSubscribedToSelectedMatch.postValue(false)
            }
        }
    }

    fun unsubscribeToMatch(match: Match) {
        // Foreseen the result (to be responsible for the user),
        //  if it fail the UI can bring the previous status back
        _isSubscribedToSelectedMatch.postValue(false)
        viewModelScope.launch {
            val result = subscriptionRepository.cancelMatchSubscription(match.id)
            if (result is Result.Error<*>) { // Undo if it tails
                // TODO: Show an error in the snackbar
                _isSubscribedToSelectedMatch.postValue(true)
            }
        }
    }

    companion object {
        const val LOG_TAG = "NearMatchesMapViewModel"
        const val NEAR_MATCH_RADIUS = 25.0 // Km
    }
}