package media.uqab.localhosttest.composeUi.viewModel

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.coroutineScope
import com.tos.libLocalServer.ServerState
import kotlinx.coroutines.launch
import media.uqab.localhosttest.DistHelper
import media.uqab.localhosttest.MyApp
import media.uqab.localhosttest.data.UiEvent
import media.uqab.localhosttest.data.model.Dist
import media.uqab.localhosttest.data.server.ServerProvider


class HomeViewModel: ScreenModel {
    var uiEvent by mutableStateOf<UiEvent?>(null)
    var serverState by mutableStateOf<ServerState>(ServerState.Stopped())

    var availableDistFiles by mutableStateOf<List<Dist>>(emptyList())

    init {
        // list all folders inside dist root directory
        listDistFiles()
    }

    /**
     * List all the imported dist folder from app's directory
     */
    private fun listDistFiles() = coroutineScope.launch {
        availableDistFiles = DistHelper(MyApp.applicationContext)
            .getAvailableDistList().map {
                Dist(
                    name = it.name
                        .split("_")
                        .joinToString(" ")
                        .split("-")
                        .joinToString(" "),
                    path = it.path,
                )
            }
    }

    fun onDistImported(uri: Uri?) = coroutineScope.launch {
        if (uri == null) {
            uiEvent = UiEvent("No folder selected!")
            return@launch
        }

        uiEvent = UiEvent("Importing dist. Please wait...")

        DistHelper(MyApp.applicationContext).importDist(uri)

        uiEvent = UiEvent("Import Done")
        listDistFiles()
    }

    fun onStartStopClick() {
        if (ServerProvider.currentState is ServerState.Running) {
            ServerProvider.stopServer()
        } else {
            ServerProvider.startServer()
        }

        serverState = ServerProvider.currentState
    }
}