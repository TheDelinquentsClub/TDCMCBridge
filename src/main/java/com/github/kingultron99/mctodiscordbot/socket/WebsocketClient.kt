package com.github.kingultron99.mctodiscordbot.socket

import com.github.kingultron99.mctodiscordbot.MCToDiscordBot
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.engineio.client.transports.WebSocket
import java.net.URI
import java.net.URISyntaxException


object WebsocketClient {
    private lateinit var client : Socket
    lateinit var uri : URI
    private val prod: URI = URI.create("http://192.168.1.104:3000")
    private val dev: URI = URI.create("http://localhost:3000")

    fun createSocketInstance(isDev : Boolean) {
        println("Creating Websocket Client")
        uri = if (isDev) {
            dev
        } else {
            prod
        }
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