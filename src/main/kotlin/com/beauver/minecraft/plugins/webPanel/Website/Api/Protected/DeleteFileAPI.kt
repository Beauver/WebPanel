package com.beauver.minecraft.plugins.webPanel.Website.Api.Protected

import com.beauver.minecraft.plugins.webPanel.Website.Api.WebPanelApi
import fi.iki.elonen.NanoHTTPD.*
import org.bukkit.Bukkit
import java.io.File

class DeleteFileAPI : WebPanelApi {
    override val requestMethod: Method = Method.DELETE

    override fun respond(session: IHTTPSession): Response {
        if(compareMethod(session.method).status != Response.Status.OK){
            return compareMethod(session.method)
        }else if(isAuthorized(session).status != Response.Status.OK) {
            return isAuthorized(session)
        }

        return execute(session)
    }

    override fun execute(session: IHTTPSession): Response {
        session.parseBody(null)
        val parms: Map<String, List<String>> = session.parameters
        val filePath = parms["path"]?.get(0) ?: return newFixedLengthResponse(Response.Status.BAD_REQUEST, "text/plain", "Missing file path")

        val file = File(Bukkit.getWorldContainer(), filePath)
        if (!file.exists()) {
            return newFixedLengthResponse(Response.Status.NOT_FOUND, "text/plain", "File not found")
        }

        return try {
            if (file.isDirectory) {
                file.deleteRecursively()
            } else {
                file.delete()
            }
            newFixedLengthResponse(Response.Status.OK, "text/plain", "File or directory deleted successfully")
        } catch (e: Exception) {
            e.printStackTrace()
            newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "text/plain", "Failed to delete file or directory")
        }
    }


}