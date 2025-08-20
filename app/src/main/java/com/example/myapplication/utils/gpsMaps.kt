import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast

//Apre Google Maps con l'indirizzo specificato
fun openAddressInMaps(context: Context, address: String) {
    val encodedAddress = Uri.encode(address)
    val gmmIntentUri = Uri.parse("geo:0,0?q=$encodedAddress")

    // Primo tentativo: apri con l'app Google Maps
    Intent(Intent.ACTION_VIEW, gmmIntentUri).apply {
        setPackage("com.google.android.apps.maps")
        try {
            context.startActivity(this)
            return
        } catch (e: ActivityNotFoundException) {
            // Ignora e procedi al fallback
        }
    }

    // Fallback: apri nel browser
    val browserIntent = Intent(
        Intent.ACTION_VIEW,
        Uri.parse("https://www.google.com/maps/search/?api=1&query=$encodedAddress")
    )
    try {
        context.startActivity(browserIntent)
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(context, "Nessuna app disponibile", Toast.LENGTH_SHORT).show()
        throw e
    }
}

//Apre Google Maps con coordinate GPS
fun openLocationInMaps(context: Context, latitude: Double, longitude: Double) {
    val geoUri = Uri.parse("geo:$latitude,$longitude?z=15") // z=15 è lo zoom
    val intent = Intent(Intent.ACTION_VIEW, geoUri).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
    }

    try {
        context.startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(
            context,
            "Installa Google Maps per visualizzare la posizione",
            Toast.LENGTH_LONG
        ).show()
    }
}