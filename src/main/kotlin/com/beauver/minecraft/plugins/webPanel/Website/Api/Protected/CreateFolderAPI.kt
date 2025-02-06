package com.beauver.minecraft.plugins.webPanel.Website.Api.Protected

import com.beauver.minecraft.plugins.webPanel.Website.Api.WebPanelApi
import fi.iki.elonen.NanoHTTPD.*
import org.bukkit.Bukkit
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths

class CreateFolderAPI : WebPanelApi {
    override val requestMethod: Method = Method.POST

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
        val currentPath = parms["path"]?.get(0) ?: return newFixedLengthResponse(Response.Status.BAD_REQUEST, "text/plain", "Missing path")
        val folderName = parms["folderName"]?.get(0) ?: return newFixedLengthResponse(Response.Status.BAD_REQUEST, "text/plain", "Missing folder name")

        val folderPath = Paths.get(Bukkit.getWorldContainer().path.toString(), currentPath, folderName)

        return try {
            Files.createDirectories(folderPath)
            newFixedLengthResponse(Response.Status.OK, "text/plain", "Folder created successfully")
        } catch (e: IOException) {
            e.printStackTrace()
            newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "text/plain", "Failed to create folder")
        }
    }

}