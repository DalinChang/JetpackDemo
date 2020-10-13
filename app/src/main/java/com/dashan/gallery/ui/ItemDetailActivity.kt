package com.dashan.gallery.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.dashan.base.StatusBarUtil
import com.dashan.gallery.R
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_item_detail.*


class ItemDetailActivity : AppCompatActivity(), View.OnClickListener {

    private var mCoverUrl: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_item_detail)
        StatusBarUtil.setTranslucent(this, 10)
        StatusBarUtil.setDarkMode(this)
        setSupportActionBar(detail_toolbar)
        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own detail action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }

        // Show the Up button in the action bar.
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // savedInstanceState is non-null when there is fragment state
        // saved from previous configurations of this activity
        // (e.g. when rotating the screen from portrait to landscape).
        // In this case, the fragment will automatically be re-added
        // to its container so we don't need to manually add it.
        // For more information, see the Fragments API guide at:
        //
        // http://developer.android.com/guide/components/fragments.html
        //
        if (savedInstanceState == null) {
            // Create the detail fragment and add it to the activity
            // using a fragment transaction.
            mCoverUrl = intent.getStringExtra(ItemDetailFragment.COVER_URL)
            Glide.with(this).asDrawable().override(ViewGroup.LayoutParams.MATCH_PARENT, 300)
                .load(mCoverUrl).into(iv_show)
            val fragment = ItemDetailFragment().apply {
                arguments = Bundle().apply {
                    putString(
                        ItemDetailFragment.COVER_URL,
                        mCoverUrl
                    )
                    putString(
                        ItemDetailFragment.COVER_DETAILS,
                        intent.getStringExtra(ItemDetailFragment.COVER_DETAILS)
                    )
                }
            }
            supportFragmentManager.beginTransaction()
                .add(R.id.item_detail_container, fragment)
                .commit()
            iv_show.setOnClickListener(this)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem) =
        when (item.itemId) {
            android.R.id.home -> {
                // This ID represents the Home or Up button. In the case of this
                // activity, the Up button is shown. For
                // more details, see the Navigation pattern on Android Design:
                //
                // http://developer.android.com/design/patterns/navigation.html#up-vs-back

//                navigateUpTo(Intent(this, ItemListActivity::class.java))
                if (Build.VERSION.SDK_INT >= 21) {
                    finishAfterTransition()
                } else {
                    finish()
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.iv_show -> {
//                val path: String = System.currentTimeMillis().toString() + ".jpg"
//                val destinationUri = Uri.fromFile(File(cacheDir,path))
//                val uCrop = UCrop.of(Uri.parse(mCoverUrl), destinationUri);
//                val options = UCrop.Options()
//                //设置裁剪图片可操作的手势
//                //设置裁剪图片可操作的手势
//                options.setAllowedGestures(
//                    UCropActivity.SCALE,
//                    UCropActivity.ROTATE,
//                    UCropActivity.ALL
//                )
//                //设置toolbar颜色
//                //设置toolbar颜色
//                options.setToolbarColor(ActivityCompat.getColor(this, R.color.colorPrimary))
//                //设置状态栏颜色
//                //设置状态栏颜色
//                options.setStatusBarColor(ActivityCompat.getColor(this, R.color.colorPrimary))
//                //是否能调整裁剪框
//                // options.setFreeStyleCropEnabled(true);
//                //是否能调整裁剪框
//                // options.setFreeStyleCropEnabled(true);
//                uCrop.withOptions(options)
//                uCrop.start(this)
                val intent = Intent(v.context, ResizeActivity::class.java).apply {
                    putExtra(ResizeActivity.KEY_IMAGE, mCoverUrl)
                }
                startActivity(intent)
            }
        }
    }
}
