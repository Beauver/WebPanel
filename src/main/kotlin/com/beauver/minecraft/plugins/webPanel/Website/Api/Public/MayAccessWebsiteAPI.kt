package com.beauver.minecraft.plugins.webPanel.Website.Api.Public

import com.beauver.minecraft.plugins.webPanel.WebPanel
import com.beauver.minecraft.plugins.webPanel.Website.Api.WebPanelApi
import com.beauver.minecraft.plugins.webPanel.Website.PanelWebsite.Companion.getClientIP
import com.beauver.minecraft.plugins.webPanel.Website.PanelWebsite.Companion.isIpInRange
import com.beauver.minecraft.plugins.webPanel.Website.PanelWebsite.Companion.trustedIPs
import fi.iki.elonen.NanoHTTPD.*

class MayAccessWebsiteAPI : WebPanelApi {
    override val requestMethod: Method = Method.GET

    override fun respond(session: IHTTPSession): Response {
        if(compareMethod(session.method).status != Response.Status.OK){
            return compareMethod(session.method)
        }

        return newFixedLengthResponse(Response.Status.OK, "application/json", getData(session).toString())
    }

    override fun getData(session: IHTTPSession): Boolean {
        val value = WebPanel.plugin.config.getBoolean("website.mayOpenIfNotPermittedIPList") ?: false

        if(!value){
            val ipAddress = getClientIP(session) ?: return false
            if (!trustedIPs.any { it == ipAddress || (it.contains("-") && isIpInRange(ipAddress, it)) }) {
                return false
            }
        }
        return true
    }
}