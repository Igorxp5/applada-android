package dev.igorxp5.applada.ui.nearmatchesmap

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.asAndroidColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import dagger.hilt.android.AndroidEntryPoint
import dev.igorxp5.applada.R
import dev.igorxp5.applada.data.Match
import dev.igorxp5.applada.data.MatchCategory
import dev.igorxp5.applada.data.MatchStatus
import kotlinx.coroutines.launch
import java.util.TimeZone

@AndroidEntryPoint
class NearMatchesMapActivity : ComponentActivity() {

    private val mapViewModel by viewModels<NearMatchesMapViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            var isLocationPermissionGranted by remember { mutableStateOf(isLocationPermissionGranted()) }
            var shouldShowLocationRationale by remember {
                mutableStateOf(
                    shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION)
                            || shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)
                )
            }

            val locationPermissionLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestMultiplePermissions(),
                onResult = { permissions ->
                    isLocationPermissionGranted = permissions.values.all { it }
                    if (!isLocationPermissionGranted) {
                        shouldShowLocationRationale = shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION)
                            || shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)
                    }
                }
            )

            val lifecycleOwner = LocalLifecycleOwner.current

            DisposableEffect(lifecycleOwner) {
                val observer = LifecycleEventObserver { _, event ->
                    if (event == Lifecycle.Event.ON_START && !isLocationPermissionGranted && !shouldShowLocationRationale) {
                        requestLocationPermission(locationPermissionLauncher)
                    }
                }
                lifecycleOwner.lifecycle.addObserver(observer)
                onDispose {
                    lifecycleOwner.lifecycle.removeObserver(observer)
                }
            }

            if (shouldShowLocationRationale) {
                ShowLocationPermissionRationaleAlert(locationPermissionLauncher)
            }
            RootLayout(deviceLocationAsCenterPoint = isLocationPermissionGranted)
        }
    }

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    @Composable
    fun RootLayout(deviceLocationAsCenterPoint: Boolean) {
        Scaffold (
            containerColor = Color.White,
            modifier = Modifier.fillMaxSize(),
            topBar = {
                TopBarLayout()
            },
            bottomBar = {
                BottomBarLayout()
            }
        ) { innerPadding ->
            Surface(modifier = Modifier.padding(innerPadding)) {
                MapLayout(deviceLocationAsCenterPoint)
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun TopBarLayout() {
        Box {
           TopAppBar(
               title = {
                       Text("AppLada")
               },
               modifier = Modifier.fillMaxWidth())
      }
    }

    @Composable
    fun BottomBarLayout() {
        val selectedMatch = mapViewModel.selectedMatch.observeAsState().value

        AnimatedContent(
            selectedMatch,
            transitionSpec = {
                fadeIn(
                    animationSpec = tween(300)
                ) togetherWith fadeOut(animationSpec = tween(300))
            },
            label = "BottomBarLayout"
        ) { selected ->
            BottomAppBar(
                containerColor = Color.White,
                modifier = Modifier
                    .padding(dimensionResource(R.dimen.map_bottom_bar_padding))
                    .animateContentSize()
            ) {
                if (selected == null) {
                    BottomBarNearMatches()
                } else {
                    BottomBarMatchSelected(selected)
                }
            }
        }

    }

    @Composable
    fun BottomBarMatchSelected(match: Match) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        style = MaterialTheme.typography.titleLarge.copy(),
                        text = match.title
                    )
                    MatchStatusTextForMatchSelected(match)
                }

                IconButton(
                    onClick = { /* TODO */ }
                ) {
                    Icon(Icons.Filled.MoreVert, contentDescription = stringResource(R.string.selected_match_view_more_description))
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "Volleyball")
                    Text(text = "Volleyball")
                    Text(text = "Volleyball")
                    Text(text = "Volleyball")
                    Text(text = "Volleyball")
                }
            }
        }
    }

    @Composable
    fun MatchStatusTextForMatchSelected(match: Match) {
        val color = when(match.getStatus()) {
            MatchStatus.ON_HOLD -> colorResource(R.color.selected_match_status_on_hold)
            MatchStatus.ON_GOING -> colorResource(R.color.selected_match_status_on_going)
            MatchStatus.FINISHED -> colorResource(R.color.selected_match_status_finished)
        }
        val text = when(match.getStatus()) {
            MatchStatus.ON_GOING -> stringResource(R.string.selected_match_on_going_text)
            MatchStatus.ON_HOLD -> stringResource(R.string.selected_match_on_hold_text)
            MatchStatus.FINISHED -> stringResource(R.string.selected_match_finished_text)
        }
        Text(
            text = text,
            color = color
        )
    }

    @Composable
    fun MatchPropertyIconText(icon: Painter, text: String) {

    }

    @Composable
    fun BottomBarNearMatches() {
        val totalMatches = mapViewModel.nearMatches.observeAsState().value?.count() ?: 0

        Row {
            Column {
                Text(
                    style = MaterialTheme.typography.titleLarge,
                    text = stringResource(R.string.near_matches_bottom_bar_title)
                )
                Text(
                    pluralStringResource(
                        R.plurals.near_matches_bottom_bar_found_number,totalMatches, totalMatches
                    )
                )
            }
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.End,
            ) {
                TextButton(
                    onClick = { /*TODO*/ }
                ) {
                    Text(
                        text = stringResource(R.string.near_matches_bottom_bar_new_match_button).uppercase()
                    )
                }
            }
        }
    }

    @SuppressLint("MissingPermission", "UnusedMaterial3ScaffoldPaddingParameter")
    @Composable
    fun MapLayout(deviceLocationAsCenterPoint: Boolean) {
        val fusedLocationProviderClient = remember { LocationServices.getFusedLocationProviderClient(this) }
        val defaultCenterPoint = getLatLngFromCurrentTimeZone()
        var centerPoint by remember { mutableStateOf(defaultCenterPoint) }
        var deviceLocation by remember { mutableStateOf<LatLng?>(null) }

        val cameraPositionState = rememberCameraPositionState {
            position = CameraPosition.fromLatLngZoom(centerPoint, DEFAULT_MAP_CAMERA_ZOOM)
        }

        if (deviceLocationAsCenterPoint) {
            val locationResult = fusedLocationProviderClient.lastLocation
            locationResult.addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    deviceLocation = LatLng(task.result.latitude, task.result.longitude)
                    if (centerPoint == defaultCenterPoint) {
                        centerPoint = deviceLocation!!
                        cameraPositionState.position = CameraPosition.fromLatLngZoom(centerPoint, DEVICE_LOCATION_MAP_CAMERA_ZOOM)
                    }
                }
                mapViewModel.fetchNearMatches(centerPoint)
            }
        } else {
            mapViewModel.fetchNearMatches(centerPoint)
        }

        val scope = rememberCoroutineScope()

        Scaffold (
            floatingActionButtonPosition = FabPosition.End,
            floatingActionButton = {
                FloatingActionButton(
                    containerColor = Color.White,
                    onClick = {
                        deviceLocation?.let {
                            centerPoint = it
                            scope.launch {
                                cameraPositionState.animate(
                                    CameraUpdateFactory.newLatLngZoom(centerPoint, DEVICE_LOCATION_MAP_CAMERA_ZOOM), 1000
                                )
                                mapViewModel.fetchNearMatches(centerPoint)
                                mapViewModel.unSelectMatch()
                            }
                        }
                    }
                ) {
                    Icon(painterResource(R.drawable.ic_my_location), stringResource(R.string.my_location_fab_description))
                }
            },
        ) {
            if (!cameraPositionState.isMoving) {
                mapViewModel.fetchNearMatches(cameraPositionState.position.target)
            }

            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                uiSettings = MapUiSettings(
                    zoomControlsEnabled = false,
                    myLocationButtonEnabled = false
                ),
                properties = MapProperties(isMyLocationEnabled = isLocationPermissionGranted()),
                onMapClick = {
                    mapViewModel.unSelectMatch()
                }
            ) {
                NearMatchMarkers()
            }
        }
    }

    @Composable
    fun NearMatchMarkers() {
        val nearMatches = mapViewModel.nearMatches.observeAsState().value
        val selectedMatch = mapViewModel.selectedMatch.observeAsState().value
        nearMatches?.let { matches ->
            matches.forEach { match ->
                val isSelected = selectedMatch == match
                Marker(
                    state = rememberMarkerState(position = LatLng(match.location.latitude, match.location.longitude)),
                    tag = match,
                    icon = getMarkerIconForMatch(match, isSelected),
                    onClick = { marker ->
                        mapViewModel.updateSelectedMatch(marker.tag as Match)
                        true
                    }
                )
            }
        }
    }

    @Composable
    fun ShowLocationPermissionRationaleAlert(locationPermissionLauncher: ManagedActivityResultLauncher<Array<String>, Map<String, @JvmSuppressWildcards Boolean>>) {
        AlertDialog(
            onDismissRequest = {},
            title = {
                Text(stringResource(R.string.location_permission_rationale_title))
            },
            text = {
                Text(stringResource(R.string.location_permission_rationale_text))
            },
            confirmButton = {
                TextButton(onClick = {
                    requestLocationPermission(locationPermissionLauncher)
                }) {
                    Text(stringResource(R.string.location_permission_rationale_action_button))
                }
            })
    }

    private fun getLatLngFromCurrentTimeZone() : LatLng {
        val latitude = 0.0
        val longitude = (TimeZone.getDefault().rawOffset.toDouble() / 3600000) * (180 / 12)
        return LatLng(latitude, longitude)
    }

    private fun getMarkerIconForMatch(match: Match, isSelected: Boolean): BitmapDescriptor {
        val drawable = ContextCompat.getDrawable(this, R.drawable.ic_sharp_location)
        val color = if (match.getStatus() == MatchStatus.ON_GOING) {
            ContextCompat.getColor(this, R.color.on_going_marker_color)
        } else {
            when (match.category) {
                MatchCategory.SOCCER -> ContextCompat.getColor(this, R.color.soccer_marker_color)
                MatchCategory.VOLLEYBALL -> ContextCompat.getColor(this, R.color.volleyball_marker_color)
                MatchCategory.BASKETBALL -> ContextCompat.getColor(this, R.color.basketball_marker_color)
            }
        }
        drawable!!.colorFilter = ColorFilter.tint(Color(color), BlendMode.SrcIn).asAndroidColorFilter()
        val expandFactor =  if(isSelected) 1.5f else 1f
        val bitmap = Bitmap.createBitmap(
            (drawable.intrinsicWidth * expandFactor).toInt(),
            (drawable.intrinsicHeight * expandFactor).toInt(),
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

    private fun requestLocationPermission(locationPermissionLauncher: ManagedActivityResultLauncher<Array<String>, Map<String, @JvmSuppressWildcards Boolean>>) {
        locationPermissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION))
    }

    private fun isLocationPermissionGranted() : Boolean {
        val permissions = listOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION)
        return permissions.all { ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED }
    }

    companion object {
        const val DEFAULT_MAP_CAMERA_ZOOM = 10f
        const val DEVICE_LOCATION_MAP_CAMERA_ZOOM = 14f
    }
}
