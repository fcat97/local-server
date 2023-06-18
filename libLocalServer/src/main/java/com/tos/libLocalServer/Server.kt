package com.tos.libLocalServer

/**
 * Basic of a server
 */
internal interface Server {
    fun inRunning(): Boolean
    fun startServer(): ServerState
    fun stopServer(): ServerState
}