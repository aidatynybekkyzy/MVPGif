package com.kimoterru.mvpgif

import com.kimoterru.mvpgif.api.GifResponse

class MainActivityPresenter(val view: Contract.MainView?) : Contract.MainPresenter {
    private val model = MainActivityModel()
    private var isLoading = false
    private var isLastPage = false
    private var currentOffset = 0

    override fun onLoadTrending() {
        isLoading = true
        view?.showLoading(isLoading)
        model.loadTrending("CtRYArKhb4YGx41Z7h0wR3h0SAyVRxWr", 30, currentOffset,
            object : Contract.MainListener {
                override fun onResponse(response: GifResponse) {
                    processResponse(response)
                    isLoading = false
                    view?.showLoading(isLoading)
                }

                override fun onFailure(t: Throwable) {
                    view?.showError(t.message ?:"OOW")
                    isLastPage = false
                    view?.showLoading(isLoading)
                }

            })
    }
    private fun processResponse(response: GifResponse){
        view?.showTrending(response.data)
        currentOffset += 30
        if(currentOffset >= response.pagination.total) {
            isLoading = true
        }
    }
    fun canLoadNewPage() : Boolean {
        return !isLastPage && !isLoading
    }
}