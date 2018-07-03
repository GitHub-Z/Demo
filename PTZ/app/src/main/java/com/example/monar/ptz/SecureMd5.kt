package com.example.monar.ptz

import java.security.MessageDigest

/**
* Created by monarch on 2018/3/14.
*/
class SecureMd5 {
    fun getMD5Str(str:String):String
    {
        val messageDigest = MessageDigest.getInstance("MD5").apply {
            reset()
            update(str.toByteArray(Charsets.UTF_8))
        }
        val byteArray = messageDigest.digest()
        val md5StrBuff = StringBuffer()
        for (i in byteArray) {
            if (Integer.toHexString(i.toInt() and 0xff).length == 1)
                md5StrBuff.append("0").append(Integer.toHexString(i.toInt()and 0xff))
            else
                md5StrBuff.append(Integer.toHexString(i.toInt() and 0xff))
        }
        return md5StrBuff.toString()
    }
}