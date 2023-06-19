package com.tos.libLocalServer.nano

import com.tos.libLocalServer.Server
import com.tos.libLocalServer.ServerState
import com.tos.libLocalServer.nano.MimeMap.MIME_MAP
import fi.iki.elonen.NanoHTTPD
import java.io.File
import java.io.FileInputStream

/**
 * A server controller based on NanoHTTPD library
 *
 * @author github/fCat97
 */
internal class NanoServer: Server {
    companion object {
        private const val TAG = "NanoServer"
        private const val PORT = 9099
    }

    private var _distPath: String = ""
    private val distPath: String get() = _distPath

    private var nanoServer: NanoHTTPD? = null

    override fun inRunning(): Boolean {
        return nanoServer?.isAlive == true
    }

    override fun startServer(): ServerState {
        if (nanoServer == null) {
            nanoServer = MyNanoHHTTPD(PORT) { distPath }
        }

        return if (nanoServer?.isAlive == true) {
            ServerState.Running("Server Started", "${nanoServer?.hostname}:$PORT")
        } else {
            try {
                nanoServer?.start()
                println("startServer: $nanoServer ${nanoServer?.hostname}:${nanoServer?.listeningPort} ${nanoServer?.isAlive}")
                ServerState.Running("Server Started", "127.0.0.1:$PORT")
            } catch (e: Exception) {
                e.printStackTrace()
                ServerState.Stopped(error = e)
            }
        }
    }

    override fun stopServer(): ServerState {
        return try {
            nanoServer?.stop()
            ServerState.Stopped()
        } catch (e: Exception) {
            ServerState.Stopped(error = e)
        }
    }

    /**
     * Set root direction of the app. This directory should contain
     * the index.html file.
     */
    fun setDistPath(distPath: String) {
        this._distPath = distPath
    }

    private class MyNanoHHTTPD(port: Int, private val getAppDir: () -> String): NanoHTTPD(port) {
        override fun serve(session: IHTTPSession?): Response {
            val uri = session?.uri ?: return super.serve(session)

            val appPath = getAppDir()
            println("serve: $uri $appPath")

            // Set the appropriate content type
            if (uri.startsWith("/") && uri.endsWith("/")) {
                val mimeType = "text/html"
                val inputStream = FileInputStream(File("$appPath/index.html"))
                return newChunkedResponse(Response.Status.OK, mimeType, inputStream)
            } else if (uri.startsWith("/")) {
                val ext = uri.split(".").last()
                val mimeType = MIME_MAP[ext]
                val inputStream = FileInputStream(File("$appPath${uri}"))
                return newChunkedResponse(Response.Status.OK, mimeType, inputStream)
            }

            return newFixedLengthResponse(Response.Status.NOT_FOUND, MIME_PLAINTEXT, "404 Not Found");
        }
    }
}