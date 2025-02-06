package com.beauver.minecraft.plugins.webPanel.Website.Api.Protected

import com.beauver.minecraft.plugins.webPanel.WebPanel
import com.beauver.minecraft.plugins.webPanel.Website.Api.WebPanelApi
import fi.iki.elonen.NanoHTTPD.*
import org.bukkit.Bukkit


class SendToConsoleAPI : WebPanelApi{
    override val requestMethod: Method = Method.POST

    override fun respond(session: IHTTPSession): Response {
        if(compareMethod(session.method).status != Response.Status.OK){
            return compareMethod(session.method)
        }else if(isAuthorized(session).status != Response.Status.OK) {
            return isAuthorized(session)
        }

        execute(session)
        return newFixedLengthResponse(Response.Status.OK, "text/plain", "Executed Command")
    }

    override fun execute(session: IHTTPSession) {
        session.parseBody(null)
        val parms: Map<String, List<String>> = session.parameters
        val command = parms["command"]?.get(0) ?: ""

        Bukkit.getScheduler().runTask(WebPanel.plugin, Runnable {
            Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), command)
        });
    }

}