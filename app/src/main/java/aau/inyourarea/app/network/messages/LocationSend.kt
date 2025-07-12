package aau.inyourarea.app.network.messages


import aau.inyourarea.app.network.CommandType
import android.content.Context
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import android.location.Location
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import com.google.android.gms.location.LocationServices
import aau.inyourarea.app.network.NetworkService
import aau.inyourarea.app.network.getNetworkService
import android.annotation.SuppressLint
import android.content.Intent
import android.util.Log
import androidx.activity.ComponentActivity

class LocationSend(private val context: Context):ComponentActivity() {
    override fun onStart() {
        super.onStart()
        Intent(this, NetworkService::class.java).also {
            startService(it)
            bindService(it, networkServiceHolder.connection, BIND_AUTO_CREATE)
        }
    }

    override fun onStop() {
        super.onStop()
        unbindService(networkServiceHolder.connection)
    }
    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)
    val networkServiceHolder = getNetworkService()

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
            networkServiceHolder.service.sendCommand(CommandType.UPDATE_LOCATION,payload)

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
