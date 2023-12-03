package me.banker.enchantedallies

import me.banker.encahntedalies.commands.FriendCommandExecutor
import me.banker.encahntedalies.listeners.FriendEvents
import me.banker.encahntedalies.utils.FriendsManager
import org.bukkit.plugin.java.JavaPlugin
import revxrsal.commands.bukkit.BukkitCommandHandler

class EnchantedAllies : JavaPlugin() {
    private lateinit var friendsManager: FriendsManager
    private lateinit var commandHandler: BukkitCommandHandler

    override fun onEnable() {
        loadConfiguration()
        initializeManagers()
        registerCommands()
        registerEvents()

        logger.info("${description.name} has been enabled.")
    }

    override fun onDisable() {
        logger.info("${description.name} has been disabled.")
    }

    private fun loadConfiguration() {
        saveDefaultConfig()
    }

    private fun initializeManagers() {
        friendsManager = FriendsManager(this)
        commandHandler = BukkitCommandHandler.create(this)
    }

    private fun registerCommands() {
        // Assuming FriendCommandExecutor is correctly set up for Lamp
        commandHandler.register(FriendCommandExecutor(friendsManager, commandHandler))
    }

    private fun registerEvents() {
        server.pluginManager.registerEvents(FriendEvents(friendsManager), this)
    }
}
