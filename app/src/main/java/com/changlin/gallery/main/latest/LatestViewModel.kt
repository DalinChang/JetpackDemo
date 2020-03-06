package com.changlin.gallery.main.latest

import android.text.TextUtils
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.changlin.gallery.entity.CoverEntity
import org.jsoup.Jsoup

class LatestViewModel : ViewModel(){

    private val LATEST_URL : String = "https://wallhaven.cc/latest?"

    private val mCovers: MutableLiveData<ArrayList<CoverEntity>> = MutableLiveData()

    private var currentPage : Int = 0

    init {
        requestData(currentPage + 1)
    }

    fun getData(): LiveData<ArrayList<CoverEntity>> {
        return mCovers
    }

    private fun requestData(page: Int){
        if(currentPage == page){
            return
        }
        Thread(Runnable {
            val requestUrl = LATEST_URL + "page=" + page
            val js = Jsoup.connect(requestUrl).get()
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
                mCovers.value?.add(coverEntity)
            }
            currentPage = page
        }).start()
    }
}
