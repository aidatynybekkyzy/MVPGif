package com.kimoterru.mvpgif

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import com.kimoterru.mvpgif.api.GifItem
import com.kimoterru.mvpgif.databinding.ActivityMainBinding
import com.kimoterru.mvpgif.posts.CustomRecyclerAdapter
import com.kimoterru.mvpgif.utils.PagedScrollListener

class MainActivity : AppCompatActivity(), Contract.MainView {
    private lateinit var binding: ActivityMainBinding
    private var presenter: MainActivityPresenter? = null
    private var adapter: CustomRecyclerAdapter? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initRecycler()
        presenter = MainActivityPresenter(this)
        presenter?.onLoadTrending()
    }

    private fun initRecycler() {
        val lm = GridLayoutManager(this, 2)
        binding.gifsRecycler.layoutManager = lm
        adapter = CustomRecyclerAdapter(ArrayList()) {

        }
        binding.gifsRecycler.adapter = adapter
        binding.gifsRecycler.addOnScrollListener(object : PagedScrollListener(lm) {
            override fun canLoad(): Boolean {
                return presenter?.canLoadNewPage() ?: false
            }

            override fun loadMoreItems() {
                presenter?.onLoadTrending()
            }

        })
    }

    override fun showTrending(data: List<GifItem>) {
        adapter?.addData(data)
    }


    override fun showError(error: String) {

    }

    override fun showLoading(isLoading: Boolean) {
        binding.progress.isVisible = isLoading
    }
}