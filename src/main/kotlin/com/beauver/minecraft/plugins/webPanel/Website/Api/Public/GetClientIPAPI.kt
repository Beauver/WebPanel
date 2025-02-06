package com.beauver.minecraft.plugins.webPanel.Website.Api.Public

import com.beauver.minecraft.plugins.webPanel.Website.Api.WebPanelApi
import com.beauver.minecraft.plugins.webPanel.Website.PanelWebsite.Companion.getClientIP
import fi.iki.elonen.NanoHTTPD.*

class GetClientIPAPI : WebPanelApi {
    override val requestMethod: Method = Method.GET

    override fun respond(session: IHTTPSession): Response {
        if (compareMethod(session.method).status != Response.Status.OK) {
            return compareMethod(session.method)
        }

        val clientIP = getClientIP(session)
        val jsonResponse = """{"clientIP": "$clientIP"}"""

        return newFixedLengthResponse(Response.Status.OK, "application/json", jsonResponse)
    }
}