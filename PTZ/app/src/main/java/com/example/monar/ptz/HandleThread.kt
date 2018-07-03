package com.example.monar.ptz

import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import org.json.JSONObject


/**
Created by monarch on 2018/3/18.
 */
class HandleThread : Thread(){
    var loginHandler = Handler()
    override fun run() {
        super.run()
        Looper.prepare()
        loginHandler = object : Handler(){
            override fun handleMessage(msg: Message) {
                super.handleMessage(msg)
                if (msg.arg1 != 1006)
                    Log.d("CMD callBack","${msg.what}${msg.arg1}${msg.obj}")
                gVar.msgReceive = 1
                if (msg.what == 1) {
                    val strHeadJson = JSONObject(msg.obj.toString()).getJSONObject("header")
                    val strDataJson = JSONObject(msg.obj.toString()).getJSONObject("body")
                    if (strHeadJson.get("err_code").toString() == "0" && msg.arg1 == 1001) {
                        gVar.isStartVideoActivity = true
                        gVar.callBackDataStr = msg.obj.toString()
                        gDev.initDevInfo(strDataJson.toString())
                        //LoginActivity().runCheck()
                    } else {
                        gVar.callBackDataStr = msg.obj.toString()
                    }
                    msg.what = 0
                }
            }
        }
        Looper.loop()
    }
}