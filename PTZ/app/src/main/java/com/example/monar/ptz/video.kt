package com.example.monar.ptz


import android.app.Application
import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.PixelFormat
import android.media.AudioManager
import android.net.Uri
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.util.DisplayMetrics
import android.util.Log
import android.view.*
import android.widget.FrameLayout
import org.videolan.libvlc.IVLCVout
import org.videolan.libvlc.LibVLC
import org.videolan.libvlc.Media
import org.videolan.libvlc.MediaPlayer
import java.util.*

/**
* Created by monarch on 2018/3/14.
*/

private val surfaceBestFit = 0
private val surfaceFitScreen = 1
private val surfaceFill = 2
private val surface16To9 = 3
private val surface4To3 = 4
private val surfaceOriginal = 5
private var enableSubtitles = true
private var currentSize = surfaceBestFit

class Video
(private val baseUrl: String, private val id: SurfaceView, private val x: Context,sf:FrameLayout,sv:SurfaceView,vs:ViewStub) :IVLCVout.OnNewVideoLayoutListener {

    private val mHandler = Handler()
    private var mOnLayoutChangeListener: View.OnLayoutChangeListener? = null
    private var mVideoHeight = 0
    private var mVideoWidth = 0
    private var mVideoVisibleHeight = 0
    private var mVideoVisibleWidth = 0
    private var mVideoSarNum = 0
    private var mVideoSarDen = 0
    private var mVideoSurfaceFrame: FrameLayout? = sf
    private var mVideoSurface: SurfaceView? = sv
    private var mSubtitlesSurface: SurfaceView? = null
    private var mLibVLC: LibVLC? = null
    private var mMediaPlayer: MediaPlayer? = null
    private var stub = vs

    fun videoStop(){
        mMediaPlayer?.stop()
    }
    fun videoStart(){
        mMediaPlayer?.play()
    }
    fun videoInit(){
        val args: ArrayList<String> = ArrayList()
        args.add("--network-caching=300")
        args.add("-v")
        args.add("--no-audio-time-stretch")
        args.add("--avcodec-skiploopfilter")
        args.add("--avcodec-skip-frame")
        args.add("2")
        args.add("--subsdec-encoding")
        args.add("--stats")

        mLibVLC = LibVLC(x,args)
        mMediaPlayer = MediaPlayer(mLibVLC)

        if (enableSubtitles) {
            //val stub = (R.id.subtitles_stub) as ViewStub
            mSubtitlesSurface = stub.inflate() as SurfaceView
            mSubtitlesSurface?.setZOrderMediaOverlay(true)
            mSubtitlesSurface?.holder?.setFormat(PixelFormat.TRANSLUCENT)
        }
        val vlcVOut = mMediaPlayer?.vlcVout
        vlcVOut?.setVideoView(id)
        if (mSubtitlesSurface != null)
            vlcVOut?.setSubtitlesView(mSubtitlesSurface)
        vlcVOut?.attachViews(this)
        val media = Media(mLibVLC,Uri.parse(baseUrl))
        media.setHWDecoderEnabled(false,false)
        mMediaPlayer?.media = media
        media.release()

        if (mOnLayoutChangeListener == null) {
            mOnLayoutChangeListener = object : View.OnLayoutChangeListener {
                private val mRunnable = Runnable { updateVideoSurfaces() }
                override fun onLayoutChange(v: View, left: Int, top: Int, right: Int,
                                            bottom: Int, oldLeft: Int, oldTop: Int, oldRight: Int, oldBottom: Int) {
                    if (left != oldLeft || top != oldTop || right != oldRight || bottom != oldBottom) {
                        mHandler.removeCallbacks(mRunnable)
                        mHandler.post(mRunnable)
                    }
                }
            }
        }
        mVideoSurfaceFrame?.addOnLayoutChangeListener(mOnLayoutChangeListener)
    }

    private fun changeMediaPlayerLayout(displayW: Int, displayH: Int) {
        /* Change the video placement using the MediaPlayer API */
        when (currentSize) {
            surfaceBestFit -> {
                mMediaPlayer?.aspectRatio = null
                mMediaPlayer?.scale = 0f
            }
            surfaceFitScreen, surfaceFill -> {
                val vTrack = mMediaPlayer?.currentVideoTrack ?: return
                val videoSwapped = vTrack.orientation == Media.VideoTrack.Orientation.LeftBottom || vTrack.orientation == Media.VideoTrack.Orientation.RightTop
                if (currentSize == surfaceFitScreen) {
                    var videoW = vTrack.width
                    var videoH = vTrack.height

                    if (videoSwapped) {
                        val swap = videoW
                        videoW = videoH
                        videoH = swap
                    }
                    if (vTrack.sarNum != vTrack.sarDen) {
                        videoW = videoW * vTrack.sarNum / vTrack.sarDen
                    }

                    val ar = videoW / videoH.toFloat()
                    val dar = displayW / displayH.toFloat()

                    val scale: Float
                    scale = when {
                        dar >= ar -> displayW / videoW.toFloat()
                    /* horizontal */
                        else -> displayH / videoH.toFloat()
                    } /* vertical */
                    mMediaPlayer?.scale = scale
                    mMediaPlayer?.aspectRatio = null
                } else {
                    mMediaPlayer?.scale = 0f
                    mMediaPlayer?.aspectRatio = if (!videoSwapped)
                        "" + displayW + ":" + displayH
                    else
                        "" + displayH + ":" + displayW
                }
            }
            surface16To9 -> {
                mMediaPlayer?.aspectRatio = "16:9"
                mMediaPlayer?.scale = 0f
            }
            surface4To3 -> {
                mMediaPlayer?.aspectRatio = "4:3"
                mMediaPlayer?.scale = 0f
            }
            surfaceOriginal -> {
                mMediaPlayer?.aspectRatio = null
                mMediaPlayer?.scale = 1f
            }
        }
    }

    private fun updateVideoSurfaces() {
        val sw = gOutMetric.widthPixels
        val sh = gOutMetric.heightPixels
        // sanity check
        if (sw * sh == 0) {
            Log.e("TAG", "Invalid surface size")
            return
        }
        mMediaPlayer?.vlcVout?.setWindowSize(sw, sh)

        var lp = mVideoSurface?.layoutParams
        if (mVideoWidth * mVideoHeight == 0) {
            /* Case of OpenGL vOuts: handles the placement of the video using MediaPlayer API */
            lp?.width = ViewGroup.LayoutParams.MATCH_PARENT
            lp?.height = ViewGroup.LayoutParams.MATCH_PARENT
            mVideoSurface?.layoutParams = lp
            lp = mVideoSurfaceFrame?.layoutParams
            lp?.width = ViewGroup.LayoutParams.MATCH_PARENT
            lp?.height = ViewGroup.LayoutParams.MATCH_PARENT
            mVideoSurfaceFrame?.layoutParams = lp
            changeMediaPlayerLayout(sw, sh)
            return
        }

        if (lp?.width == lp?.height && lp?.width == ViewGroup.LayoutParams.MATCH_PARENT) {
            /* We handle the placement of the video using Android View LayoutParams */
            mMediaPlayer?.aspectRatio = null
            mMediaPlayer?.scale = 0f
        }

        var dw = sw.toDouble()
        var dh = sh.toDouble()
        val isPortrait = Resources.getSystem().configuration.layoutDirection == Configuration.ORIENTATION_PORTRAIT

        if (sw > sh && isPortrait || sw < sh && !isPortrait) {
            dw = sh.toDouble()
            dh = sw.toDouble()
        }

        // compute the aspect ratio
        var ar: Double
        val vw: Double
        if (mVideoSarDen == mVideoSarNum) {
            /* No indication about the density, assuming 1:1 */
            vw = mVideoVisibleWidth.toDouble()
            ar = mVideoVisibleWidth.toDouble() / mVideoVisibleHeight.toDouble()
        } else {
            /* Use the specified aspect ratio */
            vw = mVideoVisibleWidth * mVideoSarNum.toDouble() / mVideoSarDen
            ar = vw / mVideoVisibleHeight
        }

        // compute the display aspect ratio
        val dar = dw / dh

        when (currentSize) {
            surfaceBestFit -> if (dar < ar)
                dh = dw / ar
            else
                dw = dh * ar
            surfaceFitScreen -> if (dar >= ar)
                dh = dw / ar /* horizontal */
            else
                dw = dh * ar /* vertical */
            surfaceFill -> {
            }
            surface16To9 -> {
                ar = 16.0 / 9.0
                if (dar < ar)
                    dh = dw / ar
                else
                    dw = dh * ar
            }
            surface4To3 -> {
                ar = 4.0 / 3.0
                if (dar < ar)
                    dh = dw / ar
                else
                    dw = dh * ar
            }
            surfaceOriginal -> {
                dh = mVideoVisibleHeight.toDouble()
                dw = vw
            }
        }

        // set display size
        lp?.width = Math.ceil(dw * mVideoWidth / mVideoVisibleWidth).toInt()
        lp?.height = Math.ceil(dh * mVideoHeight / mVideoVisibleHeight).toInt()
        mVideoSurface?.layoutParams = lp
        if (mSubtitlesSurface != null) {
            mSubtitlesSurface?.layoutParams = lp
        }
        // set frame size (crop if necessary)
        lp = mVideoSurfaceFrame?.layoutParams
        lp?.width = Math.floor(dw).toInt()
        lp?.height = Math.floor(dh).toInt()
        mVideoSurfaceFrame?.layoutParams= lp

        mVideoSurface?.invalidate()
        if (mSubtitlesSurface != null) {
            mSubtitlesSurface?.invalidate()
        }
    }
    override fun onNewVideoLayout(vlcVOut:IVLCVout, width:Int, height:Int, visibleWidth:Int, visibleHeight:Int, sarNum:Int, sarDen:Int) {
        mVideoWidth = width
        mVideoHeight = height
        mVideoVisibleWidth = visibleWidth
        mVideoVisibleHeight = visibleHeight
        mVideoSarNum = sarNum
        mVideoSarDen = sarDen
        updateVideoSurfaces()
    }
}

