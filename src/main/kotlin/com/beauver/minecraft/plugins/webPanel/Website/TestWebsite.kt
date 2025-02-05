package com.beauver.minecraft.plugins.webPanel.Website

import com.beauver.minecraft.plugins.webPanel.WebPanel
import fi.iki.elonen.NanoHTTPD
import org.bukkit.Bukkit
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.util.*
import kotlin.io.path.name
import kotlin.io.path.pathString


class TestWebsite() : NanoHTTPD(WebPanel.plugin.config.getInt("website.port")) {

    val apiKeysActive = mutableMapOf<String, String>()
    val trustedIPs = WebPanel.plugin.config.getStringList("websites.apikey.permittedIPs")

    init {
        start(SOCKET_READ_TIMEOUT, false)
        WebPanel.plugin.logger.info("Started Website")
    }

    override fun serve(session: IHTTPSession): Response {
        // Serve the main HTML file
        if (session.uri == "/" || session.uri == "/index.html") {
            WebPanel::class.java.classLoader.getResourceAsStream("website/index.html")?.let { inputStream ->
                return newFixedLengthResponse(Response.Status.OK, "text/html", inputStream, inputStream.available().toLong())
            } ?: return newFixedLengthResponse(Response.Status.NOT_FOUND, "text/plain", "File not found")
        }

        if (session.uri == "/players.html") {
            WebPanel::class.java.classLoader.getResourceAsStream("website/players.html")?.let { inputStream ->
                return newFixedLengthResponse(Response.Status.OK, "text/html", inputStream, inputStream.available().toLong())
            } ?: return newFixedLengthResponse(Response.Status.NOT_FOUND, "text/plain", "File not found")
        }

        if(session.uri == "/files.html"){
            WebPanel::class.java.classLoader.getResourceAsStream("website/files.html")?.let { inputStream ->
                return newFixedLengthResponse(Response.Status.OK, "text/html", inputStream, inputStream.available().toLong())
            } ?: return newFixedLengthResponse(Response.Status.NOT_FOUND, "text/plain", "File not found")
        }

        if (session.uri == "/css/index.css") {
            // Serve the CSS file
            WebPanel::class.java.classLoader.getResourceAsStream("website/css/index.css")?.let { inputStream ->
                return newFixedLengthResponse(Response.Status.OK, "text/css", inputStream, inputStream.available().toLong())
            } ?: return newFixedLengthResponse(Response.Status.NOT_FOUND, "text/plain", "CSS file not found")
        }
        if (session.uri == "/css/files.css") {
            // Serve the CSS file
            WebPanel::class.java.classLoader.getResourceAsStream("website/css/files.css")?.let { inputStream ->
                return newFixedLengthResponse(Response.Status.OK, "text/css", inputStream, inputStream.available().toLong())
            } ?: return newFixedLengthResponse(Response.Status.NOT_FOUND, "text/plain", "CSS file not found")
        }
        if (session.uri == "/css/players.css") {
            // Serve the CSS file
            WebPanel::class.java.classLoader.getResourceAsStream("website/css/players.css")?.let { inputStream ->
                return newFixedLengthResponse(Response.Status.OK, "text/css", inputStream, inputStream.available().toLong())
            } ?: return newFixedLengthResponse(Response.Status.NOT_FOUND, "text/plain", "CSS file not found")
        }

        if (session.uri == "/getPlayers") {
            // Example of player data
            val players = mutableListOf<String>()

            WebPanel.plugin.server.onlinePlayers.forEach {
                players.add(it.name)
            }

            val playerCount = players.size
            val jsonResponse = """{
                "playerCount": $playerCount,
                "maxPlayers": ${WebPanel.plugin.server.maxPlayers},
                "players": [${players.joinToString(",") { "\"$it\"" }}]
            }"""

            return newFixedLengthResponse(Response.Status.OK, "application/json", jsonResponse)
        }

        if (session.uri == "/getConsoleOutput") {
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

            val jsonResponse = """{
                "messages": [${messages.joinToString(",") { "\"${it.replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\n", "\\n")
                    .replace("\r", "\\r")
                    .replace("\t", "\\t")
                    .replace("\b", "\\b")
                    .replace("\u000C", "\\f")}\"" }}]
            }"""

            return newFixedLengthResponse(Response.Status.OK, "application/json", jsonResponse)
        }

        if (session.uri == "/sendToConsole" && session.method == Method.POST) {
            val headers = session.headers
            val apiKey = headers["authorization"]?.substringAfter("Bearer ") ?: return newFixedLengthResponse(Response.Status.UNAUTHORIZED, "text/plain", "Missing API Key")

            val ipAddress = session.headers["remote-addr"] ?: return newFixedLengthResponse(Response.Status.FORBIDDEN, "text/plain", "Forbidden")

            if (apiKeysActive[ipAddress] != apiKey) {
                return newFixedLengthResponse(Response.Status.UNAUTHORIZED, "text/plain", "Invalid API Key")
            } else {
                apiKeysActive.remove(ipAddress)
            }

            session.parseBody(null)
            val parms: Map<String, List<String>> = session.parameters
            val command = parms["command"]?.get(0) ?: ""

            Bukkit.getScheduler().runTask(WebPanel.plugin, Runnable {
                Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), command)
            });

