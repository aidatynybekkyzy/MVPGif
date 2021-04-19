package com.kimoterru.mvpgif

import com.kimoterru.mvpgif.api.GifItem
import com.kimoterru.mvpgif.api.GifResponse

interface Contract {
    interface MainView{
        fun showTrending(data: List<GifItem>)
        fun showError(error:String)
        fun showLoading(isLoading : Boolean)
    }
    interface MainModel{
        fun loadTrending(key:String,limit:Int,offset: Int,listener: MainListener)
    }
    interface MainPresenter{
        fun onLoadTrending()
    }
    interface MainListener{
        fun onResponse(response: GifResponse)
        fun onFailure(t:Throwable)
    }
}
