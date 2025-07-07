package cc.vastsea.permspawnpoint.managers

import cc.vastsea.permspawnpoint.PermSpawnpoint
import cc.vastsea.permspawnpoint.config.SpawnPointConfig
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Player
import java.io.File
import java.util.*

class SpawnManager(private val plugin: PermSpawnpoint) {
    
    private val firstJoinFile = File(plugin.dataFolder, "first-join.txt")
    private val firstJoinPlayers = mutableSetOf<UUID>()
    
    init {
        loadFirstJoinData()
    }
    
    private fun loadFirstJoinData() {
        if (firstJoinFile.exists()) {
            try {
                firstJoinFile.readLines().forEach { line ->
                    if (line.isNotBlank()) {
                        try {
                            firstJoinPlayers.add(UUID.fromString(line.trim()))
                        } catch (e: IllegalArgumentException) {
                            plugin.logger.warning("Invalid UUID in first-join.txt: $line")
                        }
                    }
                }
            } catch (e: Exception) {
                plugin.logger.severe("Failed to load first-join data: ${e.message}")
            }
        }
    }
    
    private fun saveFirstJoinData() {
        try {
            if (!plugin.dataFolder.exists()) {
                plugin.dataFolder.mkdirs()
            }
            
            firstJoinFile.writeText(firstJoinPlayers.joinToString("\n") { it.toString() })
        } catch (e: Exception) {
            plugin.logger.severe("Failed to save first-join data: ${e.message}")
        }
    }
    
    fun isFirstJoin(player: Player): Boolean {
        return !firstJoinPlayers.contains(player.uniqueId)
    }
    
    fun markAsJoined(player: Player) {
        firstJoinPlayers.add(player.uniqueId)
        saveFirstJoinData()
    }
    
    fun getSpawnLocation(player: Player): Location? {
        if (!isFirstJoin(player)) {
            return null // Not first join, don't teleport
        }
        
        val spawnPoints = plugin.configManager.getSpawnPoints()
        if (spawnPoints.isEmpty()) {
            plugin.logger.warning(plugin.languageManager.getMessage("spawn.no-spawn-points"))
            return null
        }
        
        // Find the best spawn point for this player
        val validSpawnPoints = spawnPoints.values.filter { spawnPoint ->
            hasRequiredPermissions(player, spawnPoint)
        }.sortedByDescending { it.priority }
        
        if (validSpawnPoints.isEmpty()) {
            if (plugin.configManager.isDebugEnabled()) {
                plugin.logger.info(plugin.languageManager.getMessage(
                    "spawn.no-valid-spawn", 
                    player.name
                ))
            }
            return null
        }
        
        val selectedSpawn = validSpawnPoints.first()
        return createLocation(selectedSpawn)
    }
    
    private fun hasRequiredPermissions(player: Player, spawnPoint: SpawnPointConfig): Boolean {
        if (spawnPoint.permissions.isEmpty()) {
            return true // No permissions required
        }
        
        // Check if player has bypass permission
        if (player.hasPermission("permspawnpoint.bypass")) {
            return true
        }
        
        // Check if player has any of the required permissions
        return spawnPoint.permissions.any { permission ->
            when {
                permission.startsWith("group:") -> {
                    val groupName = permission.substring(6)
                    PermSpawnpoint.permission?.playerInGroup(player, groupName) ?: false
                }
                else -> player.hasPermission(permission)
            }
        }
    }
    
    private fun createLocation(spawnPoint: SpawnPointConfig): Location? {
        val world = Bukkit.getWorld(spawnPoint.world)
        if (world == null) {
            plugin.logger.warning(plugin.languageManager.getMessage(
                "spawn.world-not-found", 
                spawnPoint.world
            ))
            return null
        }
        
        return Location(
            world,
            spawnPoint.x,
            spawnPoint.y,
            spawnPoint.z,
            spawnPoint.yaw,
            spawnPoint.pitch
        )
    }
    
    fun teleportToSpawn(player: Player): Boolean {
        val spawnLocation = getSpawnLocation(player)
        if (spawnLocation == null) {
            return false
        }
        
        try {
            player.teleport(spawnLocation)
            markAsJoined(player)
            
            if (plugin.configManager.isDebugEnabled()) {
                plugin.logger.info(plugin.languageManager.getMessage(
                    "spawn.teleported", 
                    player.name,
                    spawnLocation.world?.name ?: "unknown",
                    spawnLocation.x.toInt(),
                    spawnLocation.y.toInt(),
                    spawnLocation.z.toInt()
                ))
            }
            
            return true
        } catch (e: Exception) {
            plugin.logger.severe(plugin.languageManager.getMessage(
                "spawn.teleport-failed", 
                player.name,
                e.message ?: "Unknown error"
            ))
            return false
        }
    }
    
    fun resetFirstJoin(player: Player) {
        firstJoinPlayers.remove(player.uniqueId)
        saveFirstJoinData()
    }
    
    fun getSpawnPointsList(): List<SpawnPointConfig> {
        return plugin.configManager.getSpawnPoints().values.sortedByDescending { it.priority }
    }
}