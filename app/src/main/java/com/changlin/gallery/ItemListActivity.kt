package com.changlin.gallery

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import androidx.core.util.Pair
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.changlin.gallery.api.TestService
import com.changlin.gallery.entity.CoverEntity
import com.changlin.gallery.utils.ScreenUtils
import com.changlin.networklibrary.ApiClient
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_item_detail.*
import kotlinx.android.synthetic.main.activity_item_list.*
import kotlinx.android.synthetic.main.activity_item_list.fab
import kotlinx.android.synthetic.main.item_list.*
import kotlinx.android.synthetic.main.item_list_content.view.*
import org.jsoup.Jsoup


/**
 * An activity representing a list of Pings. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a [ItemDetailActivity] representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
class ItemListActivity : AppCompatActivity() {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private var twoPane: Boolean = false

    private var mCovers: ArrayList<CoverEntity> = ArrayList()

    private var mAdapter: SimpleItemRecyclerViewAdapter ?= null

    private var mScreenWidth: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_item_list)

        setSupportActionBar(toolbar)
        toolbar.title = title

        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }

        if (item_detail_container != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            twoPane = true
        }
        mScreenWidth = ScreenUtils.getScreenWidth(this)
        setupRecyclerView(item_list)
        Thread(Runnable {
            val js = Jsoup.connect("https://wallhaven.cc/toplist?page=1").get()
            val body = js.body()
            val thumbs = body.getElementById("main").getElementById("thumbs")
            val section = thumbs.getElementsByTag("section")
            val figure = section.select("figure")
            for (headline in figure) {
                val coverEntity = CoverEntity()
                var src = headline.getElementsByClass("lazyload").attr("data-src")
                val span = headline.getElementsByTag("span")
                var suffix  = ""
                if(span.size > 1){
                    suffix = span[1].text().toLowerCase()
                }
                src = src.replaceFirst("https://th.wallhaven.cc/small","https://w.wallhaven.cc/full")
                var name = "wallhaven-" + src.substring(src.lastIndexOf("/") + 1)
                if(!TextUtils.isEmpty(suffix)){
                    name = name.replaceAfterLast(".",suffix)
                }
                coverEntity.url = src.replaceAfterLast("/",name)
                coverEntity.details = headline.getElementsByClass("preview").attr("href")
                Log.d("test",coverEntity.toString())
                mCovers.add(coverEntity)
            }
            handler.sendEmptyMessage(0x1)
        }).start()
//        val apiService = ApiClient.instance.getApiService(TestService::class.java)
//        apiService.getTopList(1)
//            .sub
    }

    @SuppressLint("HandlerLeak")
    private val handler = object : Handler(){

        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            mAdapter!!.setData(mCovers)
        }
    }

    private fun setupRecyclerView(recyclerView: RecyclerView) {
        mAdapter = SimpleItemRecyclerViewAdapter(this, mCovers,mScreenWidth)
        recyclerView.adapter = mAdapter
    }

    class SimpleItemRecyclerViewAdapter(
        private val parentActivity: ItemListActivity,
        private var values: List<CoverEntity>,
        private val screenWidth : Int
    ) :
        RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder>() {

        private var imageHeight : Int = 0

        init {
            imageHeight = (screenWidth - 16 * 2)  * 2/3
        }

        fun setData(data: List<CoverEntity>){
            values = data
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_list_content, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = values[position]
            Glide.with(holder.coverImage.context).asDrawable()
                .override(ViewGroup.LayoutParams.MATCH_PARENT,150)
                .load(item.url).into(holder.coverImage)
            with(holder.itemView) {
                tag = item
                setOnClickListener { v ->
                    val activityOptions = ActivityOptionsCompat.makeSceneTransitionAnimation(
                        v.context as Activity, Pair.create(holder.coverImage,"CoverName"))
                    val intent = Intent(v.context, ItemDetailActivity::class.java).apply {
                        putExtra(ItemDetailFragment.COVER_URL, item.url)
                        putExtra(ItemDetailFragment.COVER_DETAILS, item.details)
                    }
                    v.context.startActivity(intent,activityOptions.toBundle())
                }
            }
        }

        override fun getItemCount() = values.size

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val coverImage: ImageView = view.iv_cover
        }
    }
}
