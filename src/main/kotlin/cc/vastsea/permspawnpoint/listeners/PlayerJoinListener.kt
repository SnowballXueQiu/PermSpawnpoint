package cc.vastsea.permspawnpoint.listeners

import cc.vastsea.permspawnpoint.PermSpawnpoint
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.scheduler.BukkitRunnable

class PlayerJoinListener(private val plugin: PermSpawnpoint) : Listener {
    
    private fun setRespawnLocationWithRetry(player: Player, attempts: Int, delay: Long) {
        object : BukkitRunnable() {
            override fun run() {
                if (player.isOnline) {
                    val spawnLocation = plugin.spawnManager.getSpawnLocation(player)
                    if (spawnLocation != null) {
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
                            setRespawnLocationWithRetry(player, attempts + 1, 20L) // Retry after 1 second
                        } else {
                            if (plugin.configManager.isDebugEnabled()) {
                                plugin.logger.warning("Failed to set respawn location for ${player.name} after ${attempts + 1} attempts")
                            }
                        }
                    }
                }
            }
        }.runTaskLater(plugin, delay)
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    fun onPlayerJoin(event: PlayerJoinEvent) {
        val player = event.player
        
        // This listener now only handles logging and cleanup
        // The actual spawn location is handled by PlayerSpawnLocationListener
        
        if (plugin.configManager.isDebugEnabled()) {
            if (plugin.spawnManager.isFirstJoin(player)) {
                plugin.logger.info(plugin.languageManager.getMessage(
                    "join.first-time", 
                    player.name
                ))
            } else {
                plugin.logger.info(plugin.languageManager.getMessage(
                    "join.not-first-time", 
                    player.name
                ))
            }
        }
    }
}