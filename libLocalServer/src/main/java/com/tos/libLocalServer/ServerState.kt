package com.tos.libLocalServer

sealed class ServerState {
    data class Running(val text: String, val url: String): ServerState()
    data class Stopped(
        val cause: String = "Server Stopped",
        val error: Exception? = null
    ): ServerState()
}
