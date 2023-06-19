package com.tos.libLocalServer

import com.tos.libLocalServer.nano.NanoServer

object ServerProvider {
    var currentState: ServerState = ServerState.Stopped()
    private var server: NanoServer? = null

    fun startServer() {
        if (server == null || server?.inRunning() == false) {
            server = NanoServer()
        }
        currentState = server?.startServer() ?: return
    }

    fun stopServer() { currentState = server?.stopServer() ?: return }

    fun setDistDir(path: String) {
        server?.setDistPath(path)
    }
}