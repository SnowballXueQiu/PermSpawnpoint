package cc.vastsea.permspawnpoint

import cc.vastsea.permspawnpoint.commands.PermSpawnCommand
import cc.vastsea.permspawnpoint.config.ConfigManager
import cc.vastsea.permspawnpoint.listeners.PlayerJoinListener
import cc.vastsea.permspawnpoint.listeners.PlayerSpawnLocationListener
import cc.vastsea.permspawnpoint.listeners.PlayerDeathListener
import cc.vastsea.permspawnpoint.managers.LanguageManager
import cc.vastsea.permspawnpoint.managers.SpawnManager
import net.milkbowl.vault.permission.Permission
import org.bukkit.plugin.RegisteredServiceProvider
import org.bukkit.plugin.java.JavaPlugin

class PermSpawnpoint : JavaPlugin() {
    
    companion object {
        lateinit var instance: PermSpawnpoint
            private set
        
        var permission: Permission? = null
            private set
    }
    
    lateinit var configManager: ConfigManager
        private set
    
    lateinit var languageManager: LanguageManager
        private set
        
    lateinit var spawnManager: SpawnManager
        private set
    
    override fun onEnable() {
        instance = this
        
        // Setup Vault permissions
        if (!setupPermissions()) {
            languageManager.getMessageWithFallback("plugin.vault-not-found", "Vault not found! Disabling plugin.")
                .let { logger.severe(it) }
            server.pluginManager.disablePlugin(this)
            return
        }
        
        // Initialize managers
        configManager = ConfigManager(this)
        languageManager = LanguageManager(this)
        spawnManager = SpawnManager(this)
        
        // Load configurations
        configManager.loadConfig()
        languageManager.loadLanguages()
        
        // Register events
        server.pluginManager.registerEvents(PlayerJoinListener(this), this)
        server.pluginManager.registerEvents(PlayerSpawnLocationListener(this), this)
        server.pluginManager.registerEvents(PlayerDeathListener(this), this)
        
        // Register commands
        getCommand("permspawn")?.setExecutor(PermSpawnCommand(this))
        
        logger.info(languageManager.getMessage("plugin.enabled"))
    }
    
    override fun onDisable() {
        logger.info(languageManager.getMessage("plugin.disabled"))
    }
    
    private fun setupPermissions(): Boolean {
        val rsp: RegisteredServiceProvider<Permission>? = server.servicesManager.getRegistration(Permission::class.java)
        permission = rsp?.provider
        return permission != null
    }
    
    fun reloadPlugin() {
        configManager.loadConfig()
        languageManager.loadLanguages()
        logger.info(languageManager.getMessage("plugin.reloaded"))
    }
}