            return newFixedLengthResponse(Response.Status.OK, "text/plain", "")
        }

        if (session.uri == "/stopServer" && session.method == Method.POST) {
            val headers = session.headers
            val apiKey = headers["authorization"]?.substringAfter("Bearer ") ?: return newFixedLengthResponse(Response.Status.UNAUTHORIZED, "text/plain", "Missing API Key")

            val ipAddress = session.headers["remote-addr"] ?: return newFixedLengthResponse(Response.Status.FORBIDDEN, "text/plain", "Forbidden")

            if (apiKeysActive[ipAddress] != apiKey) {
                return newFixedLengthResponse(Response.Status.UNAUTHORIZED, "text/plain", "Invalid API Key")
            } else {
                apiKeysActive.remove(ipAddress)
            }

            WebPanel.plugin.server.shutdown()
            return newFixedLengthResponse(Response.Status.OK, "text/plain", "Stopped Server")
        }

        if (session.uri == "/getApiKey" && session.method == Method.GET) {
            val ipAddress = session.headers["remote-addr"] ?: return newFixedLengthResponse(Response.Status.FORBIDDEN, "text/plain", "Forbidden")

            if (!trustedIPs.any { it == ipAddress || (it.contains("-") && isIpInRange(ipAddress, it)) }) {
                return newFixedLengthResponse(Response.Status.UNAUTHORIZED, "text/plain", "Unauthorized")
            }

            val apiKey = generateSecureApiKey()
            apiKeysActive[ipAddress] = apiKey

            // Auto-expire key
            Bukkit.getScheduler().runTaskLater(WebPanel.plugin, Runnable {
                apiKeysActive.remove(ipAddress)
            }, WebPanel.plugin.config.getLong("website.apikey.timeoutSeconds") * 20L)

            return newFixedLengthResponse(Response.Status.OK, "text/plain", apiKey)
        }

        if(session.uri == "/getFiles" && session.method == Method.POST){
            session.parseBody(null)
            val parms: Map<String, List<String>> = session.parameters
            val givenPath = parms["path"]?.get(0)

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

            val jsonResponse = """{
                "path": "$pathString",
                "folders": [${folders.joinToString(",") { "\"${it.name}\"" }}],
                "files": [${files.joinToString(",") { "\"${it.name}\"" }}]
            }"""

            return newFixedLengthResponse(Response.Status.OK, "application/json", jsonResponse)
        }

        if (session.uri == "/uploadFile" && session.method == Method.POST) {
            val headers = session.headers
            val apiKey = headers["authorization"]?.substringAfter("Bearer ")
                ?: return newFixedLengthResponse(Response.Status.UNAUTHORIZED, "text/plain", "Missing API Key")

            val ipAddress = session.remoteIpAddress
                ?: return newFixedLengthResponse(Response.Status.FORBIDDEN, "text/plain", "Forbidden")

            if (apiKeysActive[ipAddress] != apiKey) {
                return newFixedLengthResponse(Response.Status.UNAUTHORIZED, "text/plain", "Invalid API Key")
            } else {
                apiKeysActive.remove(ipAddress)
            }

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

            val relativePath = parameters["path"]?.get(0) ?: ""
            val originalFileName = parameters["filename"]?.get(0) ?: tempFile.name
            val destinationPath = Paths.get(Bukkit.getWorldContainer().path.toString(), relativePath, originalFileName)

            try {
                Files.createDirectories(destinationPath.parent)
                tempFile.inputStream().use { input ->
                    Files.newOutputStream(destinationPath).use { output ->
                        input.copyTo(output)
                    }
                }

                tempFile.delete()

            } catch (e: IOException) {
                e.printStackTrace()
                return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "text/plain", "Failed to save file")
            }

            return newFixedLengthResponse(Response.Status.OK, "text/plain", "File uploaded successfully to $destinationPath")
        }

        if (session.uri == "/deleteFile" && session.method == Method.POST) {
            val headers = session.headers
            val apiKey = headers["authorization"]?.substringAfter("Bearer ")
                ?: return newFixedLengthResponse(Response.Status.UNAUTHORIZED, "text/plain", "Missing API Key")

            val ipAddress = session.remoteIpAddress
                ?: return newFixedLengthResponse(Response.Status.FORBIDDEN, "text/plain", "Forbidden")

            if (apiKeysActive[ipAddress] != apiKey) {
                return newFixedLengthResponse(Response.Status.UNAUTHORIZED, "text/plain", "Invalid API Key")
            } else {
                apiKeysActive.remove(ipAddress)
            }

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

        if (session.uri == "/createFolder" && session.method == Method.POST) {
            val headers = session.headers
            val apiKey = headers["authorization"]?.substringAfter("Bearer ")
                ?: return newFixedLengthResponse(Response.Status.UNAUTHORIZED, "text/plain", "Missing API Key")

            val ipAddress = session.remoteIpAddress
                ?: return newFixedLengthResponse(Response.Status.FORBIDDEN, "text/plain", "Forbidden")

            if (apiKeysActive[ipAddress] != apiKey) {
                return newFixedLengthResponse(Response.Status.UNAUTHORIZED, "text/plain", "Invalid API Key")
            } else {
                apiKeysActive.remove(ipAddress)
            }

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

        if(session.uri == "/downloadFile" && session.method == Method.GET) {
            val headers = session.headers
            val apiKey = headers["authorization"]?.substringAfter("Bearer ")
                ?: return newFixedLengthResponse(Response.Status.UNAUTHORIZED, "text/plain", "Missing API Key")

            val ipAddress = session.remoteIpAddress
                ?: return newFixedLengthResponse(Response.Status.FORBIDDEN, "text/plain", "Forbidden")

            if (apiKeysActive[ipAddress] != apiKey) {
                return newFixedLengthResponse(Response.Status.UNAUTHORIZED, "text/plain", "Invalid API Key")
            } else {
                apiKeysActive.remove(ipAddress)
            }

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

        // Handle other URIs or pass it to the default response
        return super.serve(session)
    }

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



