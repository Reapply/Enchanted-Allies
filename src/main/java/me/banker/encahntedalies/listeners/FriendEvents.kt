package me.banker.encahntedalies.listeners

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import me.banker.encahntedalies.utils.FriendsManager

class FriendEvents(private val friendsManager: FriendsManager) : Listener {

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        friendsManager.addPlayerToDatabaseIfNotExists(event.player.name)
        friendsManager.notifyFriendsPlayerIsOnline(event.player)
        friendsManager.updatePlayerStatus(event.player.name, true) // Player is online
    }

    @EventHandler
    fun onPlayerLeave(event: PlayerQuitEvent) {
        friendsManager.notifyFriendsPlayerIsOffline(event.player)
        friendsManager.updatePlayerStatus(event.player.name, false) // Player is offline
    }
}
