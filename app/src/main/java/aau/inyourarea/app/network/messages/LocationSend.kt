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
import aau.inyourarea.app.network.NetworkServiceHolder
import aau.inyourarea.app.network.getNetworkService
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.core.content.ContextCompat
import android.Manifest



/* Diese Klasse ist dafür verantwortlich, den aktuellen Standort des Geräts zu ermitteln und diesen an den Server zu senden.
   Sie verwendet die FusedLocationProviderClient API von Google Play Services, um Standortaktualisierungen zu erhalten.
   Die Klasse implementiert Methoden zum Starten und Stoppen der Standortaktualisierungen und sendet die Standortdaten als Payload an den Server. */


class LocationSend(private val context: Context,networkService: NetworkServiceHolder) {





    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)                                    //FusedLocationProviderClient für den Zugriff auf Standortdienste


    private val locationRequest = LocationRequest.Builder(                                          //LocationRequest für die Standortaktualisierungen
        Priority.PRIORITY_HIGH_ACCURACY,
        10000L
    ).build()

    private val locationCallback = object : LocationCallback() {                                    // LocationCallback, um Standortaktualisierungen zu empfangen
        override fun onLocationResult(result: LocationResult) {
            val location: Location = result.lastLocation ?: return
            val latitude = location.latitude
            val longitude = location.longitude

            val payload = LocationPayLoad(latitude, longitude)                                      // Payload für die Standortdaten
            if (networkService.service != null && networkService.service.loggedIn) {                // Überprüfen, ob der Dienst verbunden und der Benutzer angemeldet ist
                networkService.service.sendCommand(CommandType.UPDATE_LOCATION, payload)
            }
        }
    }

    /* Diese Methode wird aufgerufen, um den aktuellen Standort des Geräts zu ermitteln und an den Server zu senden.
       Sie überprüft die erforderlichen Berechtigungen und startet die Standortaktualisierungen. */
    fun startLocationUpdates() {
        val finePermission = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)       // Überprüfen der Berechtigung für den feinen Standort
        val coarsePermission = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)   // Überprüfen der Berechtigung für den groben Standort

        if (finePermission != PackageManager.PERMISSION_GRANTED &&
            coarsePermission != PackageManager.PERMISSION_GRANTED) {                                // Wenn keine der Berechtigungen erteilt wurde, wird eine Fehlermeldung protokolliert
            Log.e("LocationSend", "Location permissions not granted")
            return
        }

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)         // Anfordern von Standortaktualisierungen
        Log.d("LocationSend", "Started location updates")
    }
    /* Diese Methode wird aufgerufen, um die Standortaktualisierungen zu stoppen.
       Sie entfernt den LocationCallback von FusedLocationProviderClient. */
    fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)                                 // Stoppen der Standortaktualisierungen
        Log.d("LocationSend", "Stopped location updates")
    }

}
data class LocationPayLoad(                                                                         //Datenklasse für  die Standortdaten
    val latitude: Double,
    val longitude: Double,

    )
