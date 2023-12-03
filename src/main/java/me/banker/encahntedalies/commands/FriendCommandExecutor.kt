package me.banker.encahntedalies.commands

import me.banker.encahntedalies.data.FriendRequest
import me.banker.encahntedalies.utils.FriendsManager

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.entity.Player
import revxrsal.commands.CommandHandler
import revxrsal.commands.bukkit.BukkitCommandActor
import revxrsal.commands.annotation.Command
import revxrsal.commands.annotation.Description
import revxrsal.commands.annotation.Named

class FriendCommandExecutor(
    private val friendsManager: FriendsManager,
    commandHandler: CommandHandler
) {

    init {
        commandHandler.register(this)
    }

    @Command("friend")
    @Description("Manage your friends list")
    fun friendCommand(
        actor: BukkitCommandActor,
        @Named("subcommand") subcommand: String,
        @Named("targetPlayerName") targetPlayerName: String?
    ) {
        val player = actor.sender as? Player ?: run {
            actor.reply(Component.text("This command can only be used by players."))
            return
        }

        when (subcommand.toLowerCase()) {
            "add" -> targetPlayerName?.let { handleAddFriend(player, it) }
            "accept" -> targetPlayerName?.let { handleAcceptFriend(player, it) }
            "deny" -> targetPlayerName?.let { handleDenyFriend(player, it) }
            "remove" -> targetPlayerName?.let { handleRemoveFriend(player, it) }
            "list" -> handleListFriends(player)
            "requests" -> handleListRequests(player)
            else -> player.sendMessage(Component.text("Usage: /friend <add/accept/deny/remove/list> <playerName>"))
        }
    }

    private fun handleListFriends(player: Player) {
        friendsManager.getFriends(player.name) { friendsList ->
            val friendListBuilder = Component.text().content("Your friends: ")

            val onlineFriends = mutableListOf<Component>()
            val offlineFriends = mutableListOf<Component>()

            friendsList.forEach { friendName ->
                val friend = player.server.getPlayerExact(friendName)
                val friendComponent = Component.text(friendName)
                    .color(if (friend?.isOnline == true) NamedTextColor.GREEN else NamedTextColor.GRAY)

                if (friend?.isOnline == true) {
                    onlineFriends.add(friendComponent)
                } else {
                    offlineFriends.add(friendComponent)
                }
            }

            onlineFriends.forEach { component ->
                friendListBuilder.append(component).append(Component.text(", ").color(NamedTextColor.WHITE))
            }

            if (onlineFriends.isNotEmpty() && offlineFriends.isNotEmpty()) {
                friendListBuilder.append(Component.text(" "))
            }

            offlineFriends.forEachIndexed { index, component ->
                friendListBuilder.append(component)
                if (index < offlineFriends.size - 1) {
                    friendListBuilder.append(Component.text(", ").color(NamedTextColor.WHITE))
                }
            }

            if (friendsList.isNotEmpty()) {
                player.sendMessage(friendListBuilder.build())
            } else {
                player.sendMessage(Component.text("You have no friends.").color(NamedTextColor.RED))
            }
        }
    }

    private fun handleListRequests(player: Player) {
        val pendingRequests = friendsManager.pendingFriendRequests
        if (pendingRequests.isEmpty()) {
            player.sendMessage(Component.text("You have no pending friend requests.").color(NamedTextColor.GRAY))
        } else {
            val requestListBuilder = Component.text().content("Pending friend requests:").color(NamedTextColor.WHITE)
            pendingRequests.forEach { request ->
                requestListBuilder.append(Component.text("- ${request.sender}").color(NamedTextColor.WHITE))
            }
            player.sendMessage(requestListBuilder.build())
        }
    }

    private fun handleRemoveFriend(player: Player, targetPlayerName: String) {
        friendsManager.removeFriend(player.name, targetPlayerName)
        player.sendMessage(
            Component.text(
                "You are no longer friends with $targetPlayerName.",
                NamedTextColor.WHITE
            )
        )
    }

    private fun handleDenyFriend(player: Player, targetPlayerName: String) {
        if (friendsManager.isRequestPending(player.name, targetPlayerName)) {
            friendsManager.denyFriendRequest(player.name, targetPlayerName)
            player.sendMessage(
                Component.text(
                    "You have denied the friend request from $targetPlayerName.",
                    NamedTextColor.WHITE
                )
            )
        } else {
            player.sendMessage(Component.text("No friend request found from $targetPlayerName.").color(NamedTextColor.RED))
        }
    }

    private fun handleAddFriend(player: Player, targetPlayerName: String) {
        if (player.name.equals(targetPlayerName, ignoreCase = true)) {
            player.sendMessage(
                Component.text(
                    "You cannot send a friend request to yourself.",
                    NamedTextColor.RED
                )
            )
            return
        }

        val targetPlayer = player.server.getPlayerExact(targetPlayerName)
        if (targetPlayer == null) {
            player.sendMessage(Component.text("Player not found.", NamedTextColor.RED))
            return
        }

        if (friendsManager.isRequestPending(player.name, targetPlayerName)) {
            player.sendMessage(
                Component.text(
                    "A pending friend request already exists to $targetPlayerName.",
                    NamedTextColor.YELLOW
                )
            )
            return
        }

        if (friendsManager.areFriends(player.name, targetPlayerName)) {
            player.sendMessage(
                Component.text(
                    "You are already friends with $targetPlayerName.",
                    NamedTextColor.YELLOW
                )
            )
            return
        }

        friendsManager.addFriendRequest(FriendRequest(player.name, targetPlayerName))
        player.sendMessage(Component.text("Friend request sent to $targetPlayerName.", NamedTextColor.GREEN))

        // Send a clickable text message to the target player
        val acceptText = Component.text("[Accept]", NamedTextColor.GREEN)
            .clickEvent(ClickEvent.runCommand("/friend accept ${player.name}"))
            .hoverEvent(HoverEvent.showText(Component.text("Click to accept")))

        val denyText = Component.text("[Deny]", NamedTextColor.RED)
            .clickEvent(ClickEvent.runCommand("/friend deny ${player.name}"))
            .hoverEvent(HoverEvent.showText(Component.text("Click to deny")))

        // Create the full message by appending the components
        val message = Component.text()
            .append(Component.text("${player.name} wants to be your friend. ", NamedTextColor.AQUA))
            .append(acceptText)
            .append(Component.text(" "))
            .append(denyText)
            .build()

        // Send the message to the target player
        targetPlayer.sendMessage(message)
    }

    private fun handleAcceptFriend(player: Player, targetPlayerName: String) {
        if (friendsManager.isRequestPending(targetPlayerName, player.name)) {
            friendsManager.acceptFriendRequest(targetPlayerName, player.name)
            player.sendMessage(Component.text("You are now friends with $targetPlayerName.", NamedTextColor.GREEN))
            val targetPlayer = player.server.getPlayerExact(targetPlayerName)
            targetPlayer?.sendMessage(Component.text("${player.name} has accepted your friend request.", NamedTextColor.GREEN))
        } else {
            player.sendMessage(Component.text("No friend request found from $targetPlayerName.", NamedTextColor.RED))
        }
    }
}
