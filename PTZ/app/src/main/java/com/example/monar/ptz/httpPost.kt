package com.example.monar.ptz

import android.os.Handler
import android.os.Looper
import android.os.Message
import android.text.TextUtils
import android.util.Log
import org.json.JSONObject


import java.io.BufferedOutputStream
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.UnsupportedEncodingException
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL



/**
* Created by monarch on 2018/3/15.
*/

/**
 * http post 请求
 * @param urlStr       请求url
 * @param strData    post参数
 * @return          HttpResponse请求结果实例
 */
private var strData = ""
private var urlStr = ""
class HttpPost :Runnable/*: Thread() */{
    private class Response {
        var code: Int? = null
        var content: String? = null
    }
    private fun readStream(connection: HttpURLConnection): Response {
        val response = Response()

        val stringBuilder = StringBuilder()

        var reader: BufferedReader? = null
        try {

            reader = BufferedReader(InputStreamReader(
                    connection.inputStream,Charsets.UTF_8))
            reader.forEachLine {
                stringBuilder.append(it)
            }

            response.code = connection.responseCode
            response.content = stringBuilder.toString()
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
            response.code = -1
            response.content = e.message
        } catch (e: IOException) {
            e.printStackTrace()

            try {
                //it could be caused by 400 and so on

                reader = BufferedReader(InputStreamReader(
                        connection.errorStream, "UTF-8"))

                //clear
                stringBuilder.setLength(0)

                val tmp = reader.read()
                while (tmp != -1) {
                    stringBuilder.append(tmp.toChar())
                }

                response.code = connection.responseCode
                response.content = stringBuilder.toString()

            } catch (e1: IOException) {
                response.content = e1.message
                response.code = -1
                e1.printStackTrace()
            } catch (ex: Exception) {
                //if user directly shuts down network when trying to write to server
                //there could be NullPointerException or SSLException
                response.content = ex.message
                response.code = -1
                ex.printStackTrace()
            }

        } finally {
            if (reader != null) {
                try {
                    reader.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }

            }
        }
        return response
    }

    //return null means successfully write to server
    private fun writeStream(connection: HttpURLConnection, content: String): Response? {
        var out: BufferedOutputStream? = null
        var response: Response? = null
        try {
            out = BufferedOutputStream(connection.outputStream)
            out.write(content.toByteArray(charset("UTF-8")))
            out.flush()
        } catch (e: IOException) {
            e.printStackTrace()

            try {
                //it could be caused by 400 and so on
                response = Response()

                val reader = BufferedReader(InputStreamReader(
                        connection.errorStream, "UTF-8"))

                val stringBuilder = StringBuilder()

                val tmp = reader.read()
                while (tmp != -1) {
                    stringBuilder.append(tmp.toChar())
                }

                response.code = connection.responseCode
                response.content = stringBuilder.toString()

            } catch (e1: IOException) {
                response = Response()
                response.content = e1.message
                response.code = -1
                e1.printStackTrace()
            } catch (ex: Exception) {
                //if user directly shutdowns network when trying to write to server
                //there could be NullPointerException or SSLException
                response = Response()
                response.content = ex.message
                response.code = -1
                ex.printStackTrace()
            }

        } finally {
            try {
                if (out != null)
                    out.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }

        }
        return response
    }

    private fun send(){
        if (TextUtils.isEmpty(strData) || TextUtils.isEmpty(urlStr) || strData == "" || urlStr == ""){
            return
        }
        var response: Response?
        var httpURLConnection: HttpURLConnection? = null
        val transData = JSONObject(strData)
        val isLogin = transData.getJSONObject("body").get("cmd").toString()
        if (isLogin == "1001") {
            gUsr.userName = transData.getJSONObject("body").getString("user")
            gUsr.passWord = transData.getJSONObject("body").getString("password")
        }
        try {
            val urlObj = URL(urlStr)

            httpURLConnection = urlObj.openConnection() as HttpURLConnection

            httpURLConnection.requestMethod = "POST"
            httpURLConnection.connectTimeout = 6000
            httpURLConnection.readTimeout = 6000
            httpURLConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8")
            httpURLConnection.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64; Trident/7.0; rv:11.0) like Gecko")
            httpURLConnection.addRequestProperty("Accept", "*/*")
            httpURLConnection.doOutput = true

            //start to post
            response = writeStream(httpURLConnection, transData.toString())
            if (response == null) { //if post successfully
                try {
                    response = readStream(httpURLConnection)
                    val message = Message()
                    message.obj = response.content
                    message.what = 1
                    message.arg1 = isLogin.toInt() //is login flag
                    message.arg2 = response.code as Int //err code
                    handleThread?.loginHandler?.sendMessage(message)
                }catch (e:Exception){
                    e.printStackTrace()
                    response = Response()
                    response.content = e.message
                    response.code = -1
                }
            }
        } catch (e: MalformedURLException) {
            e.printStackTrace()

            response = Response()
            response.content = e.message
            response.code = -1
        } catch (e: IOException) {
            e.printStackTrace()

            response = Response()
            response.content = e.message
            response.code = -1
        } catch (ex: Exception) {
            ex.printStackTrace()
            response = Response()
            response.content = ex.message
            response.code = -1
        } finally {
            if (httpURLConnection != null)
                httpURLConnection.disconnect()
        }
    }

    fun initParams(purl:String,data:String){
        urlStr = purl
        strData = data
    }

    override fun run() {
        try {
            send()
        }catch (e:Exception){
            e.printStackTrace()

            var response = Response()
            response.content = e.message
            response.code = -1
        }
    }
}
