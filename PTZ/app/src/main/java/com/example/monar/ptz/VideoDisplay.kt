package com.example.monar.ptz

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import kotlinx.android.synthetic.main.activity_video_display.*
import android.app.ActivityManager.RunningAppProcessInfo
import android.app.ActivityManager
import android.content.Context
import android.content.res.Configuration
import android.util.Log
import android.view.GestureDetector
import android.view.ScaleGestureDetector
import java.util.*



private var isBackToFore = false
private var landScape = false
private const val FLING_MIN_DISTANCE = 20
private const val FLING_MIN_VELOCITY = 40
private const val SCALE_MIN_VALUED = 40
private var enableGestureDetector = true
private var enableScaleGestureDetector = true

class VideoDisplay : AppCompatActivity() {
    private var ptzDisplay = false
    private var cmdDataStr = ""
    private var video:Video? = null
    private var a:GestureDetector? = null
    private var b:ScaleGestureDetector? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        windowManager.defaultDisplay.getRealMetrics(gOutMetric)
        setContentView(R.layout.activity_video_display)
        a = GestureDetector(this, SimpleGesture())
        b = ScaleGestureDetector(this, SimpleGesture())

        video = Video(gUrl.videoUrl,video_surface,this,video_surface_frame,video_surface,subtitles_stub)
        video?.videoInit()
        video?.videoStart()
        KeepAlive().keepAlive()
        VideoPlayDaemon().daemon(this)
        bt_ptz_show.setOnClickListener {
            ptzBtnShow(ptzDisplay)
        }

