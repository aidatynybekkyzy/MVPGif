package com.kimoterru.mvpgif

import android.Manifest
import android.app.Dialog
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.os.StrictMode
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.kimoterru.mvpgif.api.GifItem
import com.kimoterru.mvpgif.databinding.ActivityMainBinding
import com.kimoterru.mvpgif.posts.CustomRecyclerAdapter
import com.kimoterru.mvpgif.utils.PagedScrollListener
import java.io.File
import java.lang.IllegalArgumentException
import java.lang.reflect.Method


//added comment

class MainActivity : AppCompatActivity(), Contract.MainView {
    private lateinit var binding: ActivityMainBinding
    private var presenter: MainActivityPresenter? = null
    private var adapter: CustomRecyclerAdapter? = null

    private var dm: DownloadManager? = null
    private var requestId: Long = 0L
    private var fileName = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initRecycler()
        presenter = MainActivityPresenter(this)
        presenter?.onLoadTrending()

        val filter = IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)//  опеределили то что мы хотим получать в ответ
        registerReceiver(downloadReceiver, filter)
    }

    private fun initRecycler() {
        val lm = GridLayoutManager(this, 2)
        binding.gifsRecycler.layoutManager = lm
        adapter = CustomRecyclerAdapter(ArrayList()) {
           val status =  checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            if(status==PackageManager.PERMISSION_GRANTED){
                downloadGif(it)
            }
            else{
                val perms = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                requestPermissions(perms,15)
            }
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
        fileName = System.currentTimeMillis().toString() + ".gif" //чтобы гифки отличались друг от друга
        val request = DownloadManager.Request(uri) // запрос на загрузку
        request.setTitle("Downloading Gif")
        request.setDescription("wait a second...") // описание
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)//(директория)адрес данного файла зависит от того что мы тут указывваем
        requestId = dm?.enqueue(request) ?: 0 //поставить в очеред запрос на скачивание, при вызове возвращает лонг цифру - это айди запроса

    }

    private val downloadReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {

            val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            if (id == requestId) {

                val m: Method = StrictMode::class.java.getMethod("disableDeathOnFileUriExposure") //
                m.invoke(null)

                showLoading(false)
                val path = Environment.getExternalStorageDirectory().toString()+"/"+ Environment.DIRECTORY_DOWNLOADS+ fileName
                val file = File(path)
                showDialog(file)
            }

        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(downloadReceiver) // ресейвер отключается
        } catch (ex: IllegalArgumentException) {
            print("error")
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

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode==15){
            if(grantResults.isNotEmpty()&& grantResults[0]== PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this,"Отлично, теперь выберите гифку",Toast.LENGTH_SHORT).show()
            }
            else{
                Toast.makeText(this,"Разрешите доступ к файлам",Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showDialog(file: File) {
        val dialog = Dialog(this)
        dialog.setCancelable(true)//для закрытия диалога ищ вне
        dialog.setContentView(R.layout.dialog_confirm_dl)

        val image = dialog.findViewById<ImageView>(R.id.preview)
        val button = dialog.findViewById<Button>(R.id.share)
        Glide.with(this).load(file).into(image) // для отображения картинки
        button.setOnClickListener {
            dialog.dismiss()
            val myIntent = Intent(Intent.ACTION_SEND)
            myIntent.type = "image/*" // Какого формата указывается после слеш и *
            myIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file))
            startActivity(Intent.createChooser(myIntent, "Share GIF")) // окно выбора опций, куда надо отправить

            dialog.show()
        }
    }
}