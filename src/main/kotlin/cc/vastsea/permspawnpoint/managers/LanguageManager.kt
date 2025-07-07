package cc.vastsea.permspawnpoint.managers

import cc.vastsea.permspawnpoint.PermSpawnpoint
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File
import java.io.InputStreamReader

class LanguageManager(private val plugin: PermSpawnpoint) {
    
    private val messages = mutableMapOf<String, String>()
    private var currentLanguage = "en_US"
    
    fun loadLanguages() {
        currentLanguage = plugin.configManager.getLanguage()
        
        // Create language directory if it doesn't exist
        val langDir = File(plugin.dataFolder, "lang")
        if (!langDir.exists()) {
            langDir.mkdirs()
        }
        
        // Save default language files
        saveDefaultLanguageFiles()
        
        // Load the selected language
        loadLanguage(currentLanguage)
    }
    
    private fun saveDefaultLanguageFiles() {
        val languages = listOf("en_US", "zh_CN")
        
        for (lang in languages) {
            val langFile = File(plugin.dataFolder, "lang/$lang.yml")
            if (!langFile.exists()) {
                plugin.saveResource("lang/$lang.yml", false)
            }
        }
    }
    
    private fun loadLanguage(language: String) {
        messages.clear()
        
        val langFile = File(plugin.dataFolder, "lang/$language.yml")
        
        if (!langFile.exists()) {
            plugin.logger.warning("Language file $language.yml not found, falling back to en_US")
            loadLanguage("en_US")
            return
        }
        
        try {
            val config = YamlConfiguration.loadConfiguration(langFile)
            
            // Load all messages recursively
            loadMessagesFromSection(config, "")
            
            plugin.logger.info("Loaded language: $language")
        } catch (e: Exception) {
            plugin.logger.severe("Failed to load language file $language.yml: ${e.message}")
        }
    }
    
    private fun loadMessagesFromSection(config: FileConfiguration, prefix: String) {
        for (key in config.getKeys(true)) {
            val value = config.get(key)
            if (value is String) {
                val fullKey = if (prefix.isEmpty()) key else "$prefix.$key"
                messages[fullKey] = value
            }
        }
    }
    
    fun getMessage(key: String, vararg args: Any): String {
        val message = messages[key] ?: run {
            plugin.logger.warning("Missing translation key: $key")
            return key
        }
        
        return if (args.isNotEmpty()) {
            String.format(message, *args)
        } else {
            message
        }
    }
    
    fun getMessageWithFallback(key: String, fallback: String, vararg args: Any): String {
        val message = messages[key] ?: fallback
        
        return if (args.isNotEmpty()) {
            String.format(message, *args)
        } else {
            message
        }
    }
    
    fun hasMessage(key: String): Boolean {
        return messages.containsKey(key)
    }
    
    fun getCurrentLanguage(): String {
        return currentLanguage
    }
}