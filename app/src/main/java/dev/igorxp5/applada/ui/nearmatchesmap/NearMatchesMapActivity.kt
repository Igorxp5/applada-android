package dev.igorxp5.applada.ui.nearmatchesmap

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.rememberCameraPositionState
import dagger.hilt.android.AndroidEntryPoint
import dev.igorxp5.applada.R
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
        BottomAppBar(
            containerColor = Color.White
        ) {
            Column {
                Text("BottomBar")
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
            }
        }

        val scope = rememberCoroutineScope()

        Scaffold (
            floatingActionButtonPosition = FabPosition.End,
            floatingActionButton = {
                FloatingActionButton(
                    containerColor = Color.White,
                    onClick = {
                        deviceLocation?.let {
                        scope.launch {
                            cameraPositionState.animate(
                                CameraUpdateFactory.newLatLngZoom(centerPoint, DEVICE_LOCATION_MAP_CAMERA_ZOOM), 1000
                            )
                        }
                        centerPoint = it
                    }
                    }
                ) {
                    Icon(painterResource(R.drawable.icon_my_location), stringResource(R.string.my_location_fab_description))
                }
            },
        ) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                uiSettings = MapUiSettings(
                    zoomControlsEnabled = false,
                    myLocationButtonEnabled = false
                ),
                properties = MapProperties(isMyLocationEnabled = isLocationPermissionGranted())
            )
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



