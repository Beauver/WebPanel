package com.beauver.minecraft.plugins.webPanel.Website.Api.Public

import com.beauver.minecraft.plugins.webPanel.Website.Api.WebPanelApi
import fi.iki.elonen.NanoHTTPD.*
import org.bukkit.Bukkit
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.name

class GetFilesAPI : WebPanelApi {
    override val requestMethod: Method = Method.GET

    override fun respond(session: IHTTPSession): Response {
        if (compareMethod(session.method).status != Response.Status.OK) {
            return compareMethod(session.method)
        }

        return newFixedLengthResponse(Response.Status.OK, "application/json", getData(session))
    }

    override fun getData(session: IHTTPSession): String {
        session.parseBody(null)
        val givenPath = session.parameters["path"]?.get(0)

        var pathString = ""
        val folders = mutableListOf<Path>()
        val files = mutableListOf<File>()

        if (givenPath == ".") {
            val path = Bukkit.getWorldContainer().toPath()
            Files.list(path).use { stream ->
                stream.forEach {
                    if (Files.isDirectory(it)) {
                        folders.add(it)
                    } else {
                        files.add(it.toFile())
                    }
                }
            }
            pathString = "server/"
        }else{
            val path = Paths.get(Bukkit.getWorldContainer().path.toString() + "/" + givenPath)
            Files.list(path).use { stream ->
                stream.forEach {
                    if (Files.isDirectory(it)) {
                        folders.add(it)
                    } else {
                        files.add(it.toFile())
                    }
                }
            }
            pathString = "server/$givenPath"
        }

        return """{
                "path": "$pathString",
                "folders": [${folders.joinToString(",") { "\"${it.name}\"" }}],
                "files": [${files.joinToString(",") { "\"${it.name}\"" }}]
            }"""
    }
}