package media.uqab.localhosttest.composeUi.screen

import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.KeyboardArrowRight
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.tos.libLocalServer.ServerState
import media.uqab.localhosttest.R
import media.uqab.localhosttest.data.UiEvent
import media.uqab.localhosttest.data.model.Dist
import com.tos.libLocalServer.ServerProvider
import media.uqab.localhosttest.composeUi.theme.serverUpColor
import media.uqab.localhosttest.composeUi.viewModel.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeScreen(viewModel: HomeViewModel) {
    val navigator = LocalNavigator.current
    val surfaceColor = MaterialTheme.colorScheme.primary
    val backgroundColor = remember(viewModel.serverState) {
        when(viewModel.serverState) {
            is ServerState.Running -> serverUpColor
            is ServerState.Stopped -> surfaceColor
        }
    }
    var serverStateText by remember { mutableStateOf("") }
    var buttonText by remember { mutableStateOf("") }
    val snackBarHostState = remember { SnackbarHostState() }
    val systemUiController = rememberSystemUiController()

    systemUiController.setSystemBarsColor(color = backgroundColor)

    val pickDistDirLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument(),
        onResult = viewModel::onDistImported
    )

    LaunchedEffect(key1 = viewModel.serverState) {
        when(viewModel.serverState) {
            is ServerState.Running -> {
                serverStateText = (viewModel.serverState as ServerState.Running).url
                buttonText = "Stop Server"
            }

            is ServerState.Stopped -> {
                serverStateText = (viewModel.serverState as ServerState.Stopped).cause
                buttonText = "Start Server"
            }
        }
    }

    LaunchedEffect(key1 = viewModel.uiEvent) {
        viewModel.uiEvent?.let { event ->
            snackBarHostState.showSnackbar(event.msg)
            viewModel.uiEvent = null // consumed
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = stringResource(id = R.string.app_name))
                },
                colors = TopAppBarDefaults
                    .mediumTopAppBarColors(
                        containerColor = backgroundColor,
                        titleContentColor = Color.White
                    )
            )
        },
        snackbarHost = { SnackbarHost(snackBarHostState) },
    ) { pad ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(pad)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = backgroundColor.copy(alpha = 0.2f)),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                AnimatedVisibility(visible = viewModel.availableDistFiles.isNotEmpty()) {
                    Text(
                        text = "Available dist",
                        style = MaterialTheme.typography.titleLarge,
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // server dist list
                LazyColumn(modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.7f)
                    .padding(horizontal = 12.dp),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {

                    items(viewModel.availableDistFiles) {
                        ItemDist(
                            dist = it,
                            onClick = { dist ->
                                if (viewModel.serverState !is ServerState.Running) {
                                    viewModel.uiEvent = UiEvent("Server not running!")
                                } else {
                                    ServerProvider.setDistDir(dist.path)
                                    navigator?.push(BrowserScreen("http://$serverStateText"))
                                }
                            }
                        )
                    }
                }

                // import button
                AnimatedVisibility(visible = viewModel.availableDistFiles.isNotEmpty()) {

                    OutlinedButton(onClick = {
                        pickDistDirLauncher.launch(null)
                    }) {
                        Text(text = "Import Dist")
                    }
                }

                // server start/stop button
                ElevatedButton(
                    onClick = viewModel::onStartStopClick,
                    colors = ButtonDefaults.elevatedButtonColors(
                        containerColor = if (viewModel.serverState is ServerState.Running) {
                            serverUpColor
                        } else surfaceColor
                    ),
                ) {
                    Text(text = buttonText, color = Color.White)
                }

                Spacer(modifier = Modifier.height(8.dp))

                // server status text
                Text(text = if (viewModel.serverState is ServerState.Running) {
                    "Running @"
                } else ""
                )

                // open in webView button
                Text(
                    text = serverStateText,
                    style = MaterialTheme.typography.headlineMedium,
                )
            }

            if (viewModel.availableDistFiles.isEmpty()) {
                Card(
                    onClick = {
                        Log.d("HomeScreen", "HomeScreen: called")
                        pickDistDirLauncher.launch(arrayOf("application/zip"))
                    },
                    shape = CircleShape,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .fillMaxWidth(0.7f)
                        .aspectRatio(1f),
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Text(
                            text = "no dist available! \nImport...",
                            style = MaterialTheme.typography.titleMedium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ItemDist(
    modifier: Modifier = Modifier,
    dist: Dist,
    onClick: (Dist) -> Unit
) {
    ElevatedCard(
        onClick = { onClick(dist) },
        modifier.fillMaxWidth(),
        shape = CardDefaults.elevatedShape,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.fillMaxWidth(0.85f)) {
                Text(
                    text = dist.name,
                    style = MaterialTheme.typography.titleMedium
                )

                Text(
                    text = dist.path,
                    style = MaterialTheme.typography.labelMedium)
            }

            Icon(imageVector = Icons.TwoTone.KeyboardArrowRight
                , contentDescription = null)
        }
    }
}

@Composable
@Preview
fun ItemPathPreview() {
    val dist = Dist("angular test", "/angular_test")

    ItemDist(
        modifier = Modifier,
        dist = dist,
        onClick = {}
    )
}

@Preview
@Composable
fun HomeScreenPreview() {
    HomeScreen(HomeViewModel())
}

class HomeScreen: Screen {
    @Composable
    override fun Content() {
        val viewModel = rememberScreenModel { HomeViewModel() }
        HomeScreen(viewModel = viewModel)
    }
}