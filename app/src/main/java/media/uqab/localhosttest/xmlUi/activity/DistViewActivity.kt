package media.uqab.localhosttest.xmlUi.activity

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.LinearLayout
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.tos.libLocalServer.ServerState
import kotlinx.coroutines.launch
import media.uqab.localhosttest.DistHelper
import media.uqab.localhosttest.data.server.ServerProvider

class DistViewActivity: ComponentActivity() {
    private lateinit var webView: WebView
    private lateinit var distHelper: DistHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        webView = WebView(this)
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        )
        webView.layoutParams = params
        setContentView(webView)

        webView.apply {
            @SuppressLint("SetJavaScriptEnabled")
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                webViewClient = this@DistViewActivity.webViewClient
            }
        }

        val url = intent.getStringExtra(EXTRA_DIST_URL)
        if (url.isNullOrBlank()) {
            finish()
            return
        }

        val distName = url.substringAfterLast("/").replace(".zip", "")


        distHelper = DistHelper(this)

        if (!distHelper.isDistAvailable(distName)) {
            lifecycleScope.launch {
                val success = distHelper.downloadAndImportDist(
                    url = url,
                    onDownloadStart = { showDownloading() },
                )

                if (success) loadDist(distName)
                else showError()
            }
        } else {
            loadDist(distName)
        }
    }

    private fun showDownloading() {
        webView.settings.allowFileAccess = true
        distHelper.getLoadingFile()?.let { webView.loadUrl(it) }
    }

    private fun showError() {
        webView.settings.allowFileAccess = true
        distHelper.getErrorFile()?.let { webView.loadUrl(it) }
    }

    private fun loadDist(distName: String) {
        webView.settings.allowFileAccess = false

        ServerProvider.startServer()
        ServerProvider.setDistDir(distHelper.getDistPath(distName))

        if (ServerProvider.currentState is ServerState.Running) {
            val url = (ServerProvider.currentState as ServerState.Running).url
            Log.d(TAG, "loadDist: $url")

            webView.loadUrl(url)
        }
    }

    private val webViewClient = object: WebViewClient() {
        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
            Log.d(TAG, "onPageStarted: $url")
        }
    }

    companion object {
        private const val TAG = "DistViewActivity"
        const val EXTRA_DIST_URL = "dist_url"
    }
}