package com.beauver.minecraft.plugins.webPanel.Website.Pages

import com.beauver.minecraft.plugins.webPanel.WebPanel
import fi.iki.elonen.NanoHTTPD.Response
import fi.iki.elonen.NanoHTTPD.newFixedLengthResponse

class HTMLPages {

    fun serve(path: String): Response{
        WebPanel::class.java.classLoader.getResourceAsStream(path)?.let { inputStream ->
            return newFixedLengthResponse(Response.Status.OK, "text/html", inputStream, inputStream.available().toLong())
        } ?: return newFixedLengthResponse(Response.Status.NOT_FOUND, "text/plain", "File not found")
    }

}