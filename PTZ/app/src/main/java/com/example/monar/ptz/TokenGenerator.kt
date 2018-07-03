package com.example.monar.ptz

import java.util.*
import javax.crypto.Mac
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec


/**
* Created by monarch on 2018/3/14.
*/
class TokenGenerator {
    private fun getBasicStr(tt:String,ss:String):String{
        return tt + "=" + ss
    }
    fun generateTt():String{
        val time = Calendar.getInstance(TimeZone.getDefault())
        val year = time.get(Calendar.YEAR).toString()
        val month = when(time.get(Calendar.MONTH).toString().length) {
            1 -> {
                "0" + (time.get(Calendar.MONTH)+1).toString()
            }
            2 -> {
                (time.get(Calendar.MONTH)+1).toString()
            }
            else -> {
                "00"
            }
        }
        val day = when(time.get(Calendar.DAY_OF_MONTH).toString().length) {
            1 -> {
                "0" + time.get(Calendar.DAY_OF_MONTH).toString()
            }
            2 -> {
                time.get(Calendar.DAY_OF_MONTH).toString()
            }
            else -> {
                "00"
            }
        }
        val hour = when(time.get(Calendar.HOUR_OF_DAY).toString().length) {
            1 -> {
                "0" + time.get(Calendar.HOUR_OF_DAY).toString()
            }
            2 -> {
                time.get(Calendar.HOUR_OF_DAY).toString()
            }
            else -> {
                "00"
            }
        }
        val minute = when(time.get(Calendar.MINUTE).toString().length) {
            1 -> {
                "0" + time.get(Calendar.MINUTE).toString()
            }
            2 -> {
                time.get(Calendar.MINUTE).toString()
            }
            else -> {
                "00"
            }
        }
        val second = when(time.get(Calendar.SECOND).toString().length) {
            1 -> {
                "0" + time.get(Calendar.SECOND).toString()
            }
            2 -> {
                time.get(Calendar.SECOND).toString()
            }
            else -> {
                "00"
            }
        }
        return year+month+day+hour+minute+second
    }

    object HmacSha1 {
        private val MAC_NAME = "HmacSHA1"
        private val ENCODING = "UTF-8"

        @Throws(Exception::class)
        fun hMacSha1Encrypt(encryptText: String, encryptKey: String): String {
            val data = encryptKey.toByteArray(charset(ENCODING))
            val secretKey: SecretKey = SecretKeySpec(data, MAC_NAME)
            val mac = Mac.getInstance(MAC_NAME)
            mac.init(secretKey)
            val text = encryptText.toByteArray(charset(ENCODING))
            val a = mac.doFinal(text)
            val hmacSha1StrBuff = StringBuffer(gHmanShaLength*2+1)
            return hmacSha1StrBuff.toString()
        }
    }
    fun getCc():String{
        return HmacSha1.hMacSha1Encrypt(getBasicStr(generateTt(), gDev.session), gEncryptKey)
    }
}