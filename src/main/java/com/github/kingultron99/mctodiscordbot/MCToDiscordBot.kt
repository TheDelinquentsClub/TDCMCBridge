package com.github.kingultron99.mctodiscordbot

import com.github.kingultron99.mctodiscordbot.listeners.ChatListener
import com.github.kingultron99.mctodiscordbot.socket.WebsocketClient
import io.socket.client.Socket
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.apache.commons.lang.StringUtils
import org.bukkit.Statistic
import org.bukkit.attribute.Attribute
import org.bukkit.event.HandlerList
import org.bukkit.plugin.java.JavaPlugin
import org.json.simple.JSONObject
import java.util.*

class MCToDiscordBot : JavaPlugin() {
    lateinit var socket : Socket
    override fun onEnable() {
        // Plugin startup logic
        println("Firing up the handlers!")

        // Register event handlers
        server.pluginManager.registerEvents(ChatListener(), this)

        // Init WS client
        WebsocketClient.createSocketInstance(false)

        socket = WebsocketClient.getSocket()

        socket.on(Socket.EVENT_CONNECT) {
            println("Connected to WS Server!")
            socket.emit("serverinstance")
        }
        socket.on(Socket.EVENT_CONNECT_ERROR) { args ->
            println(args[0])
        }
        socket.on(Socket.EVENT_DISCONNECT) {
            println("Disconnected from WS Server!")
        }
        socket.on("discordmessage") { args ->
            server.sendMessage(Component.
            text(args[1].toString() + ": ").
            color(TextColor.color(0x5865F2)).
            append(Component.text(args[0].toString()).color(TextColor.color(0xffffff))))
        }
        socket.on("getserverinfo") {
            println("Retrieving server info!")
            var msgObj = JSONObject()
            msgObj["tps"] = server.tps[0]
            msgObj["averageTickTime"] = server.averageTickTime
            msgObj["onlinePlayers"] = server.onlinePlayers.size
            msgObj["allPlayers"] = server.offlinePlayers.size
            msgObj["bannedPlayers"] = server.bannedPlayers.size
            msgObj["motd"] = PlainTextComponentSerializer.plainText().serialize(server.motd())
            msgObj["version"] = server.version
            msgObj["maxPlayers"] = server.maxPlayers

            socket.emit("getserverinfo", msgObj.toJSONString())
        }
        socket.on("getplayerinfo") { args ->
            println("Retrieving player info for: " + args[0])
            val msgObj = JSONObject()
            val player = server.getPlayer(args[0].toString())
            val offlinePlayer = server.getOfflinePlayerIfCached(args[0].toString())
            if (player != null) {
                msgObj["displayName"] = PlainTextComponentSerializer.plainText().serialize(player.displayName())
                msgObj["uuid"] = player.uniqueId.toString()
                msgObj["gameMode"] = player.gameMode.toString()
                msgObj["firstPlayed"] = player.firstPlayed
                msgObj["health"] = player.health
                msgObj["maxHealth"] = player.getAttribute(Attribute.GENERIC_MAX_HEALTH)?.value
                msgObj["mobsKilled"] = player.getStatistic(Statistic.MOB_KILLS)
                msgObj["itemsDropped"] = player.getStatistic(Statistic.DROP_COUNT)
                msgObj["animalsBred"] = player.getStatistic(Statistic.ANIMALS_BRED)
                msgObj["deaths"] = player.getStatistic(Statistic.DEATHS)
                msgObj["gamesQuit"] = player.getStatistic(Statistic.LEAVE_GAME)
                msgObj["timePlayed"] = player.getStatistic(Statistic.TOTAL_WORLD_TIME)
                msgObj["isFlying"] = player.isFlying
                msgObj["isSleeping"] = player.isSleeping
                msgObj["isSneaking"] = player.isSneaking
                msgObj["isSprinting"] = player.isSprinting
                msgObj["isoOp"] = player.isOp
                msgObj["online"] = player.isOnline
                msgObj["isBanned"] = player.isBanned
            } else if (offlinePlayer != null) {
                msgObj["displayName"] = offlinePlayer.name.toString()
                msgObj["uuid"] = offlinePlayer.uniqueId.toString()
                msgObj["firstPlayed"] = offlinePlayer.firstPlayed
                msgObj["lastSeen"] = offlinePlayer.lastSeen
                msgObj["mobsKilled"] = offlinePlayer.getStatistic(Statistic.MOB_KILLS)
                msgObj["itemsDropped"] = offlinePlayer.getStatistic(Statistic.DROP_COUNT)
                msgObj["animalsBred"] = offlinePlayer.getStatistic(Statistic.ANIMALS_BRED)
                msgObj["deaths"] = offlinePlayer.getStatistic(Statistic.DEATHS)
                msgObj["gamesQuit"] = offlinePlayer.getStatistic(Statistic.LEAVE_GAME)
                msgObj["timePlayed"] = offlinePlayer.getStatistic(Statistic.TOTAL_WORLD_TIME)
                msgObj["isOp"] = offlinePlayer.isOp
                msgObj["online"] = offlinePlayer.isOnline
                msgObj["isBanned"] = offlinePlayer.isBanned
            } else{
                val playerbyuuid = server.getPlayer(UUID.fromString(args[0].toString()))
                if (playerbyuuid != null){
                    msgObj["displayName"] = PlainTextComponentSerializer.plainText().serialize(playerbyuuid.displayName())
                    msgObj["uuid"] = playerbyuuid.uniqueId.toString()
                    msgObj["gameMode"] = playerbyuuid.gameMode.toString()
                    msgObj["firstPlayed"] = playerbyuuid.firstPlayed
                    msgObj["health"] = playerbyuuid.health
                    msgObj["maxHealth"] = playerbyuuid.getAttribute(Attribute.GENERIC_MAX_HEALTH)?.value
                    msgObj["mobsKilled"] = playerbyuuid.getStatistic(Statistic.MOB_KILLS)
                    msgObj["itemsDropped"] = playerbyuuid.getStatistic(Statistic.DROP_COUNT)
                    msgObj["animalsBred"] = playerbyuuid.getStatistic(Statistic.ANIMALS_BRED)
                    msgObj["deaths"] = playerbyuuid.getStatistic(Statistic.DEATHS)
                    msgObj["gamesQuit"] = playerbyuuid.getStatistic(Statistic.LEAVE_GAME)
                    msgObj["timePlayed"] = playerbyuuid.getStatistic(Statistic.TOTAL_WORLD_TIME)
                    msgObj["isFlying"] = playerbyuuid.isFlying
                    msgObj["isSleeping"] = playerbyuuid.isSleeping
                    msgObj["isSneaking"] = playerbyuuid.isSneaking
                    msgObj["isSprinting"] = playerbyuuid.isSprinting
                    msgObj["isoOp"] = playerbyuuid.isOp
                    msgObj["online"] = playerbyuuid.isOnline
                    msgObj["isBanned"] = playerbyuuid.isBanned
                } else {
                    msgObj["error"] = "Player not found!"
                }
            }
            socket.emit("getplayerinfo", msgObj.toJSONString())
        }

        // Command Invokes
        socket.on("restart") {
            server.shutdown()
        }
        socket.on("announce") { args ->
            println(args[0])

            server.sendMessage(Component.text("[ANNOUNCEMENT]: ").
            color(TextColor.color(0xFABC3C)).decorate(TextDecoration.BOLD).
            append(Component.text(args[0].toString()).
            color(TextColor.color(0xFFB238)).decoration(TextDecoration.BOLD, false)))
        }
        socket.on("msg") { args ->
            val playerStr = args[1].toString().split(" ")[0]
            val msg = StringUtils.join(args[1].toString().split(" ").subList(1, args[1].toString().split(" ").lastIndex+1), " ")
            var player = server.getPlayer(playerStr)
            if (player != null) {
                player.sendMessage(MiniMessage.miniMessage().deserialize("<color:#5865F2>(discord) " + args[0].toString() + " → You:</color> ")
                    .append(MiniMessage.miniMessage().deserialize(msg))
                    .color(TextColor.color(NamedTextColor.GRAY)))
            } else {
                socket.emit("playernotfound")
            }
        }
        socket.on("kill") { args ->
            println(args[0])

            val player = server.getPlayer(args[0].toString())
            if (player != null) {
                player.health = 0.0
            } else {
                socket.emit("playernotfound")
            }
        }
        // Start ws client
        socket.connect()
    }

    override fun onDisable() {
        // Plugin shutdown logic
        println("Shutting Down...")
        socket.disconnect()
        HandlerList.unregisterAll()
    }
}

