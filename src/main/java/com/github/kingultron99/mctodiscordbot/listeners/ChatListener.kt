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
                var string = PlainTextComponentSerializer.plainText().serialize(e.message())
                var msgObj = JSONObject()
                msgObj["username"] = PlainTextComponentSerializer.plainText().serialize(e.player.displayName())
                msgObj["msg"] = string.replace("(<[A-z]+:[A-z0-9#!.]+:?[\\/#!\"'A-z0-9.\\s]+(:?[\"'A-z0-9#!\\/.\\s]*)?(:?[\"'A-z0-9#!\\/.\\s]*)?(:?[\"'A-z0-9#!\\/.\\s]*)?>?)|(<[A-z0-9:!#'\"<>]+>)|(<\\/[A-z]+>)".toRegex(), "")
                WebsocketClient.getSocket().emit("playerchat", msgObj.toJSONString())
        }
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
        if (e.advancement.display!!.doesAnnounceToChat()) {
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
}