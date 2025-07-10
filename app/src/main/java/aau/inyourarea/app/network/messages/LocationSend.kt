package aau.inyourarea.app.network.messages


import android.content.Context
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import android.location.Location
import androidx.compose.runtime.mutableStateOf
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import com.google.android.gms.location.LocationServices
import aau.inyourarea.app.network.NetworkService
import android.annotation.SuppressLint
import android.util.Log

class LocationSend(private val context: Context) {
    private val networkService = NetworkService() // get NetworkService instance Fix!!!
    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)
    
    private val locationRequest = LocationRequest.Builder(
        Priority.PRIORITY_HIGH_ACCURACY,
        10000L
    ).build()

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            val location: Location = result.lastLocation ?: return
            val latitude = location.latitude
            val longitude = location.longitude

            val payload = LocationPayLoad(latitude, longitude)

            // Hier wird die Payload an den NetworkService gesendet
        }
    }
    @SuppressLint("MissingPermission")
    fun startLocationUpdates() {
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
        Log.d("LocationSend", "Started location updates")
    }

    fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
        Log.d("LocationSend", "Stopped location updates")
    }
}
data class LocationPayLoad(
    val latitude: Double,
    val longitude: Double,

    )
