package com.kimoterru.mvpgif

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.StrictMode
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import com.kimoterru.mvpgif.api.GifItem
import com.kimoterru.mvpgif.databinding.ActivityMainBinding
import com.kimoterru.mvpgif.posts.CustomRecyclerAdapter
import com.kimoterru.mvpgif.utils.PagedScrollListener
import java.io.File
import java.lang.IllegalArgumentException
import java.lang.reflect.Method

class MainActivity : AppCompatActivity(), Contract.MainView {
    private lateinit var binding: ActivityMainBinding
    private var presenter: MainActivityPresenter? = null
    private var adapter: CustomRecyclerAdapter? = null

    private var dm : DownloadManager? = null
    private var requestId : Long = 0L
    private var fileName =""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initRecycler()
        presenter = MainActivityPresenter(this)
        presenter?.onLoadTrending()

        val filter = IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
        registerReceiver(downloadReceiver, filter)
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(downloadReceiver)
        } catch (ex : IllegalArgumentException) {
            ///.......
        }
    }
    private fun initRecycler() {
        val lm = GridLayoutManager(this, 2)
        binding.gifsRecycler.layoutManager = lm
        adapter = CustomRecyclerAdapter(ArrayList()) {
            downloadGif(it)
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

    private fun downloadGif(item: GifItem) {
        showLoading(true)
        val link = item.images.original.url
        dm = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val uri = Uri.parse(link)
        fileName = System.currentTimeMillis().toString() + ".gif"
        val request = DownloadManager.Request(uri)
        request.setTitle("Downloading GIF")
        request.setDescription("wait a second....")
        request.setDestinationInExternalFilesDir(this, "gifsToShare", fileName)
        requestId = dm?.enqueue(request) ?: 0L
    }

    private val downloadReceiver = object  : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val  id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            if (id == requestId) {
                showLoading(false)
                val path = getExternalFilesDir("gifsToShare").toString() + "/" + fileName
                val file = File(path)

                val m: Method = StrictMode :: class.java.getMethod("disableDeathOnFileUriExposure")
                m.invoke(null)

                val myIntent = Intent(Intent.ACTION_SEND)
                myIntent.type = "image/*"
                myIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file))
                startActivity(Intent.createChooser(myIntent,"Share Gif"))
            }
            println("Receiver")
        }
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