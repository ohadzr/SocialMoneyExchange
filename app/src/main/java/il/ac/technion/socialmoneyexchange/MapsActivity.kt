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
import com.google.android.gms.maps.model.CircleOptions

import android.location.Location
import android.os.Build
import android.os.Looper
import android.provider.Settings
import android.widget.Button
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import com.google.android.material.slider.Slider


@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class MapsActivity : AppCompatActivity(), OnMapReadyCallback {
    private val PERMISSION_ID = 1000
    private val DEAFAULT_RADIUS = 25000.0
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
        if(checkLocatePermision()) {
            mFusedLocationClient.lastLocation.addOnCompleteListener(this) { task ->
                var location: Location? = task.result
                if (location == null) {
                    requestNewLocationData()
                } else {
                    chosenLocationLat = location.latitude
                    chosenLocationLong = location.longitude
                }
                mapFragment.getMapAsync(this)
            }
        } else {
            Toast.makeText(this, "Turn on location", Toast.LENGTH_LONG).show()
            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivity(intent)
            mapFragment.getMapAsync(this)
        }

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

    @SuppressLint("MissingPermission", "RestrictedApi")
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        var selectedRadius = DEAFAULT_RADIUS
        val tempRadius = intent.getStringExtra("Radius")
        if(!tempRadius.isNullOrEmpty()&&tempRadius.toDouble()!=0.0) {
            chosenLocationLat=intent.getStringExtra("Lat").toDouble()
            chosenLocationLong=intent.getStringExtra("Long").toDouble()
            selectedRadius = intent.getStringExtra("Radius").toDouble()
        }
        val chosenLocation = LatLng(chosenLocationLat,chosenLocationLong)
        val circleOptions = CircleOptions().apply {
            center(chosenLocation)
            radius(selectedRadius)
            strokeColor(Color.BLUE)

            fillColor(Color.argb(90,0,0,150))
//            clickable(true)

        }
        val myCurrency = intent.getStringExtra("PickedCurrency")
        val savedAddedCoins = intent.getStringExtra("savedAddedCoins")

        val savedPickedAmount = intent.getStringExtra("pickedAmount")
        val savedRequestedCurrencies = intent.getStringArrayListExtra("savedRequestedCurrencies")

        val circle = mMap.addCircle(circleOptions)
//        mMap.setOnCircleClickListener {circle -> circleOptions.radius = circle.radius }
        var marker=mMap.addMarker(MarkerOptions().position(chosenLocation))

        mMap.setOnMapClickListener { latLng ->
            marker.position=latLng
            circle.center = latLng
        }
        mMap.moveCamera(CameraUpdateFactory.newLatLng(chosenLocation))
        mMap.animateCamera((CameraUpdateFactory.newLatLngZoom(chosenLocation,8f)))
        val slider:Slider = findViewById(R.id.slider_map)
        slider.setOnChangeListener{slider: Slider?, value: Float ->
            circle.radius = value.toDouble()*1000.0
        }

        val button:Button = findViewById(R.id.map_done)
        button.setOnClickListener{
            val intent = Intent(this,MainActivity::class.java)
            intent.putExtra("fromMap","True")
            intent.putExtra("Radius",circle.radius.toString())
            intent.putExtra("Lat",marker.position.latitude.toString())
            intent.putExtra("Long",marker.position.longitude.toString())
            intent.putExtra("PickedCurrency",myCurrency)
            intent.putExtra("savedAddedCoins",savedAddedCoins)
            intent.putExtra("pickedAmount",savedPickedAmount)
            intent.putExtra("savedRequestedCurrencies",savedRequestedCurrencies)
//            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            startActivity(intent)
            finish()


        }





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
