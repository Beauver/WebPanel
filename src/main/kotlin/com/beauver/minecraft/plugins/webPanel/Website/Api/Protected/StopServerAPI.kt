package com.beauver.minecraft.plugins.webPanel.Website.Api.Protected

import com.beauver.minecraft.plugins.webPanel.WebPanel
import com.beauver.minecraft.plugins.webPanel.Website.Api.WebPanelApi
import fi.iki.elonen.NanoHTTPD.*

class StopServerAPI : WebPanelApi {
    override val requestMethod: Method = Method.POST

    override fun respond(session: IHTTPSession): Response {
        if(compareMethod(session.method).status != Response.Status.OK){
            return compareMethod(session.method)
        }else if(isAuthorized(session).status != Response.Status.OK) {
            return isAuthorized(session)
        }

        execute()
        return newFixedLengthResponse(Response.Status.OK, "text/plain", "Stopped Server")
    }

    override fun execute() {
        WebPanel.plugin.server.shutdown()
    }
}