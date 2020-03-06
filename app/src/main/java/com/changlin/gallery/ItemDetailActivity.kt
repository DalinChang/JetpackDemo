package com.changlin.gallery

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import android.view.MenuItem
import android.view.ViewGroup
import androidx.palette.graphics.Palette
import com.bumptech.glide.Glide
import com.changlin.gallery.utils.StatusBarUtil
import com.changlin.networklibrary.ApiClient
import com.github.florent37.glidepalette.BitmapPalette
import com.github.florent37.glidepalette.GlidePalette
import kotlinx.android.synthetic.main.activity_item_detail.*
import org.json.JSONObject

/**
 * An activity representing a single Item detail screen. This
 * activity is only used on narrow width devices. On tablet-size devices,
 * item details are presented side-by-side with a list of items
 * in a [ItemListActivity].
 */
class ItemDetailActivity : AppCompatActivity() {

    private var mCoverUrl: String ?= null

    @SuppressLint("NewApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_item_detail)
//        StatusBarUtil.setRootViewFitsSystemWindows(this, true)
        StatusBarUtil.setTranslucentStatus(this)
        ApiClient.instance
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
            Glide.with(this).asDrawable().override(ViewGroup.LayoutParams.MATCH_PARENT,300)
                .load(mCoverUrl)
                .listener(GlidePalette.with(mCoverUrl)
                    .intoCallBack {
                        var test = it?.getVibrantColor(getColor(R.color.colorPrimary))
                        toolbar_layout.contentScrim = test?.let { it1 -> ColorDrawable(it1) }
                        Log.d("test-color",test.toString())
                    }).into(iv_show)
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
}
