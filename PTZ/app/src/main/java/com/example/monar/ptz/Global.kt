package com.example.monar.ptz

import android.util.DisplayMetrics
import org.json.JSONObject
import java.util.*

/**
* Created by monarch on 2018/3/14.
*/
var gVar = Global()
var gDev = Global().DeviceInfo()
var gUsr = Global().UserInfo()
val gEncryptKey = "0da|c76h*%\$#@)(*"
val gHmanShaLength = 20
var gUrl = Global.Url()
val gDelayToExecute:Long = 10 * 1000
val gPeriodToExecute:Long = 10 * 1000
var gOutMetric = DisplayMetrics()
class Global {
    var isStartVideoActivity = false
    var callBackDataStr = ""
    var msgReceive = 0

    class Url{
        var protocolUrl = String()
        var videoUrl = String()
        init {
            protocolUrl = "http://192.168.3.38/ipnc/"
            videoUrl = "rtsp://192.168.3.38"
        }
        fun manualInit(url:String){
            protocolUrl = "http://$url/ipnc/"
            videoUrl = "rtsp://$url"
        }
    }
    inner class UserInfo{
        var userName = ""
        var passWord = ""
    }
    inner class Ptz {
        val stopAll = 0
        val stopZoom = 3001
        val stopFocus = 3002
        val stopAuto = 3003
        val zoomIn = 21
        val zoomOut = 20
        val focusNear = 22
        val focusFar = 23
        val moveUp = 1
        val moveDown = 2
        val moveLeft = 3
        val moveRight = 4
    }
    inner class DeviceInfo {
        var session:String = ""
        var userLevel:Int = -1
        var channelCnt:Int = -1
        var streamCnt:Int = -1
        var model:String = ""
        var innerModel:String = ""
        var serial:String = ""
        var mac:String = ""
        var firmwareVersion:String = ""
        var softwareVersion:String = ""
        var webVersion:String = ""
        var pluginVersion:String = ""
        var onvifVersion:String = ""
        var alarmInput:Int = -1
        var alarmOutput:Int = -1
        var audio:Int = -1
        var romDatetime:String = ""
        var hwDatetime:String = ""
        var hardDiskCnt:Int = -1
        var focusType:Int = -1
        var rs232:Int = -1
        var rs485:Int = -1
        var track:Int = -1
        var platGbPu:Int = -1
        var productType:Int = -1
        var wifi:Int = -1
        var defIp:String = ""
        var rtspPort:Int = -1
        var smartFlag:Int = -1

        fun initDevInfo(callbackData:String) {
            val x = JSONObject(callbackData)
            session = x.getString("session")
            userLevel = x.getInt("user_level")
            channelCnt = x.getInt("channel_cnt")
            streamCnt = x.getInt("stream_cnt")
            model = x.getString("model")
            innerModel = x.getString("inner_model")
            serial = x.getString("serial")
            mac = x.getString("mac")
            firmwareVersion = x.getString("firmware_version")
            softwareVersion = x.getString("software_version")
            webVersion = x.getString("web_version")
            pluginVersion = x.getString("plugin_version")
            onvifVersion = x.getString("onvif_version")
            alarmInput = x.getInt("alarm_input")
            alarmOutput = x.getInt("alarm_output")
            audio = x.getInt("audio")
            romDatetime = x.getString("rom_datetime")
            hwDatetime = x.getString("hw_datetime")
            hardDiskCnt = x.getInt("harddisk_cnt")
            focusType = x.getInt("focustype")
            rs232 = x.getInt("rs232")
            rs485 = x.getInt("rs485")
            track = x.getInt("track")
            platGbPu = x.getInt("platgbpu")
            productType = x.getInt("product_type")
            wifi = x.getInt("wifi")
            defIp = x.getString("def_ip")
            rtspPort = x.getInt("rtsp_port")
            smartFlag = x.getInt("smartflag")
        }
    }
}