package com.github.kingultron99.mctodiscordbot.socket

import com.github.kingultron99.mctodiscordbot.MCToDiscordBot
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.engineio.client.transports.WebSocket
import java.net.URI
import java.net.URISyntaxException


object WebsocketClient {
    private lateinit var client : Socket
    private var uri : URI = URI.create("http://localhost:3000")

    fun createSocketInstance(isDev : Boolean) {
        println("Creating Websocket Client")
        try {
            val options = IO.Options()
            options.transports = arrayOf(WebSocket.NAME)
            client = IO.socket(uri, options)
        } catch (e: URISyntaxException) {
            error(e)
        }
    }

    fun getSocket(): Socket {
        return client
    }

}