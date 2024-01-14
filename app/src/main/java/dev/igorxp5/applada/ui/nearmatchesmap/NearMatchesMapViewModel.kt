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
import dev.igorxp5.applada.data.Result
import dev.igorxp5.applada.data.repositories.MatchRepository
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NearMatchesMapViewModel @Inject constructor(
    private val matchRepository: MatchRepository
) : ViewModel() {
    private val _nearMatches = MutableLiveData<List<Match>?>()
    val nearMatches: LiveData<List<Match>?>
        get() = _nearMatches

    init {
        _nearMatches.value = emptyList()
    }

    fun fetchNearMatches(location: LatLng) {
        viewModelScope.launch {
            val result = matchRepository.getNearMatches(
                location = Location(location.latitude, location.longitude),
                radius = NEAR_MATCH_RADIUS
            )

            if (result is Result.Success) {
                _nearMatches.postValue(result.data)
            } else if (result is Result.Error) {
                // TODO Show an error in the snackbar
                Log.e(LOG_TAG, result.exception.toString())
            }
        }
    }

    companion object {
        const val LOG_TAG = "NearMatchesMapViewModel"
        const val NEAR_MATCH_RADIUS = 25.0 // Km
    }
}