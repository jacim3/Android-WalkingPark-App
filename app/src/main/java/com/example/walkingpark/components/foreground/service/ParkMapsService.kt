package com.example.walkingpark.components.foreground.service

import android.Manifest
import android.app.*
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.example.walkingpark.MainActivity
import com.example.walkingpark.R
import com.example.walkingpark.database.singleton.Common
import com.example.walkingpark.database.singleton.Settings
import com.example.walkingpark.database.singleton.UserData
import com.google.android.gms.location.*
import com.google.android.gms.tasks.CancellationToken
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ParkMapsService : Service() {

    //    // 지오펜싱
//    lateinit var gpsTracker:GpsTracker
//    override fun onBind(intent: Intent): IBinder {
//
//    }

    private val mBinder: IBinder = LocalBinder()
    var number: Int = 0
        get() = field + 1
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationTrackNotification: NotificationCompat.Builder
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback

    val thisService: ParkMapsService = this

    class LocalBinder : Binder() {
        fun getService(): ParkMapsService {
            return ParkMapsService().thisService
        }
    }

    override fun onCreate() {
        super.onCreate()
        Log.e("ParkMapsService", "onCreate")

        //Log.e("asdfd","onBind()")
        locationTrackNotification = NotificationCompat.Builder(this, "default").apply {
            setContentTitle(Common.DESC_TITLE_LOCATION_NOTIFICATION)
            setContentText(Common.DESC_TEXT_LOCATION_NOTIFICATION)
            setSmallIcon(R.drawable.ic_launcher_foreground)
        }


        CoroutineScope(Dispatchers.Default).launch {

        }

        setLocationRequest()
        setLocationCallback()

        number = 0
    }

    // 컴포넌트가 서비스에 바인딩하고자 할때 수행
    // 클라이언트가 서비스와 통신을 수고받기 위해 사용할 인터페이스를 여기서 제공해야 한다.
    override fun onBind(sIntent: Intent?): IBinder {
        Log.e("ParkMapsService", "onBind")

        // 위치추적 관련 Notification 생성
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE)
        } else {
            PendingIntent.getActivity(this, 0, notificationIntent, 0)
        }

        locationTrackNotification.setContentIntent(pendingIntent)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager: NotificationManager =
                getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            /*
                1. IMPORTANCE_HIGH = 알림음이 울리고 헤드업 알림으로 표시
                2. IMPORTANCE_DEFAULT = 알림음 울림
                3. IMPORTANCE_LOW = 알림음 없음
                4. IMPORTANCE_MIN = 알림음 없고 상태줄 표시 X
            */
            manager.createNotificationChannel(
                NotificationChannel(
                    "default", "기본 채널",
                    NotificationManager.IMPORTANCE_LOW
                )
            )
        }
        startForeground(2, locationTrackNotification.build())
        return mBinder
    }

    // fusedLocationClient 객체를 초기화 하며, 사용자 위치정보 찾기 수행
    private fun searchLocation() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this@ParkMapsService)

        if (ActivityCompat.checkSelfPermission(
                this@ParkMapsService,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this@ParkMapsService,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.e("ParkMapsService::class", "퍼미션 허용 안됨")
        }

        val src = CancellationTokenSource()
        val ct: CancellationToken = src.token
        fusedLocationClient.getCurrentLocation(
            LocationRequest.PRIORITY_HIGH_ACCURACY,
            ct
        ).addOnFailureListener {
            Log.e("fusedLocationProvider", "fail")
        }.addOnSuccessListener {
            Log.e("fusedLocationProvider", "${it.latitude} ${it.longitude}")

            // BroadCastReceiver 콜백이 호출되었음을 알리기
            val requestIntent = Intent()
            requestIntent.action = Common.REQUEST_ACTION_UPDATE
            sendBroadcast(requestIntent)

            UserData.currentLatitude = it.latitude
            UserData.currentLongitude = it.longitude
        }
    }

    // Location 설정 변경
    private fun setLocationRequest() {
        locationRequest = LocationRequest.create().apply {
            interval = Settings.LOCATION_UPDATE_INTERVAL
            fastestInterval = Settings.LOCATION_UPDATE_INTERVAL_FASTEST
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
    }

    // 로케이션 콜백 등록
    private fun setLocationCallback() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                super.onLocationResult(result)
                Log.e("LocationCallback", "OnLocationResult")
                Log.e(
                    "LocationCallback",
                    "${result.lastLocation.latitude} ${result.lastLocation.longitude}"

                )
                UserData.currentLatitude = result.lastLocation.latitude
                UserData.currentLongitude = result.lastLocation.longitude
            }

            override fun onLocationAvailability(response: LocationAvailability) {
                super.onLocationAvailability(response)
                Log.e("LocationCallback", "onLocationAvailability")
                Log.e("LocationCallback", "${response.isLocationAvailable}")

            }
        }
    }

    private fun updateLocation() {

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.e("ParkMapsService", "퍼미션 허용 안됨")
            return
        }

        CoroutineScope(Dispatchers.Default).launch {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            ).addOnCompleteListener { Log.e("Completed", "Completed") }
        }
    }

    fun stopUpdateLocation() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    // TODO 구성변경에 따라 변동이 일어날 경우, Bundle에 저장
/*    override fun onSaveInstanceState(outState: Bundle?) {
        outState?.putBoolean(REQUESTING_LOCATION_UPDATES_KEY, requestingLocationUpdates)
        super.onSaveInstanceState(outState)
    }*/

    // 서비스 시작 콜백
    // 컴포넌트가 서비스 사용을 요청할때마다 수행된다.
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.e("ParkMapsService", "onStartCommand()")

        val requestCode = intent!!.getIntExtra("requestCode", -1)

        if (requestCode != -1) {

            when (requestCode) {
                Common.PERMISSION -> {
                    searchLocation()
                }
                Common.LOCATION_UPDATE -> {
                    updateLocation()
                }
                Common.LOCATION_UPDATE_CANCEL -> {
                    stopUpdateLocation()
                }
                Common.LOCATION_SETTINGS -> {

                }
            }
        }
        /*
            1. START_STICKY = Service 가 재시작될 때 null intent 전달
            2. START_NOT_STICKY = Service 가 재시작되지 않음
            3. START_REDELIVER_INTENT = Service 가 재시작될 때 이전에 전달했던 intent 전달
        */
        return START_STICKY

    }

    override fun onDestroy() {
        super.onDestroy()
    }
}