package cc.vastsea.permspawnpoint.listeners

import cc.vastsea.permspawnpoint.PermSpawnpoint
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.scheduler.BukkitRunnable

class PlayerDeathListener(private val plugin: PermSpawnpoint) : Listener {
    
    @EventHandler(priority = EventPriority.HIGHEST)
    fun onPlayerDeath(event: PlayerDeathEvent) {
        val player = event.entity
        
        // Check if player has a bed spawn location set
        val bedSpawn = player.bedSpawnLocation
        val expectedSpawn = plugin.spawnManager.getSpawnLocation(player)
        
        if (expectedSpawn != null) {
            // If bed spawn is null or doesn't match expected spawn, reset it
            if (bedSpawn == null || 
                bedSpawn.world?.name != expectedSpawn.world?.name ||
                bedSpawn.blockX != expectedSpawn.blockX ||
                bedSpawn.blockY != expectedSpawn.blockY ||
                bedSpawn.blockZ != expectedSpawn.blockZ) {
                
                // Delay the respawn location setting to ensure it takes effect
                object : BukkitRunnable() {
                    override fun run() {
                        if (player.isOnline) {
                            player.setBedSpawnLocation(expectedSpawn, true)
                            
                            if (plugin.configManager.isDebugEnabled()) {
                                plugin.logger.info(plugin.languageManager.getMessage(
                                    "death.respawn-reset",
                                    player.name,
                                    expectedSpawn.world?.name ?: "unknown",
                                    expectedSpawn.x.toInt(),
                                    expectedSpawn.y.toInt(),
                                    expectedSpawn.z.toInt()
                                ))
                            }
                        }
                    }
                }.runTaskLater(plugin, 1L) // Wait 1 tick
            }
        }
    }
}