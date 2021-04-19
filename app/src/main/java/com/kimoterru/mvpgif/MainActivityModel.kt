package com.kimoterru.mvpgif

import com.kimoterru.mvpgif.api.GifResponse
import com.kimoterru.mvpgif.api.NetworkHelper
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivityModel : Contract.MainModel {
    override fun loadTrending(key: String, limit: Int, offset: Int, listener: Contract.MainListener) {
        NetworkHelper.getService()
            .getTrendingGifs(key, limit, offset)
            .enqueue(object : Callback<GifResponse> {
                override fun onFailure(call: Call<GifResponse>, t: Throwable) {
                    t.printStackTrace()
                    listener.onFailure(t)
                }

                override fun onResponse(call: Call<GifResponse>, response: Response<GifResponse>) {
                    if (response.isSuccessful && response.body() != null) {
                        listener.onResponse(response.body()!!)
                    } else {
                        listener.onFailure(Exception(response.message()))
                    }
                }

            })

    }
}