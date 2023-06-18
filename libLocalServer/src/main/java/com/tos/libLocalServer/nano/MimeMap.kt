package com.tos.libLocalServer.nano

internal object MimeMap {
    @JvmStatic
    val MIME_MAP: MutableMap<String, String> = HashMap()

    init {
        MIME_MAP["appcache"] = "text/cache-manifest"
        MIME_MAP["css"] = "text/css"
        MIME_MAP["gif"] = "image/gif"
        MIME_MAP["html"] = "text/html"
        MIME_MAP["js"] = "application/javascript"
        MIME_MAP["json"] = "application/json"
        MIME_MAP["jpg"] = "image/jpeg"
        MIME_MAP["jpeg"] = "image/jpeg"
        MIME_MAP["mp4"] = "video/mp4"
        MIME_MAP["pdf"] = "application/pdf"
        MIME_MAP["png"] = "image/png"
        MIME_MAP["svg"] = "image/svg+xml"
        MIME_MAP["xlsm"] = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        MIME_MAP["xml"] = "application/xml"
        MIME_MAP["zip"] = "application/zip"
        MIME_MAP["md"] = "text/plain"
        MIME_MAP["txt"] = "text/plain"
        MIME_MAP["php"] = "text/plain"
        MIME_MAP["ico"] = "image/x-icon"
    }
}