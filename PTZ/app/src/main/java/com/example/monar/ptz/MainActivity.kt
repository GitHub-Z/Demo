/*
package com.example.monar.ptz

import android.annotation.SuppressLint
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {
    private var ptzDisplay = false
    private var cmdDataStr = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Video(gUrl.videolUrl,VD0,this).videoPlay()
        KeepAlive().keepAlive()
*/
/*        val intent = Intent(this,JavaActivity::class.java)
        intent.putExtra("videoUrl", gUrl.videolUrl)
        startActivity(intent)*//*

        bt_ptz_show.setOnClickListener {
            when (ptzDisplay) {
                true -> {
                    ptzlayout.visibility = View.INVISIBLE;ptzDisplay = false
                }
                false -> {
                    ptzlayout.visibility = View.VISIBLE;ptzDisplay = true
                }
            }
        }
        class PtzTouch : View.OnTouchListener{
            @SuppressLint("ClickableViewAccessibility")
            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                when(event?.action) {
                     MotionEvent.ACTION_DOWN-> onClick(v)
                     MotionEvent.ACTION_UP-> onTouchUp()
                    else->onTouchUp()
                    }
                return true
            }
        }
        ptz_up.setOnTouchListener(PtzTouch())
        ptz_down.setOnTouchListener(PtzTouch())
        ptz_left.setOnClickListener {
            onClick(ptz_left)
        }
        ptz_right.setOnClickListener {
            onClick(ptz_right)
        }

    }

    fun onClick(v: View?) {
        when (v) {
            ptz_up -> {
                ptzMove(gVar.Ptz().zoomIn)
            }
            ptz_down -> {
                ptzMove(gVar.Ptz().zoomOut)
            }
            ptz_left -> {
            }
        }
    }

    fun onTouchUp(){
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
            else->{
                cmdDataStr = "{header:{version:101,seq:0,peer_type:4001,local_version:0,peer_id:ffffffffffffffff0000000000000001,session_id:\"${gDev.session}\",tt:\"${TokenGenerator().generateTt()}\",cc:\"$cc\"}," +
                        "body:{cmd:1027,channel:0,control:\"${gVar.Ptz().stopAll}\",pan_speed:50,tilt_speed:50,zoom_speed:30}}"
            }
        }
        //HttpThread(gUrl.protocolUrl,cmdDataStr).start()
        HttpPost(gUrl.protocolUrl,cmdDataStr).start()
    }


}
*/
