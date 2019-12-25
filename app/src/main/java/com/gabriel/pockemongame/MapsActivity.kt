package com.gabriel.pockemongame

import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.app.ActivityCompat

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import java.lang.Exception

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap

    var accessLocation = 123
    var oldLocation:Location ?= null
    var location: Location? = null
    var listOfPokemons = arrayListOf<Pockemon>()
    var playerPower :Double= 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        checkPermission()
        loadPockemon()
    }


    fun checkPermission() {
        if (Build.VERSION.SDK_INT >= 33) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(
                    arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                    accessLocation
                )
                return
            }
        }
        getUserLocation()
    }

    fun getUserLocation() {
        Toast.makeText(this, "Location On!", Toast.LENGTH_SHORT).show()

        var myLocation = MyLocationListener()
        var locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        try {
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                3,
                3f,
                myLocation
            )//Fetches user location every 3 minutes
        } catch (ex: Exception) {
            Toast.makeText(
                this@MapsActivity,
                "Error at getUserLocation : ${ex.message}",
                Toast.LENGTH_LONG
            ).show()
        }

        var myThread = MyThread()
        myThread.start()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            accessLocation -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getUserLocation()
                }
                Toast.makeText(this, "We can not access your location", Toast.LENGTH_LONG).show()
            }
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
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
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
    }


    //Get user Location
    inner class MyLocationListener : LocationListener {

        constructor() {
            location = Location("Start")
            location!!.latitude = 0.0
            location!!.longitude = 0.0
        }

        override fun onLocationChanged(location: Location?) {
            this@MapsActivity.location = location
        }

        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun onProviderEnabled(provider: String?) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun onProviderDisabled(provider: String?) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }
    }



    inner class MyThread : Thread {
        constructor() : super() {
            oldLocation = Location("Start")
            oldLocation!!.latitude = 0.0
            oldLocation!!.longitude = 0.0
        }

        override fun run() {
            while (true) {
                try {
                    if (oldLocation!!.distanceTo(location) == 0f){
                        continue
                    }

                    oldLocation = location

                    runOnUiThread {
                        mMap.clear()
                        //show user location
                        val currentLocation =
                            LatLng(location!!.latitude, location!!.longitude)
                        mMap.addMarker(
                            MarkerOptions()
                                .position(currentLocation)
                                .title("Me")
                                .snippet("Power Level: $playerPower")
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.mario))
                        )
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15f))

                            //show pockemon locations
                        for(i in 0..listOfPokemons.size-1){
                            var newPockemon = listOfPokemons[i]

                            if(newPockemon.caught == false){
                                val pockemonLocation =
                                    LatLng(newPockemon.location!!.latitude, newPockemon.location!!.longitude)
                                mMap.addMarker(
                                    MarkerOptions()
                                        .position(pockemonLocation)
                                        .title(newPockemon.name)
                                        .snippet(newPockemon.des + " Power: ${newPockemon.power}")
                                        .icon(BitmapDescriptorFactory.fromResource(newPockemon.image!!))
                                )
                                if (location!!.distanceTo(newPockemon.location) <2){
                                    newPockemon.caught = true
                                    listOfPokemons[i] = newPockemon
                                    playerPower += newPockemon.power!!
                                    Toast.makeText(applicationContext, "You caught a new pockemon! Player power: $playerPower", Toast.LENGTH_LONG).show()
                                }
                            }
                        }

                    }
                    Thread.sleep(1000)
                } catch (ex: Exception) {
                    Toast.makeText(this@MapsActivity, "Error: ${ex.message}", Toast.LENGTH_LONG)
                        .show()
                }
            }
        }
    }

    fun loadPockemon() {
        listOfPokemons.add(
            Pockemon(
                "Charmander",
                "Powerful fire pockemon",
                R.drawable.charmander,
                55.0,
                -1.3101,
                36.8376
            )
        )
        listOfPokemons.add(
            Pockemon(
                "Bulbasaur",
                "Powerful earth pockemon",
                R.drawable.bulbasaur,
                35.0,
                -1.3127,
                36.8476
            )
        )
        listOfPokemons.add(
            Pockemon(
                "Squirtle",
                "Powerful water pockemon",
                R.drawable.squirtle,
                65.0,
                -1.2993,
                36.8282
            )
        )
    }

}