        class PtzTouch : View.OnTouchListener,View.OnLongClickListener{
            override fun onLongClick(v: View?): Boolean {
                onClick(v)
                return true
            }

            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                a?.onTouchEvent(event)
                b?.onTouchEvent(event)
                return when (event?.action) {
                    MotionEvent.ACTION_DOWN -> {
                        onClick(v)
                        true
                    }
                    MotionEvent.ACTION_UP -> {
                        onTouchUp()
                        true
                    }
                    else -> {
                        /* onTouchUp()*/
                        true
                    }
                }
            }
        }

        ptz_up.setOnTouchListener(PtzTouch())
        ptz_up.setOnLongClickListener(PtzTouch())
        ptz_down.setOnTouchListener(PtzTouch())
        ptz_down.setOnLongClickListener(PtzTouch())
        ptz_left.setOnTouchListener(PtzTouch())
        ptz_left.setOnLongClickListener(PtzTouch())
        ptz_right.setOnTouchListener(PtzTouch())
        ptz_right.setOnLongClickListener(PtzTouch())
        zoomin.setOnTouchListener(PtzTouch())
        zoomin.setOnLongClickListener(PtzTouch())
        zoomout.setOnTouchListener(PtzTouch())
        zoomout.setOnLongClickListener(PtzTouch())
        video_surface_frame.setOnTouchListener(PtzTouch())
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
        windowManager.defaultDisplay.getRealMetrics(gOutMetric)
        if (newConfig?.orientation == Configuration.ORIENTATION_LANDSCAPE){
            landScape = true
            window.decorView.systemUiVisibility =
                    (
                            View.SYSTEM_UI_FLAG_FULLSCREEN or
                            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                            View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    )
        }
        else
        {
            landScape = false
            window.decorView.systemUiVisibility = View.VISIBLE
        }
    }
    private fun onClick(v: View?) {
        when (v) {
            ptz_up -> {
                ptzMove(gVar.Ptz().moveUp)
            }
            ptz_down -> {
                ptzMove(gVar.Ptz().moveDown)
            }
            ptz_left -> {
                ptzMove(gVar.Ptz().moveLeft)
            }
            ptz_right -> {
                ptzMove(gVar.Ptz().moveRight)
            }
            zoomin -> {
                ptzMove(gVar.Ptz().zoomIn)
            }
            zoomout -> {
                ptzMove(gVar.Ptz().zoomOut)
            }
            video_surface_frame ->{
                Log.d("video_surface_frame","#######")
            }
            else->{
                ptzMove(gVar.Ptz().stopAll)
            }
        }
    }

    private fun onTouchUp(){
        ptzMove(gVar.Ptz().stopAll)
    }

    private fun ptzMove(v:Int) {
        val cc = TokenGenerator().getCc()
        when(v){
            gVar.Ptz().zoomIn -> {
                cmdDataStr = "{header:{version:101,seq:0,peer_type:4001,local_version:0,peer_id:ffffffffffffffff0000000000000001,session_id:\"${gDev.session}\",tt:\"${TokenGenerator().generateTt()}\",cc:\"$cc\"}," +
                        "body:{cmd:1027,channel:0,control:\"${gVar.Ptz().zoomIn}\",pan_speed:50,tilt_speed:50,zoom_speed:30}}"
            }
            gVar.Ptz().zoomOut -> {
                cmdDataStr = "{header:{version:101,seq:0,peer_type:4001,local_version:0,peer_id:ffffffffffffffff0000000000000001,session_id:\"${gDev.session}\",tt:\"${TokenGenerator().generateTt()}\",cc:\"$cc\"}," +
                        "body:{cmd:1027,channel:0,control:\"${gVar.Ptz().zoomOut}\",pan_speed:50,tilt_speed:50,zoom_speed:30}}"
            }
            gVar.Ptz().stopAll -> {
                cmdDataStr = "{header:{version:101,seq:0,peer_type:4001,local_version:0,peer_id:ffffffffffffffff0000000000000001,session_id:\"${gDev.session}\",tt:\"${TokenGenerator().generateTt()}\",cc:\"$cc\"}," +
                        "body:{cmd:1027,channel:0,control:\"${gVar.Ptz().stopAll}\",pan_speed:50,tilt_speed:50,zoom_speed:30}}"
            }
            gVar.Ptz().moveLeft -> {
                cmdDataStr = "{header:{version:101,seq:0,peer_type:4001,local_version:0,peer_id:ffffffffffffffff0000000000000001,session_id:\"${gDev.session}\",tt:\"${TokenGenerator().generateTt()}\",cc:\"$cc\"}," +
                        "body:{cmd:1027,channel:0,control:\"${gVar.Ptz().moveLeft}\",pan_speed:50,tilt_speed:50,zoom_speed:30}}"
            }
            gVar.Ptz().moveRight -> {
                cmdDataStr = "{header:{version:101,seq:0,peer_type:4001,local_version:0,peer_id:ffffffffffffffff0000000000000001,session_id:\"${gDev.session}\",tt:\"${TokenGenerator().generateTt()}\",cc:\"$cc\"}," +
                        "body:{cmd:1027,channel:0,control:\"${gVar.Ptz().moveRight}\",pan_speed:50,tilt_speed:50,zoom_speed:30}}"
            }
            gVar.Ptz().moveUp -> {
                cmdDataStr = "{header:{version:101,seq:0,peer_type:4001,local_version:0,peer_id:ffffffffffffffff0000000000000001,session_id:\"${gDev.session}\",tt:\"${TokenGenerator().generateTt()}\",cc:\"$cc\"}," +
                        "body:{cmd:1027,channel:0,control:\"${gVar.Ptz().moveUp}\",pan_speed:50,tilt_speed:50,zoom_speed:30}}"
            }
            gVar.Ptz().moveDown -> {
            cmdDataStr = "{header:{version:101,seq:0,peer_type:4001,local_version:0,peer_id:ffffffffffffffff0000000000000001,session_id:\"${gDev.session}\",tt:\"${TokenGenerator().generateTt()}\",cc:\"$cc\"}," +
                    "body:{cmd:1027,channel:0,control:\"${gVar.Ptz().moveDown}\",pan_speed:50,tilt_speed:50,zoom_speed:30}}"
        }
            else->{
                cmdDataStr = "{header:{version:101,seq:0,peer_type:4001,local_version:0,peer_id:ffffffffffffffff0000000000000001,session_id:\"${gDev.session}\",tt:\"${TokenGenerator().generateTt()}\",cc:\"$cc\"}," +
                        "body:{cmd:1027,channel:0,control:\"${gVar.Ptz().stopAll}\",pan_speed:50,tilt_speed:50,zoom_speed:30}}"
            }
        }
        val postCmd = HttpPost()
        postCmd.initParams(gUrl.protocolUrl,cmdDataStr)
        Thread(postCmd).start()
    }

    private fun ptzBtnShow(show:Boolean){
        when(show){
            true->{
                ptz_up.visibility = View.GONE
                ptz_down.visibility = View.GONE
                ptz_left.visibility = View.GONE
                ptz_right.visibility = View.GONE
                zoomin.visibility = View.GONE
                zoomout.visibility = View.GONE
                ptzDisplay = false
            }
            false -> {
                ptz_up.visibility = View.VISIBLE
                ptz_down.visibility = View.VISIBLE
                ptz_left.visibility = View.VISIBLE
                ptz_right.visibility = View.VISIBLE
                zoomin.visibility = View.VISIBLE
                zoomout.visibility = View.VISIBLE
                ptzDisplay = true
            }
        }
    }

    private fun isBackGround(context: Context){
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val appProcesses = activityManager.runningAppProcesses
        appProcesses
        .filter { it.processName == context.packageName }
        .forEach {
            when(it.importance != RunningAppProcessInfo.IMPORTANCE_FOREGROUND){
                true-> {
                    isBackToFore = true
                    video?.videoStop()
                }
                false->{
                    if (isBackToFore){
                        video?.videoStart()
                        isBackToFore = false
                    }
                }
            }
        }
    }

    private inner class VideoPlayDaemon {
        fun daemon(context: Context){
            val videoDaemon = Timer("videoDaemon", false)
            videoDaemon.schedule(Vd(context),30, 800)
        }
        private inner class Vd(context:Context) : TimerTask() {
            private val ct = context
            override fun run() {
                isBackGround(ct)
            }
        }
    }

    class SimpleGesture : GestureDetector.SimpleOnGestureListener(),ScaleGestureDetector.OnScaleGestureListener {
        override fun onScroll(e1: MotionEvent, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
            if (landScape && enableGestureDetector) {
                enableScaleGestureDetector = false
                if (e1.x - e2.x > FLING_MIN_DISTANCE && Math.abs(velocityX) > FLING_MIN_VELOCITY) {
                    VideoDisplay().ptzMove(gVar.Ptz().moveLeft)
                    Log.i("MyGesture", "Fling left")
                } else if (e2.x - e1.x > FLING_MIN_DISTANCE && Math.abs(velocityX) > FLING_MIN_VELOCITY) {
                    VideoDisplay().ptzMove(gVar.Ptz().moveRight)
                    Log.i("MyGesture", "Fling right")
                } else if (e1.y - e2.y > FLING_MIN_DISTANCE && Math.abs(velocityY) > FLING_MIN_VELOCITY) {
                    VideoDisplay().ptzMove(gVar.Ptz().moveUp)
                    Log.i("MyGesture", "Fling up")
                } else if (e2.y - e1.y > FLING_MIN_DISTANCE && Math.abs(velocityY) > FLING_MIN_VELOCITY) {
                    VideoDisplay().ptzMove(gVar.Ptz().moveDown)
                    Log.i("MyGesture", "Fling down")
                }
                enableScaleGestureDetector = true
                return if (e2.action == MotionEvent.ACTION_UP) {
                    VideoDisplay().ptzMove(gVar.Ptz().stopAll)
                    true
                } else {
                    false
                }
            }
            return true
        }

        override fun onScale(detector: ScaleGestureDetector?): Boolean {
            val cx = detector?.currentSpanX as Float
            val px = detector?.previousSpanX as Float
            val a = cx - px
            if (landScape && enableScaleGestureDetector) {
                if (a > 0) {
                    if (Math.abs(a) - SCALE_MIN_VALUED > 0) {
                        VideoDisplay().ptzMove(gVar.Ptz().zoomIn)
                    }
                } else if (a < 0) {
                    if (Math.abs(a) - SCALE_MIN_VALUED > 0) {
                        VideoDisplay().ptzMove(gVar.Ptz().zoomOut)
                    }
                } else {
                    VideoDisplay().ptzMove(gVar.Ptz().stopAll)
                }
                return true
            }
            return true
        }
        override fun onScaleBegin(detector: ScaleGestureDetector?): Boolean {
            //VideoDisplay().ptzMove(gVar.Ptz().zoomIn)
            enableGestureDetector = false
            return true
        }

        override fun onScaleEnd(detector: ScaleGestureDetector?) {
            enableGestureDetector = true
            VideoDisplay().ptzMove(gVar.Ptz().stopAll)
        }
    }
}
