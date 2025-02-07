package com.beauver.minecraft.plugins.webPanel.Website.Api.Public

import com.beauver.minecraft.plugins.webPanel.WebPanel
import com.beauver.minecraft.plugins.webPanel.Website.Api.WebPanelApi
import fi.iki.elonen.NanoHTTPD.*
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths

class GetConsoleOutputAPI : WebPanelApi {
    override val requestMethod: Method = Method.GET

    override fun respond(session: IHTTPSession): Response {
        if (compareMethod(session.method).status != Response.Status.OK) {
            return compareMethod(session.method)
        }

        return newFixedLengthResponse(Response.Status.OK, "application/json", getData())
    }

    /**
     * Gets the console data.
     * @return JSON Data as a string (messages)
     */
    override fun getData(): String {
        val messages = mutableListOf<String>()

        val logFilePath = Paths.get("logs/latest.log")
        try {
            Files.newBufferedReader(logFilePath).use { reader ->
                val lines = reader.readLines()
                val maxLines = WebPanel.plugin.config.getInt("website.console.maxlines")
                val start = if (lines.size > maxLines) lines.size - maxLines else 0
                for (i in start until lines.size) {
                    messages.add(lines[i])
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return """{
                "messages": [${messages.joinToString(",") { "\"${it.replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t")
            .replace("\b", "\\b")
            .replace("\u000C", "\\f")}\"" }}]
            }"""
    }
}