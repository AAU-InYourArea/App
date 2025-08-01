package aau.inyourarea.app.network

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class NotificationActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        when (intent?.action) {
            "network_disconnect" -> { // receives the button press from the notification disconnect button
                val serviceIntent = Intent(context, NetworkService::class.java)
                context?.stopService(serviceIntent) // we simply stop the service
            }
        }
    }
}