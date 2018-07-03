package com.example.monar.ptz

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.TargetApi
import android.content.pm.PackageManager
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.view.View
import android.content.Intent
import android.os.*
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.text.SpannableStringBuilder
import android.util.Base64
import android.util.Log
import kotlinx.android.synthetic.main.activity_login.*
import org.json.JSONObject
import java.util.*

/**
 * A login screen that offers login via email/password.
 */
private const val ERRMSG = "Incorrect QRCode or QRCode type"
var handleThread :HandleThread? = null
var isScanBack = false
private var ipInfoSplit = listOf<String>()
private var isQrIncorrect = false
private var ipEdit = SpannableStringBuilder()
private var nameEdit = SpannableStringBuilder()
private var passwordEdit = SpannableStringBuilder()

private val appPermission: Array<String> = arrayOf(
        android.Manifest.permission.ACCESS_COARSE_LOCATION,
        android.Manifest.permission.ACCESS_FINE_LOCATION,
        android.Manifest.permission.ACCESS_WIFI_STATE,
        android.Manifest.permission.ACCESS_NETWORK_STATE,
        android.Manifest.permission.ACCESS_CHECKIN_PROPERTIES,
        android.Manifest.permission.CAMERA,
        android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
        //android.Manifest.permission.KILL_BACKGROUND_PROCESSES,
        android.Manifest.permission.INTERNET
)
private val appPermissionNumber = appPermission.size

