package cc.vastsea.permspawnpoint.config

import cc.vastsea.permspawnpoint.PermSpawnpoint
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File

class ConfigManager(private val plugin: PermSpawnpoint) {
    
    private lateinit var config: FileConfiguration
    
    fun loadConfig() {
        val configFile = File(plugin.dataFolder, "config.yml")
        
        if (!configFile.exists()) {
            plugin.saveDefaultConfig()
        }
        
        config = YamlConfiguration.loadConfiguration(configFile)
        
        // Validate configuration
        validateConfig()
    }
    
    private fun validateConfig() {
        val spawnPoints = config.getConfigurationSection("spawn-points")
        if (spawnPoints == null) {
            plugin.logger.warning(plugin.languageManager.getMessageWithFallback(
                "config.no-spawn-points", 
                "No spawn points configured!"
            ))
        }
    }
    
    fun getLanguage(): String {
        return config.getString("language", "en_US") ?: "en_US"
    }
    
    fun isDebugEnabled(): Boolean {
        return config.getBoolean("debug", false)
    }
    
    fun getSpawnPoints(): Map<String, SpawnPointConfig> {
        val spawnPoints = mutableMapOf<String, SpawnPointConfig>()
        val section = config.getConfigurationSection("spawn-points") ?: return spawnPoints
        
        for (key in section.getKeys(false)) {
            val spawnSection = section.getConfigurationSection(key) ?: continue
            
            val world = spawnSection.getString("world")
            val x = spawnSection.getDouble("x")
            val y = spawnSection.getDouble("y")
            val z = spawnSection.getDouble("z")
            val yaw = spawnSection.getDouble("yaw", 0.0).toFloat()
            val pitch = spawnSection.getDouble("pitch", 0.0).toFloat()
            val permissions = spawnSection.getStringList("permissions")
            val priority = spawnSection.getInt("priority", 0)
            
            if (world != null) {
                spawnPoints[key] = SpawnPointConfig(
                    name = key,
                    world = world,
                    x = x,
                    y = y,
                    z = z,
                    yaw = yaw,
                    pitch = pitch,
                    permissions = permissions,
                    priority = priority
                )
            }
        }
        
        return spawnPoints
    }
    
    fun saveSpawnPoint(name: String, config: SpawnPointConfig) {
        val path = "spawn-points.$name"
        this.config.set("$path.world", config.world)
        this.config.set("$path.x", config.x)
        this.config.set("$path.y", config.y)
        this.config.set("$path.z", config.z)
        this.config.set("$path.yaw", config.yaw.toDouble())
        this.config.set("$path.pitch", config.pitch.toDouble())
        this.config.set("$path.permissions", config.permissions)
        this.config.set("$path.priority", config.priority)
        
        saveConfig()
    }
    
    private fun saveConfig() {
        try {
            config.save(File(plugin.dataFolder, "config.yml"))
        } catch (e: Exception) {
            plugin.logger.severe("Could not save config.yml: ${e.message}")
        }
    }
}

data class SpawnPointConfig(
    val name: String,
    val world: String,
    val x: Double,
    val y: Double,
    val z: Double,
    val yaw: Float,
    val pitch: Float,
    val permissions: List<String>,
    val priority: Int
)