package com.example.monar.ptz

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.PointF
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.util.Log
import com.libzxing.library.view.QRCodeScannerView
import com.libzxing.library.view.QRCoverView
import kotlinx.android.synthetic.main.activity_qrcodes.*

private const val PERMISSION_REQUEST_CAMERA = 0

class QrCode : AppCompatActivity() {
    private val tag = "activity_qrCodes"
    private var scanView:QRCodeScannerView? = null
    private var coverView:QRCoverView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qrcodes)
        scanView = scanner_view
        coverView = cover_view
        initView()
    }
    private fun initView() {
        scanView?.setAutofocusInterval(2000L)//自动聚焦间隔2s
        coverView?.setCoverViewScanner(320, 320)
        scanView?.setOnQRCodeReadListener { text, points ->  ////扫描结果监听处理
            Log.d(tag, "扫描结果 result -> " + text) //扫描到的内容
            judgeResult(text, points)   //【可选】判断二维码是否在扫描框中
        }
        scanView?.setOnCheckCameraPermissionListener( {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                true
            } else {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), PERMISSION_REQUEST_CAMERA)
                false
            }
        })
        scanView?.setBackCamera() //开启后置摄像头
    }

    private fun judgeResult(result: String, points: Array<PointF>) {
        val finderRect = cover_view.viewFinderRect  //接下来是处理二维码是否在扫描框中的逻辑
        Log.d("tag", "points.length = " + points.size)
        var isContain = true    //依次判断扫描结果的每个point是否都在扫描框内
        var i = 0
        val length = points.size
        while (i < length) {
            if (!finderRect.contains(points[i].x, points[i].y)) {
                isContain = false  //只要有一个不在，说明二维码不完全在扫描框中
                break
            }
            i++
        }
        if (isContain) {
            finish()
            isScanBack = true
            val intent = Intent(this, LoginActivity::class.java).putExtra("qrData", result)
            startActivity(intent)
        } else {
            Log.d(tag, "扫描失败！请将二维码图片摆放在正确的扫描区域中...")
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>,
                                            grantResults: IntArray) {
        if (requestCode != PERMISSION_REQUEST_CAMERA) {
            return
        }

        if (grantResults.size == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            scanView?.grantCameraPermission()
        }
    }
    override fun onResume() {
        super.onResume()
        scanView?.startCamera()
    }

    override fun onPause() {
        super.onPause()
        scanView?.stopCamera()
    }
}
