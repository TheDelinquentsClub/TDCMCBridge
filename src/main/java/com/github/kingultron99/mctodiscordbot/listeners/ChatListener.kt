package com.github.kingultron99.mctodiscordbot.listeners

import com.github.kingultron99.mctodiscordbot.socket.WebsocketClient
import io.papermc.paper.event.player.AsyncChatEvent
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerPreLoginEvent
import org.bukkit.event.player.PlayerAdvancementDoneEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.json.simple.JSONObject


class ChatListener : Listener {
    @EventHandler(priority = EventPriority.HIGHEST)
    fun onMessage(e: AsyncChatEvent) {
        if (e.isAsynchronous) {
            if (e.player.hasPermission("hyperchat.allowchat")) {
                var string = PlainTextComponentSerializer.plainText().serialize(e.message())
                var msgObj = JSONObject()
                msgObj["username"] = PlainTextComponentSerializer.plainText().serialize(e.player.displayName())
                msgObj["msg"] = string.replace("(<[A-z]+:[A-z0-9#!.]+:?[\\/#!\"'A-z0-9.\\s]+(:?[\"'A-z0-9#!\\/.\\s]*)?(:?[\"'A-z0-9#!\\/.\\s]*)?(:?[\"'A-z0-9#!\\/.\\s]*)?>?)|(<[A-z0-9:!#'\"<>]+>)|(<\\/[A-z]+>)".toRegex(), "")
                WebsocketClient.getSocket().emit("playerchat", msgObj.toJSONString())
            }
        }
    }
    @EventHandler
    fun onPlayerPreLogin(e: AsyncPlayerPreLoginEvent) {
//        if (e.uniqueId.toString() != "a01e6c0e-7b89-4347-9835-c4496e3d2f41") {
//            e.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, MiniMessage.miniMessage()
//                .deserialize("<color:red><bold>WOAH THERE!</bold></color>\n\n" +
//                        "The server is currently in dev/maintenance mode!\nPlease check back later and check our discord for updates :D"))
//        } else {
            e.allow()
//        }
    }
    @EventHandler
    fun onPlayerJoin(e: PlayerJoinEvent) {
        var bool = e.player.hasPlayedBefore()
        var msgObj = JSONObject()
        msgObj["username"] = PlainTextComponentSerializer.plainText().serialize(e.player.displayName())
        msgObj["uuid"] = e.player.uniqueId.toString()
        msgObj["playedbefore"] = bool

        WebsocketClient.getSocket().emit("playerjoin", msgObj.toJSONString())

    }
    @EventHandler
    fun onPlayerLeave(e: PlayerQuitEvent) {
        val player = e.player.displayName()
        WebsocketClient.getSocket().emit("playerleft", PlainTextComponentSerializer.plainText().serialize(player), e.reason)
    }
    @EventHandler
    fun onPlayerAdvancement(e: PlayerAdvancementDoneEvent) {
        var title = e.advancement.display?.title()
        println(e.advancement)

        var msgObj= JSONObject()
        msgObj["player"] = PlainTextComponentSerializer.plainText().serialize(e.player.displayName())
        msgObj["type"] = e.advancement.display?.frame().toString()
        msgObj["advancement"] = title?.let { PlainTextComponentSerializer.plainText().serialize(it) }
        msgObj["icon"] = e.advancement.display?.icon()?.type?.name

        WebsocketClient.getSocket().emit("playeradvancement", msgObj.toJSONString())
    }
}