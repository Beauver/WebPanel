package com.beauver.minecraft.plugins.webPanel.Website.Api

import com.beauver.minecraft.plugins.webPanel.Website.PanelWebsite
import com.beauver.minecraft.plugins.webPanel.Website.PanelWebsite.Companion.getClientIP
import fi.iki.elonen.NanoHTTPD.*

interface WebPanelApi {
    val requestMethod: Method;

    /**
     * All in 1 method to respond to the request
     */
    fun respond(session: IHTTPSession): Response

    /**
     * Gets data for the request. Meant for GET requests
     */
    fun getData(): Any{
        return Any();
    }

    /**
     * Gets data for the request. Meant for GET requests
     * @param session The session data
     */
    fun getData(session: IHTTPSession): Any{
        return Any();
    }

    /**
     * Executes something. Meant for POST requests
     */
    fun execute(): Any {
        return Any();
    }

    /**
     * Executes something. Meant for POST requests
     * @param session The session data
     */
    fun execute(session: IHTTPSession): Any{
        return Any();
    }

    /**
     * Compares the method of the request with the method of this API (GET, POST, ETC)
     * @param method The method of the request
     * @return a response with the respective status code
     */
    fun compareMethod(method: Method): Response{
        if(method != requestMethod){
            return newFixedLengthResponse(Response.Status.METHOD_NOT_ALLOWED, "text/plain", "Method not allowed")
        }
        return newFixedLengthResponse(Response.Status.OK, "text/plain", "Method allowed")
    }

    /**
     * Whether the request is authorized to access this API request
     * @param session The session data
     * @return a response with the respective status code
     */
    fun isAuthorized(session: IHTTPSession): Response {
        val headers = session.headers
        val apiKey = headers["authorization"]?.substringAfter("Bearer ")
            ?: return newFixedLengthResponse(Response.Status.UNAUTHORIZED, "text/plain", "Missing API Key")

        val ipAddress = getClientIP(session) ?: return newFixedLengthResponse(Response.Status.FORBIDDEN, "text/plain", "Forbidden")

        if (PanelWebsite.apiKeysActive[ipAddress] != apiKey) {
            return newFixedLengthResponse(Response.Status.UNAUTHORIZED, "text/plain", "Invalid API Key")
        } else {
            PanelWebsite.apiKeysActive.remove(ipAddress)
            return newFixedLengthResponse(Response.Status.OK, "text/plain", "Authorized")
        }
    }


}