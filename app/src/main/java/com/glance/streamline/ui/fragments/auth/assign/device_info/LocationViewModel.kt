package com.glance.streamline.ui.fragments.auth.assign.device_info


import android.Manifest
import android.app.Application
import android.location.Location
import android.os.Looper
import android.util.Log
import androidx.annotation.MainThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.glance.streamline.data.room.AppDatabase
import com.glance.streamline.mvvm.BaseViewModel
import com.glance.streamline.utils.extensions.android.Failure
import com.glance.streamline.utils.extensions.hasPermission
import com.google.android.gms.location.*
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import java.util.concurrent.TimeUnit
import javax.inject.Inject

private const val TAG = "MyLocationManager"

class LocationViewModel @Inject constructor(
    app: Application,
    private val db: AppDatabase
) : BaseViewModel(app) {

    val receivingLocationUpdatesTimer = MutableLiveData<Boolean>()
    val receivingLocationUpdates = MutableLiveData<Boolean>()

    private val _locationLiveData = MutableLiveData<Location>()
    val locationLiveData: LiveData<Location>
        get() = _locationLiveData

    private var timerDisposable: Disposable? = null

    // The Fused Location Provider provides access to location APIs.
    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(getContext())

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            locationResult.locations.firstOrNull()?.let {
                _locationLiveData.postValue(it)
                stopLocationUpdates()
            }
        }

        override fun onLocationAvailability(availability: LocationAvailability) {
            if (!availability.isLocationAvailable) onError(
                Failure<Any>(
                    "Location is unavailable!",
                    0
                )
            )
        }
    }

    // Stores parameters for requests to the FusedLocationProviderApi.
    private val locationRequest: LocationRequest = LocationRequest().apply {
        // Sets the desired interval for active location updates. This interval is inexact. You
        // may not receive updates at all if no location sources are available, or you may
        // receive them slower than requested. You may also receive updates faster than
        // requested if other applications are requesting location at a faster interval.
        //
        // IMPORTANT NOTE: Apps running on "O" devices (regardless of targetSdkVersion) may
        // receive updates less frequently than this interval when the app is no longer in the
        // foreground.
        interval = TimeUnit.SECONDS.toMillis(60)

        // Sets the fastest rate for active location updates. This interval is exact, and your
        // application will never receive updates faster than this value.
        fastestInterval = TimeUnit.SECONDS.toMillis(30)

        // Sets the maximum time when batched location updates are delivered. Updates may be
        // delivered sooner than this interval.
        maxWaitTime = TimeUnit.MINUTES.toMillis(2)

        priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }

    /**
     * Uses the FusedLocationProvider to start location updates if the correct fine locations are
     * approved.
     *
     * @throws SecurityException if ACCESS_FINE_LOCATION permission is removed before the
     * FusedLocationClient's requestLocationUpdates() has been completed.
     */
    @Throws(SecurityException::class)
    @MainThread
    fun startLocationUpdates() {
        Log.d(TAG, "startLocationUpdates()")

        if (!getContext().hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)) return

        try {
            // If the PendingIntent is the same as the last request (which it always is), this
            // request will replace any requestLocationUpdates() called before.
            receivingLocationUpdates.value = true
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        } catch (permissionRevoked: SecurityException) {
            receivingLocationUpdates.value = false
            _locationLiveData.value = null

            // Exception only occurs if the user revokes the FINE location permission before
            // requestLocationUpdates() is finished executing (very rare).
            Log.d(TAG, "Location permission revoked; details: $permissionRevoked")
            throw permissionRevoked
        }
    }

    @MainThread
    fun stopLocationUpdates() {
        Log.d(TAG, "stopLocationUpdates()")
        receivingLocationUpdates.value = false
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    fun startLogoutTimeoutTimer(isReceivingLocationUpdates: Boolean) {
        timerDisposable?.dispose()
        if (isReceivingLocationUpdates) {
            timerDisposable = Observable.interval(1L, TimeUnit.SECONDS)
                .repeat()
                .call {
                    receivingLocationUpdatesTimer.postValue(it % 2 == 0L)
                }
        }
        else receivingLocationUpdatesTimer.postValue(false)
    }

    override fun onCleared() {
        super.onCleared()
        stopLocationUpdates()
        _locationLiveData.value = null
        receivingLocationUpdates.value = null
        receivingLocationUpdatesTimer.value = null
    }
}