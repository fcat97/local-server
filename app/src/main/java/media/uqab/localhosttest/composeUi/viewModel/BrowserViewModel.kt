package media.uqab.localhosttest.composeUi.viewModel

import android.content.Intent
import android.graphics.Bitmap
import android.util.Log
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cafe.adriel.voyager.core.model.ScreenModel


class BrowserViewModel(var rootUrl: String): ScreenModel {
    var url by mutableStateOf("")

    var webView: WebView? = null
    val webViewClient = object: WebViewClient() {
        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
            Log.d(TAG, "onPageStarted: $url")
        }

        override fun shouldOverrideUrlLoading(
            view: WebView?,
            request: WebResourceRequest?
        ): Boolean {
            Log.d(TAG, "shouldOverrideUrlLoading: ${request?.url}")
            request?.url?.let { uri ->
                val i = Intent(Intent.ACTION_VIEW)
                i.data = uri
                view?.context?.startActivity(i)
            }
            return true
        }
    }

    override fun onDispose() {
        super.onDispose()
        webView = null
    }

    companion object {
        private const val TAG = "BrowserViewModel"
    }
}