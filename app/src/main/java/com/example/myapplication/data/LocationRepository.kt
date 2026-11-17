package com.example.myapplication.data

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

data class DeviceLocation(
    val latitude: Double,
    val longitude: Double
)

class LocationRepository(context: Context) {
    private val appContext = context.applicationContext
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(): DeviceLocation {
        return suspendCancellableCoroutine { continuation ->
            val cancellationTokenSource = CancellationTokenSource()
            val request = CurrentLocationRequest.Builder()
                .setPriority(Priority.PRIORITY_BALANCED_POWER_ACCURACY)
                .build()

            val task = fusedLocationClient.getCurrentLocation(request, cancellationTokenSource.token)
            continuation.invokeOnCancellation {
                cancellationTokenSource.cancel()
            }
            task.addOnSuccessListener { location ->
                if (!continuation.isActive) return@addOnSuccessListener
                if (location == null) {
                    continuation.resumeWithException(IllegalStateException("Current location is unavailable."))
                } else {
                    continuation.resume(DeviceLocation(location.latitude, location.longitude))
                }
            }
            task.addOnFailureListener { error ->
                if (continuation.isActive) continuation.resumeWithException(error)
            }
        }
    }

    @Suppress("DEPRECATION")
    suspend fun reverseGeocode(location: DeviceLocation): String? {
        if (!Geocoder.isPresent()) return null
        return withContext(Dispatchers.IO) {
            Geocoder(appContext, Locale.getDefault())
                .getFromLocation(location.latitude, location.longitude, 1)
                ?.firstOrNull()
                ?.getAddressLine(0)
        }
    }
}
