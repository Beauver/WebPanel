package com.beauver.minecraft.plugins.webPanel.Website.Api.Protected

import com.beauver.minecraft.plugins.webPanel.Website.Api.WebPanelApi
import fi.iki.elonen.NanoHTTPD.*
import org.bukkit.Bukkit
import java.io.File
import java.io.IOException

class DownloadFileAPI : WebPanelApi {

    override val requestMethod: Method = Method.GET

    override fun respond(session: IHTTPSession): Response {
        if(compareMethod(session.method).status != Response.Status.OK){
            return compareMethod(session.method)
        }else if(isAuthorized(session).status != Response.Status.OK) {
            return isAuthorized(session)
        }

        return getData(session)
    }

    override fun getData(session: IHTTPSession): Response {
        val filePath = session.parameters["path"]?.get(0)
            ?: return newFixedLengthResponse(Response.Status.BAD_REQUEST, "text/plain", "Missing file path")

        val file = File(Bukkit.getWorldContainer(), filePath)
        if (!file.exists() || file.isDirectory) {
            return newFixedLengthResponse(Response.Status.NOT_FOUND, "text/plain", "File not found")
        }

        return try {
            val inputStream = file.inputStream()
            newFixedLengthResponse(Response.Status.OK, "application/octet-stream", inputStream, file.length())
        } catch (e: IOException) {
            e.printStackTrace()
            newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "text/plain", "Failed to download file")
        }
    }
}