class LoginActivity : AppCompatActivity(){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        check(appPermission)
        if (handleThread == null){
            handleThread = HandleThread()
            handleThread?.start()
        }
        LoginTip().tips()
        if (isScanBack){
            val ipData = intent.getStringExtra("qrData")
            var ipDataStr = ipData.toString()
            if (ipDataStr.length<0){
                isQrIncorrect = true
                ipDataStr = "aW5jb3JyZWN0IFFSQ29kZSB0eXBl"
            }
            else{
                isQrIncorrect = false
            }
            try {
                ipDataStr = Base64.decode(ipDataStr, Base64.NO_WRAP).toString(Charsets.UTF_8)
                ipInfoSplit = ipDataStr.split("@")
            }
            catch (e:Exception){
                e.printStackTrace()
                isQrIncorrect = true
                Log.e("Err",e.message)
            }
            try {
                ipEdit = SpannableStringBuilder(ipInfoSplit[0])
                nameEdit = SpannableStringBuilder(ipInfoSplit[1])
                passwordEdit = SpannableStringBuilder(ipInfoSplit[2])
                password.requestFocus()
            }
            catch (e:Exception){
                e.printStackTrace()
                Log.e("deviceinfo",e.message)
                ipEdit = SpannableStringBuilder("")
                nameEdit = SpannableStringBuilder("")
                passwordEdit = SpannableStringBuilder("")
            }
            if (isQrIncorrect){
                deviceIp.hint = ERRMSG
            }
            else{
                deviceIp.text = ipEdit
                email.text = nameEdit
                password.text = passwordEdit
            }

            isScanBack = false
        }
        ipscan.setOnClickListener{
            val intent = Intent(this,QrCode::class.java)
            startActivity(intent)
        }
        email_sign_in_button.setOnClickListener { attemptLogin() }
    }

    private var mAuthTask: UserLoginTask? = null
    inner class LoginTip {
        fun tips(){
            val tip = Timer("tips", false)
            tip.schedule(Tips(tip),30, 100)
        }
        inner class Tips(tips:Timer) : TimerTask() {
            private val tmTip = tips
            override fun run() {
                runOnUiThread{isNeedRun(tmTip)}
            }
        }
    }
    fun isNeedRun(v:Timer) {
        if (gVar.msgReceive == 1) {
            if (gVar.isStartVideoActivity) {
                finish()
                v.cancel()
                val intent = Intent()
                intent.setClass(this@LoginActivity, VideoDisplay::class.java)
                startActivity(intent)
            } else {
                when (JSONObject(gVar.callBackDataStr).getJSONObject("header").get("err_code").toString()) {
                    "1001" -> {
                        email.error = "User name incorrect"
                        email.requestFocus()
                    }
                    "1002" -> {
                        password.error = "Password incorrect"
                        password.requestFocus()
                    }
                    "1003" -> {
                        email.error = ("User name's length incorrect")
                        email.requestFocus()
                    }
                    "1004" -> {
                        password.error = ("Session id incorrect")
                        password.requestFocus()
                    }
                    "1006" -> {
                        password.error = ("Set incorrect")
                        password.requestFocus()
                    }
                    "1019" -> {
                        password.error = ("IP is refused")
                        password.requestFocus()
                    }
                    else -> {
                        //password.error = ("exception error occurred")
                    }
                }
            }
            gVar.msgReceive = 0
        }
    }

    private fun check(v: Array<String>) {
        var vv = v
        var it = vv.toMutableList().iterator()
        while (it.hasNext()) {
            var permission = ContextCompat.checkSelfPermission(this, it.next())
            if (permission == PackageManager.PERMISSION_GRANTED) {
                it.remove()
            }
        }
        if (vv.isEmpty()) {
            return
        }
        ActivityCompat.requestPermissions(this, vv, appPermissionNumber)
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    inner class UserLoginTask internal constructor(private val mUser: String, private val mPassword: String) : AsyncTask<Void, Void, Boolean>() {

        override fun doInBackground(vararg params: Void): Boolean? {
            try {
                val tt:String = TokenGenerator().generateTt()
                val strData = "{header:{version:101,seq:0,peer_type:4001,local_version:0,peer_id:ffffffffffffffff0000000000000001,session_id:\"\",tt:\"$tt\",cc:\"\" }," +
                        "body:{cmd:1001,user:\"$mUser\",password:\"${SecureMd5().getMD5Str(mPassword)}\"}}"
                val postCmd = HttpPost()
                postCmd.initParams(gUrl.protocolUrl,strData)
                Thread(postCmd).start()
            } catch (e: InterruptedException) {
                return false
            }
            return true
        }

        override fun onPostExecute(success: Boolean?) {
            mAuthTask = null
            if (success!!) {
                //showProgress(false)
                //finish()
            } else {
                password.error = "Connect failed network unreachable"
                password.requestFocus()
            }
        }

        override fun onCancelled() {
            showProgress(false)
            mAuthTask = null
        }
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private fun attemptLogin() {
        if (mAuthTask != null) {
            return
        }
        // Reset errors.
        email.error = null
        password.error = null

        // Store values at the time of the login attempt.
        val emailStr = email.text.toString()
        val passwordStr = password.text.toString()
        val deviceIpStr = deviceIp.text.toString()

        var cancel = false
        var focusView: View? = null

        if (TextUtils.isEmpty(deviceIpStr)){
            deviceIp.error = "Device IP cannot be empty"
            focusView = deviceIp
            cancel = true
        }

        if (TextUtils.isEmpty(emailStr)){
            email.error = "User name cannot be empty"
            focusView = email
            cancel = true
        }
        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(passwordStr)) {
            password.error = "User password cannot be empty"
            focusView = password
            cancel = true
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView?.requestFocus()
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            //showProgress(true)
            gUrl.manualInit(deviceIpStr)
            mAuthTask = UserLoginTask(emailStr, passwordStr)
            mAuthTask!!.execute(null as Void?)
        }
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private fun showProgress(show: Boolean) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            val shortAnimTime = resources.getInteger(android.R.integer.config_shortAnimTime).toLong()

            login_form.visibility = if (show) View.GONE else View.VISIBLE
            login_form.animate()
                    .setDuration(shortAnimTime)
                    .alpha((if (show) 0 else 1).toFloat())
                    .setListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator) {
                            login_form.visibility = if (show) View.GONE else View.VISIBLE
                        }
                    })

            login_progress.visibility = if (show) View.VISIBLE else View.GONE
            login_progress.animate()
                    .setDuration(shortAnimTime)
                    .alpha((if (show) 1 else 0).toFloat())
                    .setListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator) {
                            login_progress.visibility = if (show) View.VISIBLE else View.GONE
                        }
                    })
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            login_progress.visibility = if (show) View.VISIBLE else View.GONE
            login_form.visibility = if (show) View.GONE else View.VISIBLE
        }
    }
}