package cc.vastsea.permspawnpoint.listeners

import cc.vastsea.permspawnpoint.PermSpawnpoint
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.spigotmc.event.player.PlayerSpawnLocationEvent
import org.bukkit.scheduler.BukkitRunnable

class PlayerSpawnLocationListener(private val plugin: PermSpawnpoint) : Listener {
    
    @EventHandler(priority = EventPriority.HIGHEST)
    fun onPlayerSpawnLocation(event: PlayerSpawnLocationEvent) {
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
        
        // Get the spawn location for this player
        val spawnLocation = plugin.spawnManager.getSpawnLocation(player)
        if (spawnLocation != null) {
            // Set the spawn location directly in the event
            event.spawnLocation = spawnLocation
            
            if (plugin.configManager.isDebugEnabled()) {
                plugin.logger.info(plugin.languageManager.getMessage(
                    "join.spawn-location-set",
                    player.name,
                    spawnLocation.world?.name ?: "unknown",
                    spawnLocation.x.toInt(),
                    spawnLocation.y.toInt(),
                    spawnLocation.z.toInt()
                ))
            }
            
            // Set the bed spawn location after a delay to ensure the player is fully loaded
            object : BukkitRunnable() {
                override fun run() {
                    if (player.isOnline) {
                        setRespawnLocationWithRetry(player, spawnLocation, 0)
                        
                        // Send welcome message to player
                        val welcomeMessage = plugin.languageManager.getMessage("join.welcome")
                        if (welcomeMessage.isNotBlank()) {
                            player.sendMessage(welcomeMessage)
                        }
                        
                        // Mark as joined
                        plugin.spawnManager.markAsJoined(player)
                    }
                }
            }.runTaskLater(plugin, 40L) // Wait 2 seconds
        } else {
            plugin.logger.warning(plugin.languageManager.getMessage(
                "join.no-spawn-location", 
                player.name
            ))
        }
    }
    
    private fun setRespawnLocationWithRetry(player: Player, spawnLocation: org.bukkit.Location, attempts: Int) {
        object : BukkitRunnable() {
            override fun run() {
                if (player.isOnline) {
                    try {
                        player.setBedSpawnLocation(spawnLocation, true)
                        
                        // Verify the respawn location was set correctly
                        val bedSpawn = player.bedSpawnLocation
                        if (bedSpawn != null &&
                            bedSpawn.world?.name == spawnLocation.world?.name &&
                            bedSpawn.blockX == spawnLocation.blockX &&
                            bedSpawn.blockY == spawnLocation.blockY &&
                            bedSpawn.blockZ == spawnLocation.blockZ) {
                            
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
                            return
                        }
                    } catch (e: Exception) {
                        if (plugin.configManager.isDebugEnabled()) {
                            plugin.logger.warning("Failed to set respawn location (attempt ${attempts + 1}): ${e.message}")
                        }
                    }
                    
                    // If we reach here, the setting failed, retry if attempts < 5
                    if (attempts < 4) {
                        setRespawnLocationWithRetry(player, spawnLocation, attempts + 1)
                    } else {
                        if (plugin.configManager.isDebugEnabled()) {
                            plugin.logger.warning("Failed to set respawn location for ${player.name} after ${attempts + 1} attempts")
                        }
                    }
                }
            }
        }.runTaskLater(plugin, 20L) // Wait 1 second between retries
    }
}