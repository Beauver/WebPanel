package com.beauver.minecraft.plugins.webPanel.Website.Api.Public

import com.beauver.minecraft.plugins.webPanel.WebPanel
import com.beauver.minecraft.plugins.webPanel.Website.Api.WebPanelApi
import com.beauver.minecraft.plugins.webPanel.Website.PanelWebsite
import com.beauver.minecraft.plugins.webPanel.Website.PanelWebsite.Companion.apiKeysActive
import com.beauver.minecraft.plugins.webPanel.Website.PanelWebsite.Companion.generateSecureApiKey
import com.beauver.minecraft.plugins.webPanel.Website.PanelWebsite.Companion.isIpInRange
import com.beauver.minecraft.plugins.webPanel.Website.PanelWebsite.Companion.trustedIPs
import fi.iki.elonen.NanoHTTPD.*
import org.bukkit.Bukkit

class GetApiKeyAPI : WebPanelApi {
    override val requestMethod: Method = Method.GET

    override fun respond(session: IHTTPSession): Response {
        val ipAddress = session.headers["remote-addr"] ?: return newFixedLengthResponse(Response.Status.FORBIDDEN, "text/plain", "Forbidden")
        if (!trustedIPs.any { it == ipAddress || (it.contains("-") && isIpInRange(ipAddress, it)) }) {
            return newFixedLengthResponse(Response.Status.UNAUTHORIZED, "text/plain", "Unauthorized")
        }

        return newFixedLengthResponse(Response.Status.OK, "text/plain", getData(session))
    }

    override fun getData(session: IHTTPSession): String {
        val ipAddress = session.headers["remote-addr"] ?: return "Forbidden";

        val apiKey = generateSecureApiKey()
        apiKeysActive[ipAddress] = apiKey

        // Auto-expire key
        Bukkit.getScheduler().runTaskLater(WebPanel.plugin, Runnable {
            apiKeysActive.remove(ipAddress)
        }, WebPanel.plugin.config.getLong("website.apikey.timeoutSeconds") * 20L)

        return apiKey
    }
}