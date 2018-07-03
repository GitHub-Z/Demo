package com.example.monar.ptz

import android.content.Context
import android.location.LocationManager
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.support.v7.app.AppCompatActivity
import android.util.Log

/**
 * Created by monar on 2018/3/14.
 */
class wifi : AppCompatActivity(){
    fun wifilist(): String {
        var localinfo = applicationContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (!localinfo.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Log.d("TagGPS", "GPS is set off now")
        }
        var list: List<ScanResult>
        var mWifi =applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        if (!mWifi.isWifiEnabled) {
            Log.d("WIFIMSG", "Opening wifi")
            mWifi.isWifiEnabled = true
        }
        if (mWifi.isWifiEnabled) {
            mWifi.startScan()
            list = mWifi.scanResults
        } else {
            list = listOf()
        }
        return list.toString()
    }
}