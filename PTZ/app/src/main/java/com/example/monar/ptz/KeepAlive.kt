package com.example.monar.ptz

import java.util.*

/**
* Created by monarch on 2018/3/16.
*/

class KeepAlive {
    private val cc = TokenGenerator().getCc()
    private val cmdDataStr = "{header:{version:101,seq:0,peer_type:4001,local_version:0,peer_id:ffffffffffffffff0000000000000001,session_id:\"${gDev.session}\",tt:\"${TokenGenerator().generateTt()}\",cc:\"$cc\"}," +
            "body:{cmd:1006}}"

    fun keepAlive(){
        val alive = Timer("keepAlive", false)
        alive.schedule(Alive(),gDelayToExecute, gPeriodToExecute)
    }
    private inner class Alive : TimerTask() {
        override fun run() {
            val cmdPost = HttpPost()
            cmdPost.initParams(gUrl.protocolUrl, cmdDataStr)
            Thread(cmdPost).start()
        }
    }
}