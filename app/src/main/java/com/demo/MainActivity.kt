package com.demo

import android.Manifest
import android.annotation.SuppressLint
import android.content.IntentSender
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.PendingResult
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsResult
import com.google.android.gms.location.LocationSettingsStatusCodes
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    companion object {
        private val TAG = MainActivity::class.java.simpleName
        private const val MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1
        private const val REQUEST_CHECK_SETTINGS = 199
    }

    private lateinit var viewModel: LocationViewModel

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewModel = ViewModelProvider(this)[LocationViewModel::class.java]
        viewModel.initLocation()

        viewModel.locationDetails.observe(this, Observer {
            progress_circular.visibility= View.GONE
            Log.i(TAG, "lat: ${it.latitude}")
            Log.i(TAG, "long: ${it.longitude}")
            tv.text=" ${it.latitude} -  ${it.longitude} "
        })

        checkPermission()
    }



    override fun onResume() {
        super.onResume()
        enableGps()
    }

    override fun onPause() {
        super.onPause()
        viewModel.stopLocationUpdates()
    }

    private fun enableGps() {
        val googleApiClient = GoogleApiClient.Builder(this).addApi(LocationServices.API).build()
        googleApiClient.connect()
        val builder = LocationSettingsRequest.Builder().addLocationRequest(viewModel.locationRequest)
        builder.setAlwaysShow(true)
        val result: PendingResult<LocationSettingsResult> = LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build())
        result.setResultCallback { result ->
            val status: Status = result.status
            when (status.statusCode) {
                LocationSettingsStatusCodes.SUCCESS ->
                    try {
                        viewModel.startLocationUpdates()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                LocationSettingsStatusCodes.RESOLUTION_REQUIRED ->
                    try {
                        status.startResolutionForResult(this, REQUEST_CHECK_SETTINGS)
                    } catch (e: IntentSender.SendIntentException) { }
                LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {
                }
            }
        }
    }

    private fun checkPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION)
            return
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    Log.i(TAG, "Permission was granted for Location")
                    viewModel.startLocationUpdates()
                } else {
                    Log.i(TAG, "Permission was denied for Location")
                    checkPermission()
                }
                return
            }
        }
    }
}
