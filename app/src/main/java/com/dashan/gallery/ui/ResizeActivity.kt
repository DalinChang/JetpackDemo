package com.dashan.gallery.ui

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Point
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.view.PixelCopy
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.dashan.base.StatusBarUtil
import com.dashan.gallery.R
import com.shizhefei.view.largeimage.LargeImageView
import kotlinx.android.synthetic.main.activity_resize.*


/**
 * author: 常林
 * date: 2020/10/10 16:33
 * description: 设置图片大小的Activity
 */
class ResizeActivity : AppCompatActivity() {

    private var mWidth = 0
    private var mHeight = 0

    companion object {
        const val KEY_IMAGE = "key_image"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        StatusBarUtil.setTranslucent(this, 255)
        StatusBarUtil.setLightMode(this)
        val window: Window = getWindow()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.clearFlags(
                WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                        or WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION
            )
            window.getDecorView()
                .setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_LAYOUT_STABLE)
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.setStatusBarColor(Color.TRANSPARENT)
            window.setNavigationBarColor(Color.TRANSPARENT)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS or WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
        }
        setContentView(R.layout.activity_resize)
        btn_save.setOnClickListener {
            btn_save.visibility = View.GONE
            btn_save.post {
                Thread.sleep(500)
                val capture = capture(this)
                iv_result.setImageBitmap(capture)
                iv_result.visibility = View.VISIBLE
                if (capture != null) {
                    saveMedia(capture)
                }
            }
        }
        val imageUrl = intent.extras?.getString(KEY_IMAGE)
        val defaultDisplay = windowManager.defaultDisplay
        val point = Point()
        defaultDisplay.getSize(point)
        mWidth = point.x
        mHeight = point.y
        image.setCriticalScaleValueHook(object : LargeImageView.CriticalScaleValueHook {

            override fun getMinScale(
                largeImageView: LargeImageView?,
                imageWidth: Int,
                imageHeight: Int,
                suggestMinScale: Float
            ): Float {
                return 1f
            }

            override fun getMaxScale(
                largeImageView: LargeImageView?,
                imageWidth: Int,
                imageHeight: Int,
                suggestMaxScale: Float
            ): Float {
                return 4f
            }

        })
        Glide.with(this).asBitmap().load(imageUrl)
            .listener(object : RequestListener<Bitmap?> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any,
                    target: Target<Bitmap?>,
                    isFirstResource: Boolean
                ): Boolean {
                    return false
                }

                override fun onResourceReady(
                    resource: Bitmap?,
                    model: Any,
                    target: Target<Bitmap?>,
                    dataSource: DataSource,
                    isFirstResource: Boolean
                ): Boolean {
                    runOnUiThread {
                        image.setImage(resource)
                    }
                    return false
                }
            }).submit()
    }


//    fun getBitmap(bitmap: Bitmap): Bitmap? {
//        var bitmap = bitmap
//        var scaleWidth = 1f
//        var scaleHeight = 1f
//        if (bitmap.width < mWidth) {
//            //强转为float类型，
//            scaleWidth = mWidth.toFloat() / bitmap.width.toFloat()
//        }
//        if (bitmap.height < mHeight) {
//            scaleHeight = mHeight.toFloat() / bitmap.height.toFloat()
//        }
//        if (scaleWidth > scaleHeight) scaleHeight = scaleWidth else scaleWidth = scaleHeight
//        val matrix = Matrix()
//        //根据屏幕大小选择bitmap放大比例。
//        matrix.postScale(scaleWidth, scaleHeight)
//        bitmap = Bitmap.createBitmap(
//            bitmap, 0, 0, bitmap.width, bitmap.height
//            , matrix, true
//        )
//        return bitmap
//    }

    private fun capture(activity: Activity): Bitmap? {
        val decorView = activity.window.decorView
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val createBitmap =
                Bitmap.createBitmap(decorView.width, decorView.height - 50, Bitmap.Config.ARGB_8888)
            PixelCopy.request(window, createBitmap, PixelCopy.OnPixelCopyFinishedListener {

            }, Handler(Looper.getMainLooper()))
            createBitmap
        } else {
            decorView.isDrawingCacheEnabled = true
            decorView.drawingCache
        }
    }

    /**
     * 保存到相册
     *
     * @param bitmap
     */
    private fun saveMedia(bitmap: Bitmap) {
        val image = MediaStore.Images.Media.insertImage(
            contentResolver,
            bitmap, "testaaaa", ""
        )

        //保存图片后发送广播通知更新数据库
        sendBroadcast(
            Intent(
                Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                Uri.parse(image)
            )
        )
    }
}