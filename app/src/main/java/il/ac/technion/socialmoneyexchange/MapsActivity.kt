package il.ac.technion.socialmoneyexchange

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Circle
import com.google.android.gms.maps.model.CircleOptions
import androidx.core.app.ComponentActivity.ExtraData
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Looper
import android.provider.Settings
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*


@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class MapsActivity : AppCompatActivity(), OnMapReadyCallback {
    private val PERMISSION_ID = 1000
    private lateinit var mMap: GoogleMap
    private var chosenLocationLat = 0.0
    private var chosenLocationLong = 0.0
    lateinit var mFusedLocationClient: FusedLocationProviderClient
    private lateinit var locationGPS:Location

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M)
            if(!checkLocatePermision())
                requestPermissions()
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == PERMISSION_ID) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                mMap.isMyLocationEnabled = true
            }
        }
    }
    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION),
            PERMISSION_ID
        )
    }

    private fun checkLocatePermision():Boolean {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            return true
        }
        return false
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
//        var finished = false
        if(checkLocatePermision()) {
//            var locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
//            var hasGPS =locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
//            if(hasGPS) {
//                println("..Im HERE")
//
//                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,5000,0f,object:LocationListener{
//                    override fun onLocationChanged(location: Location?) {
//                        println("0Im HERE")
//                        if(location!=null)
//                            locationGPS = location
//                    }
//
//                    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
//                    }
//
//                    override fun onProviderEnabled(provider: String?) {
//                    }
//
//                    override fun onProviderDisabled(provider: String?) {
//                    }
//                })
//                if(locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)!=null) {
//                    locationGPS = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
//                    println("1Im HERE")
//
//                }
////                chosenLocationLat = locationGPS.latitude
////                chosenLocationLong = locationGPS.longitude
//            }
            mFusedLocationClient.lastLocation.addOnCompleteListener(this) { task ->
                var location: Location? = task.result
                if (location == null) {
                    requestNewLocationData()
                } else {
                    chosenLocationLat = location.latitude
                    chosenLocationLong = location.longitude
                    println("FIRST"+chosenLocationLat.toString()+" "+chosenLocationLong.toString())

                }
//                finished = true
            }
        } else {
            Toast.makeText(this, "Turn on location", Toast.LENGTH_LONG).show()
            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivity(intent)
        }
        val circleOptions = CircleOptions()
            .center(LatLng(37.4, -122.1))
            .radius(100000.0) // In meters
            .strokeColor(Color.BLUE)

// Get back the mutable Circle
//        while(!finished){}
        println(chosenLocationLat.toString()+" "+chosenLocationLong.toString())
        val chosenLocation = LatLng(chosenLocationLat,chosenLocationLong)
        val circle = mMap.addCircle(circleOptions)
        mMap.setOnCircleClickListener {  }

        var marker=mMap.addMarker(MarkerOptions().position(chosenLocation))

        mMap.setOnMapClickListener({ latLng -> marker.position=latLng})

        mMap.moveCamera(CameraUpdateFactory.newLatLng(chosenLocation))
        mMap.animateCamera((CameraUpdateFactory.newLatLngZoom(chosenLocation,8f)))



    }
    private fun requestNewLocationData() {
        var mLocationRequest = LocationRequest()
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        mLocationRequest.interval = 0
        mLocationRequest.fastestInterval = 0
        mLocationRequest.numUpdates = 1

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        mFusedLocationClient!!.requestLocationUpdates(
            mLocationRequest, mLocationCallback,
            Looper.myLooper()
        )
    }
    private val mLocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            var mLastLocation: Location = locationResult.lastLocation
            chosenLocationLat = mLastLocation.latitude
            chosenLocationLong = mLastLocation.longitude
        }
    }


}
