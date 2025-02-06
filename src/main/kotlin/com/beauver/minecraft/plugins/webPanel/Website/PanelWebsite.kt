package com.beauver.minecraft.plugins.webPanel.Website

import com.beauver.minecraft.plugins.webPanel.WebPanel
import com.beauver.minecraft.plugins.webPanel.Website.Api.Protected.*
import com.beauver.minecraft.plugins.webPanel.Website.Api.Public.GetApiKeyAPI
import com.beauver.minecraft.plugins.webPanel.Website.Api.Public.GetConsoleOutputAPI
import com.beauver.minecraft.plugins.webPanel.Website.Api.Public.GetFilesAPI
import com.beauver.minecraft.plugins.webPanel.Website.Api.Public.GetPlayersAPI
import com.beauver.minecraft.plugins.webPanel.Website.Pages.CSSPages
import com.beauver.minecraft.plugins.webPanel.Website.Pages.HTMLPages
import fi.iki.elonen.NanoHTTPD
import org.bukkit.Bukkit
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*
import kotlin.io.path.name


class PanelWebsite() : NanoHTTPD(WebPanel.plugin.config.getInt("website.port")) {

    init {
        start(SOCKET_READ_TIMEOUT, false)
        WebPanel.plugin.logger.info("Started Website")
    }

    override fun serve(session: IHTTPSession): Response {
        when (session.uri) {
            //HTML Pages
            "/", "/index.html", "/index", "/console", "/panel" -> return HTMLPages().serve("website/index.html")
            "/players.html", "/players" -> return HTMLPages().serve("website/players.html")
            "/files.html", "/files" -> return HTMLPages().serve("website/files.html")

            //CSS Pages
            "/css/index.css" -> return CSSPages().serve("website/css/index.css")
            "/css/files.css" -> return CSSPages().serve("website/css/files.css")
            "/css/players.css" -> return CSSPages().serve("website/css/players.css")

            //GET
            "/api/getPlayers" -> return GetPlayersAPI().respond(session)
            "/api/getConsoleOutput" -> return GetConsoleOutputAPI().respond(session)
            "/api/getApiKey" -> return GetApiKeyAPI().respond(session)
            "/api/getFiles" -> return GetFilesAPI().respond(session)
            "/api/downloadFile" -> return DownloadFileAPI().respond(session)

            //POST
            "/api/sendToConsole" -> return SendToConsoleAPI().respond(session)
            "/api/stopServer" -> return StopServerAPI().respond(session)
            "/api/uploadFile" -> return UploadFilesAPI().respond(session)
            "/api/createFolder" -> return CreateFolderAPI().respond(session)

            //DELETE
            "/api/deleteFile" -> return DeleteFileAPI().respond(session)

            else -> return super.serve(session)
        }
    }

    companion object{
        val apiKeysActive = mutableMapOf<String, String>()
        val trustedIPs: MutableList<String> = WebPanel.plugin.config.getStringList("websites.apikey.permittedIPs")

        fun generateSecureApiKey(): String {
            val characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()_+-=[]{}|;:,.<>?";
            val apiKey = StringBuilder();
            for (i in 0..48) {
                val random = Random().nextInt(characters.length)
                apiKey.append(characters[random])
            }
            return apiKey.toString();
        }

        fun isIpInRange(ip: String, range: String): Boolean {
            val (startIp, endIp) = range.split("-")
            val ipToLong = { ip: String -> ip.split(".").map { it.toLong() }.reduce { acc, i -> (acc shl 8) + i } }
            return ipToLong(ip) in ipToLong(startIp)..ipToLong(endIp)
        }
    }
}



