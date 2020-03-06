package com.changlin.gallery.main.toplist

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
import androidx.core.app.ActivityOptionsCompat
import androidx.core.util.Pair
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.changlin.gallery.ItemDetailActivity
import com.changlin.gallery.ItemDetailFragment

import com.changlin.gallery.R
import com.changlin.gallery.entity.CoverEntity
import com.changlin.gallery.main.random.RandomFragment
import com.changlin.gallery.utils.ScreenUtils
import kotlinx.android.synthetic.main.item_list.*
import kotlinx.android.synthetic.main.item_list_content.view.*
import org.jsoup.Jsoup

class TopListFragment : Fragment() {

    private var topListViewModel: TopListViewModel? = null

    private var mAdapter: SimpleItemRecyclerViewAdapter?= null

    private val mCovers: ArrayList<CoverEntity> = ArrayList()

    private var mScreenWidth: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        topListViewModel = ViewModelProviders.of(this).get(TopListViewModel::class.java)
        return inflater.inflate(R.layout.fragment_toplist, container, false)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mScreenWidth = context?.let { ScreenUtils.getScreenWidth(it) }!!
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
                    suffix = span[span.size - 1].text().toLowerCase()
                    if(suffix.length < 3){
                        suffix = ""
                    }
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
                handler.sendEmptyMessage(0x1)
            }
        }).start()
    }


    @SuppressLint("HandlerLeak")
    private val handler = object : Handler(){

        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            mAdapter!!.setData(mCovers)
        }
    }

    private fun setupRecyclerView(recyclerView: RecyclerView) {
        mAdapter = SimpleItemRecyclerViewAdapter(mCovers, mScreenWidth)
        recyclerView.adapter = mAdapter
    }

    class SimpleItemRecyclerViewAdapter(
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