package cc.vastsea.permspawnpoint.listeners

import cc.vastsea.permspawnpoint.PermSpawnpoint
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.scheduler.BukkitRunnable

class PlayerJoinListener(private val plugin: PermSpawnpoint) : Listener {
    
    @EventHandler(priority = EventPriority.HIGHEST)
    fun onPlayerJoin(event: PlayerJoinEvent) {
        val player = event.player
        
        // Check if this is the player's first join
        if (!plugin.spawnManager.isFirstJoin(player)) {
            if (plugin.configManager.isDebugEnabled()) {
                plugin.logger.info(plugin.languageManager.getMessage(
                    "join.not-first-time", 
                    player.name
                ))
            }
            return
        }
        
        if (plugin.configManager.isDebugEnabled()) {
            plugin.logger.info(plugin.languageManager.getMessage(
                "join.first-time", 
                player.name
            ))
        }
        
        // Delay the teleportation to ensure the player is fully loaded
        object : BukkitRunnable() {
            override fun run() {
                if (player.isOnline) {
                    val success = plugin.spawnManager.teleportToSpawn(player)
                    
                    if (success) {
                        // Set player's respawn location to prevent returning to world spawn on death
                        val spawnLocation = plugin.spawnManager.getSpawnLocation(player)
                        if (spawnLocation != null) {
                            // Set bed spawn location (works on all Bukkit versions)
                            player.setBedSpawnLocation(spawnLocation, true)
                            
                            if (plugin.configManager.isDebugEnabled()) {
                                plugin.logger.info(plugin.languageManager.getMessage(
                                    "join.respawn-set",
                                    player.name,
                                    spawnLocation.world?.name ?: "unknown",
                                    spawnLocation.x.toInt(),
                                    spawnLocation.y.toInt(),
                                    spawnLocation.z.toInt()
                                ))
                            }
                        }
                        
                        // Send welcome message to player
                        val welcomeMessage = plugin.languageManager.getMessage("join.welcome")
                        if (welcomeMessage.isNotBlank()) {
                            player.sendMessage(welcomeMessage)
                        }
                    } else {
                        // Log warning if teleportation failed
                        plugin.logger.warning(plugin.languageManager.getMessage(
                            "join.spawn-failed", 
                            player.name
                        ))
                        
                        // Still mark as joined to prevent repeated attempts
                        plugin.spawnManager.markAsJoined(player)
                    }
                }
            }
        }.runTaskLater(plugin, 20L) // Wait 1 second (20 ticks)
    }
}