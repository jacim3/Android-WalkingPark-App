package com.example.walkingpark.ui.service

import android.Manifest
import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.*
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import com.example.walkingpark.R
import com.example.walkingpark.constants.Common
import com.example.walkingpark.constants.Settings
import com.example.walkingpark.data.model.entity.LocationObject
import com.example.walkingpark.ui.MainActivity
import com.google.android.gms.location.*
import com.google.android.gms.tasks.CancellationToken
import com.google.android.gms.tasks.CancellationTokenSource
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.subjects.PublishSubject

@AndroidEntryPoint
class LocationService : LifecycleService() {

    private val binder = LocalBinder()

    private lateinit var locationRequest: LocationRequest
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var serviceHandler: Handler

    private val locationSubject = PublishSubject.create<LocationObject>()
    private lateinit var locationFlowable:Flowable<LocationObject>

    override fun onBind(intent: Intent): IBinder {
        super.onBind(intent)
        startForeground(2, setForegroundNotification(this))
        return binder
    }

    override fun onCreate() {
        super.onCreate()

        locationFlowable = locationSubject.toFlowable(BackpressureStrategy.BUFFER)
            .doOnSubscribe { startLocationUpdate(this) }
            .doOnCancel { stopLocationUpdate() }

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        // smallestDisplacement = SMALLEST_DISPLACEMENT_100_METERS // 100 meters
        locationRequest = LocationRequest.create().apply {
            interval = Settings.LOCATION_UPDATE_INTERVAL
            fastestInterval = Settings.LOCATION_UPDATE_INTERVAL_FASTEST
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
        locationCallback =
            object : LocationCallback() {
                override fun onLocationResult(result: LocationResult) {
                    super.onLocationResult(result)
                    result.locations.forEach(::setLocation)
                }
            }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        // LocationRequest ??? Callback ????????? ?????? ????????? ?????? (?????????) ?????? ??????
        // ???????????? ?????? ?????? ????????? ?????? ?????? ??????
        when (intent?.getStringExtra("intent-filter")) {
            // ????????? ?????? ??????(?????????) ??????
            Common.REQUEST_LOCATION_INIT -> {
                startLocationInit(this)
                sendBroadcast(Intent().apply { action = Common.REQUEST_LOCATION_UPDATE_START })
            }

            // ????????? ???????????? ??????
            Common.REQUEST_LOCATION_UPDATE_START -> {
                //startLocationUpdate(this)
                //sendBroadcast(Intent().apply { action })
            }

            // ????????? ?????? ??????
            Common.REQUEST_LOCATION_UPDATE_CANCEL -> {

            }
        }
        /*
            1. START_STICKY = Service ??? ???????????? ??? null intent ??????
            2. START_NOT_STICKY = Service ??? ??????????????? ??????
            3. START_REDELIVER_INTENT = Service ??? ???????????? ??? ????????? ???????????? intent ??????
        */
        return super.onStartCommand(intent, flags, START_NOT_STICKY)
    }

    @SuppressLint("MissingPermission")
    private fun startLocationInit(context: Context) {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.e("ParkMapsService::class", "????????? ?????? ??????")
            return
        } else {
            val src = CancellationTokenSource()
            val ct: CancellationToken = src.token
            fusedLocationProviderClient.getCurrentLocation(
                LocationRequest.PRIORITY_HIGH_ACCURACY,
                ct
            ).addOnFailureListener {
                Log.e("fusedLocationProvider", "fail")
            }.addOnSuccessListener {
                Log.e("fusedLocationProvider", "${it.latitude} ${it.longitude}")

                // parsingAddressMap(context, it.latitude, it.longitude)

            }
        }
    }

    fun getLocationCallback(locationCallback: LocationCallback) {
        this.locationCallback = locationCallback
    }

    // ???????????? ?????? ???????????? ??????
    @SuppressLint("MissingPermission")
    private fun startLocationUpdate(
        context: Context
    ) {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.e("ParkMapsService", "????????? ?????? ??????")
            return
        }

        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        ).addOnCompleteListener {
            Log.e("LocationServiceRepository : ", "LocationUpdateCallbackRegistered.")
        }
    }

    private fun stopLocationUpdate() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
    }

    private fun setLocation(location: Location) {
        locationSubject.onNext(
            LocationObject(
                location.latitude,
                location.longitude,
                location.time
            )
        )
    }

    fun getLocationFlowable() = locationFlowable

    // ??????????????? ???????????? ????????? UI ??? Notification ?????? ?????????.
    private fun setForegroundNotification(context: Context): Notification {

        val locationTrackNotification = NotificationCompat.Builder(context, "default").apply {
            setContentTitle(Common.DESC_TITLE_LOCATION_NOTIFICATION)
            setContentText(Common.DESC_TEXT_LOCATION_NOTIFICATION)
            setSmallIcon(R.drawable.ic_launcher_foreground)
        }

        // ???????????? ?????? Notification ??????
        val notificationIntent = Intent(context, MainActivity::class.java)
        val pendingIntent =
            PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE)
        locationTrackNotification.setContentIntent(pendingIntent)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager: NotificationManager =
                context.getSystemService(Service.NOTIFICATION_SERVICE) as NotificationManager
            /*
                1. IMPORTANCE_HIGH = ???????????? ????????? ????????? ???????????? ??????
                2. IMPORTANCE_DEFAULT = ????????? ??????
                3. IMPORTANCE_LOW = ????????? ??????
                4. IMPORTANCE_MIN = ????????? ?????? ????????? ?????? X
            */
            manager.createNotificationChannel(
                NotificationChannel(
                    "default", "?????? ??????",
                    NotificationManager.IMPORTANCE_LOW
                )
            )
        }
        return locationTrackNotification.build()
    }

    inner class LocalBinder : Binder() {
        internal val service: LocationService
            get() = this@LocationService
    }
}