package media.uqab.localhosttest.composeUi.screen

import android.annotation.SuppressLint
import android.graphics.drawable.ColorDrawable
import android.webkit.WebView
import android.widget.LinearLayout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import media.uqab.localhosttest.composeUi.theme.serverUpColor
import media.uqab.localhosttest.composeUi.viewModel.BrowserViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BrowserScreen(viewModel: BrowserViewModel) {
    val navigator = LocalNavigator.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = viewModel.rootUrl)
                },
                colors = TopAppBarDefaults
                    .mediumTopAppBarColors(
                        containerColor = serverUpColor,
                        titleContentColor = MaterialTheme.colorScheme.contentColorFor(serverUpColor)
                    ),
                navigationIcon = {
                    IconButton(onClick = {
                        if (
                            viewModel.webView != null &&
                            viewModel.webView!!.canGoBack() &&
                            viewModel.url != viewModel.rootUrl) {

                            viewModel.webView!!.goBack()
                        } else {
                            navigator?.pop()
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "back"
                        )
                    }
                }
            )
        }
    ) { pad ->
        AndroidView(
            factory = { context ->
                WebView(context).apply {
                    val layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT)
                    setLayoutParams(layoutParams)
                    background = ColorDrawable(Color.Yellow.toArgb())

                    @SuppressLint("SetJavaScriptEnabled")
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true

                    webViewClient = viewModel.webViewClient
                }.also {
                    viewModel.webView = it
                }
            },
            modifier = Modifier
                .padding(pad)
                .fillMaxSize(),
            update = {
                it.loadUrl(viewModel.rootUrl)
            }
        )
    }
}

@Preview
@Composable
private fun BrowserScreenPreview() {
    BrowserScreen(viewModel = BrowserViewModel(""))
}

class BrowserScreen(
    private val indexUrl: String,
): Screen {
    @Composable
    override fun Content() {
        val viewModel = rememberScreenModel { BrowserViewModel(indexUrl) }

        BrowserScreen(viewModel = viewModel)
    }
}