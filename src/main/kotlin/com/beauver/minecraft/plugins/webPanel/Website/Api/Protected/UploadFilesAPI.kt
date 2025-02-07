package com.beauver.minecraft.plugins.webPanel.Website.Api.Protected

import com.beauver.minecraft.plugins.webPanel.WebPanel
import com.beauver.minecraft.plugins.webPanel.Website.Api.WebPanelApi
import fi.iki.elonen.NanoHTTPD.*
import org.bukkit.Bukkit
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths

class UploadFilesAPI : WebPanelApi {
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
        val files = mutableMapOf<String, String>()
        val parameters = session.parameters

        try {
            session.parseBody(files)
        } catch (e: Exception) {
            e.printStackTrace()
            return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "text/plain", "Failed to parse request body")
        }

        val tempFilePath = files["file"]
            ?: return newFixedLengthResponse(Response.Status.BAD_REQUEST, "text/plain", "File not found in request body")

        val tempFile = File(tempFilePath)

        if (!tempFile.exists()) {
            return newFixedLengthResponse(Response.Status.BAD_REQUEST, "text/plain", "Uploaded file does not exist")
        }

        // Check file size (e.g., limit to 5MB)
        val maxFileSize = WebPanel.plugin.config.getInt("api.uploadFile.maxSizeMb") * 1024 * 1024
        if (tempFile.length() > maxFileSize) {
            return newFixedLengthResponse(Response.Status.PAYLOAD_TOO_LARGE, "text/plain", "File size exceeds the limit of 5MB")
        }

        val relativePath = parameters["path"]?.get(0) ?: ""
        val originalFileName = parameters["filename"]?.get(0) ?: tempFile.name
        val destinationPath = Paths.get(Bukkit.getWorldContainer().path.toString(), relativePath, originalFileName)

        try {
            Files.createDirectories(destinationPath.parent)
            tempFile.inputStream().use { input ->
                Files.newOutputStream(destinationPath).use { output ->
                    input.copyTo(output)
                    input.close()
                }
            }

        } catch (e: IOException) {
            e.printStackTrace()
            return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "text/plain", "Failed to save file")
        }

        return newFixedLengthResponse(Response.Status.OK, "text/plain", "File uploaded successfully to $destinationPath")
    }


}