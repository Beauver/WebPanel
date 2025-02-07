package com.beauver.minecraft.plugins.webPanel.Website.Api.Public

import com.beauver.minecraft.plugins.webPanel.WebPanel
import com.beauver.minecraft.plugins.webPanel.Website.Api.WebPanelApi
import fi.iki.elonen.NanoHTTPD
import fi.iki.elonen.NanoHTTPD.Method
import fi.iki.elonen.NanoHTTPD.Response
import fi.iki.elonen.NanoHTTPD.newFixedLengthResponse

class GetPlayersAPI : WebPanelApi{
    override val requestMethod: Method = Method.GET

    override fun respond(session: NanoHTTPD.IHTTPSession): Response {
        if(compareMethod(session.method).status != Response.Status.OK){
            return compareMethod(session.method)
        }

        return newFixedLengthResponse(Response.Status.OK, "application/json", getData())
    }

    /**
     * Gets the PlayerData.
     * @return JSON Data as a string (playerCount, maxPlayers, players)
     */
    override fun getData(): String {
        val players = mutableListOf<String>()

        WebPanel.plugin.server.onlinePlayers.forEach {
            players.add(it.name)
        }

        val playerCount = players.size
        return """{
                "playerCount": $playerCount,
                "maxPlayers": ${WebPanel.plugin.server.maxPlayers},
                "players": [${players.joinToString(",") { "\"$it\"" }}]
            }"""
    }

}