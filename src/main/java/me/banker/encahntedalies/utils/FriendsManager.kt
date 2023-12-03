package me.banker.encahntedalies.utils

import com.mongodb.client.MongoClients
import com.mongodb.client.MongoDatabase
import com.mongodb.client.model.Filters
import com.mongodb.client.model.Updates
import me.banker.encahntedalies.data.FriendRequest
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor.*
import org.bson.Document
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitRunnable

class FriendsManager( val plugin: JavaPlugin) {
    lateinit var database: MongoDatabase
    val pendingFriendRequests = mutableListOf<FriendRequest>()

    init {
        setupDatabase()
    }

    fun setupDatabase() {
        val connectionString = plugin.config.getString("database.connection-string")
        val databaseName = plugin.config.getString("database.name")

        if (connectionString.isNullOrEmpty() || databaseName.isNullOrEmpty()) {
            plugin.logger.warning("Database connection string or name is not set in config.yml")
            return
        }

        val client = MongoClients.create(connectionString)
        database = client.getDatabase(databaseName)
    }


    // Utility functions
    fun updatePlayerStatus(playerName: String, isOnline: Boolean) {
        object : BukkitRunnable() {
            override fun run() {
                val playersCollection = database.getCollection("players")
                playersCollection.updateOne(
                    Filters.eq("username", playerName),
                    Updates.set("isOnline", isOnline)
                )
            }
        }.runTaskAsynchronously(plugin)
    }

    fun notifyFriendsPlayerIsOnline(player: Player) {
        object : BukkitRunnable() {
            override fun run() {
                val playersCollection = database.getCollection("players")
                val playerDocument = playersCollection.find(Filters.eq("username", player.name)).firstOrNull() ?: return
                val friendsList = playerDocument.getList("friends", String::class.java) ?: return

                friendsList.forEach { friendName ->
                    val friend = plugin.server.getPlayer(friendName)
                    val message = Component.text()
                        .append(Component.text("[", GRAY))
                        .append(Component.text("+", GREEN))
                        .append(Component.text("] ", GRAY))
                        .append(Component.text("${player.name} is now online", WHITE))
                        .build()
                    friend?.sendMessage(message)
                }
            }
        }.runTaskAsynchronously(plugin)
    }

    fun notifyFriendsPlayerIsOffline(player: Player) {
        object : BukkitRunnable() {
            override fun run() {
                val playersCollection = database.getCollection("players")
                val playerDocument = playersCollection.find(Filters.eq("username", player.name)).firstOrNull() ?: return
                val friendsList = playerDocument.getList("friends", String::class.java) ?: return

                friendsList.forEach { friendName ->
                    val friend = plugin.server.getPlayer(friendName)
                    val message = Component.text()
                        .append(Component.text("[", GRAY))
                        .append(Component.text("-", RED))
                        .append(Component.text("] ", GRAY))
                        .append(Component.text("${player.name} is now offline", WHITE))
                        .build()
                    friend?.sendMessage(message)
                }
            }
        }.runTaskAsynchronously(plugin)
    }

    fun addPlayerToDatabaseIfNotExists(playerName: String) {
        object : BukkitRunnable() {
            override fun run() {
                val playersCollection = database.getCollection("players")
                val playerDocument = playersCollection.find(Filters.eq("username", playerName)).firstOrNull()

                if (playerDocument == null) {
                    val newPlayerDocument = Document("username", playerName)
                        .append("friends", listOf<String>())
                    plugin.logger.info("Adding new player $playerName to the database.")
                    playersCollection.insertOne(newPlayerDocument)
                    plugin.logger.info("Added new player $playerName to the database.")
                }
            }
        }.runTaskAsynchronously(plugin)
    }

    // Friend-related functions
    fun addFriendRequest(request: FriendRequest) {
        pendingFriendRequests.add(request)
    }

    fun acceptFriendRequest(playerName: String, friendName: String) {
        object : BukkitRunnable() {
            override fun run() {
                val playersCollection = database.getCollection("players")

                playersCollection.updateOne(
                    Filters.eq("username", playerName),
                    Updates.addToSet("friends", friendName)
                )
                playersCollection.updateOne(
                    Filters.eq("username", friendName),
                    Updates.addToSet("friends", playerName)
                )

                pendingFriendRequests.removeIf { it.sender == friendName && it.receiver == playerName }
            }
        }.runTaskAsynchronously(plugin)
    }

    fun isRequestPending(receiver: String, sender: String): Boolean {
        return pendingFriendRequests.any { it.receiver == receiver && it.sender == sender }
    }

    fun removeFriend(name: String, name1: String) {
        object : BukkitRunnable() {
            override fun run() {
                val playersCollection = database.getCollection("players")

                playersCollection.updateOne(
                    Filters.eq("username", name),
                    Updates.pull("friends", name1)
                )
                playersCollection.updateOne(
                    Filters.eq("username", name1),
                    Updates.pull("friends", name)
                )
            }
        }.runTaskAsynchronously(plugin)
    }

    // Callback functions
    fun getFriends(playerName: String, callback: (List<String>) -> Unit) {
        object : BukkitRunnable() {
            override fun run() {
                val playersCollection = database.getCollection("players")
                val playerDocument = playersCollection.find(Filters.eq("username", playerName)).first()

                if (playerDocument != null) {
                    val friendsList = playerDocument.getList("friends", String::class.java) ?: listOf()
                    callback(friendsList)
                } else {
                    callback(listOf())
                }
            }
        }.runTaskAsynchronously(plugin)
    }

    fun isAlreadyFriends(name: String, targetPlayerName: String, callback: (Boolean) -> Unit) {
        object : BukkitRunnable() {
            override fun run() {
                val playersCollection = database.getCollection("players")
                val playerDocument = playersCollection.find(Filters.eq("username", name)).firstOrNull()

                var areFriends = false
                if (playerDocument != null) {
                    val friendsList = playerDocument.getList("friends", String::class.java) ?: listOf()
                    areFriends = targetPlayerName in friendsList
                }

                Bukkit.getScheduler().runTask(plugin, Runnable {
                    callback(areFriends)
                })
            }
        }.runTaskAsynchronously(plugin)
    }

    fun areFriends(name: String, targetPlayerName: String): Boolean {
        val playersCollection = database.getCollection("players")
        val playerDocument = playersCollection.find(Filters.eq("username", name)).firstOrNull()

        var areFriends = false
        if (playerDocument != null) {
            val friendsList = playerDocument.getList("friends", String::class.java) ?: listOf()
            areFriends = targetPlayerName in friendsList
        }

        return areFriends
    }

    fun denyFriendRequest(name: String, targetPlayerName: String) {
        pendingFriendRequests.removeIf { it.sender == targetPlayerName && it.receiver == name }
    }
}

