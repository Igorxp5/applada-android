package dev.igorxp5.applada.ui.nearmatchesmap

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.igorxp5.applada.data.repositories.MatchRepository
import javax.inject.Inject

@HiltViewModel
class NearMatchesMapViewModel @Inject constructor(
    private val matchRepository: MatchRepository
) : ViewModel() {

}