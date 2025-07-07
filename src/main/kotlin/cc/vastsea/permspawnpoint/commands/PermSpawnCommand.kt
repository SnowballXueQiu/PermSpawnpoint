package cc.vastsea.permspawnpoint.commands

import cc.vastsea.permspawnpoint.PermSpawnpoint
import cc.vastsea.permspawnpoint.config.SpawnPointConfig
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player

class PermSpawnCommand(private val plugin: PermSpawnpoint) : CommandExecutor, TabCompleter {
    
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (!sender.hasPermission("permspawnpoint.admin")) {
            sender.sendMessage(plugin.languageManager.getMessage("command.no-permission"))
            return true
        }
        
        if (args.isEmpty()) {
            sendHelp(sender)
            return true
        }
        
        when (args[0].lowercase()) {
            "reload" -> handleReload(sender)
            "setspawn" -> handleSetSpawn(sender, args)
            "list" -> handleList(sender)
            "reset" -> handleReset(sender, args)
            "info" -> handleInfo(sender, args)
            else -> sendHelp(sender)
        }
        
        return true
    }
    
    private fun sendHelp(sender: CommandSender) {
        val messages = listOf(
            "command.help.header",
            "command.help.reload",
            "command.help.setspawn",
            "command.help.list",
            "command.help.reset",
            "command.help.info"
        )
        
        messages.forEach { key ->
            sender.sendMessage(plugin.languageManager.getMessage(key))
        }
    }
    
    private fun handleReload(sender: CommandSender) {
        try {
            plugin.reloadPlugin()
            sender.sendMessage(plugin.languageManager.getMessage("command.reload.success"))
        } catch (e: Exception) {
            sender.sendMessage(plugin.languageManager.getMessage("command.reload.failed", e.message ?: "Unknown error"))
        }
    }
    
    private fun handleSetSpawn(sender: CommandSender, args: Array<out String>) {
        if (sender !is Player) {
            sender.sendMessage(plugin.languageManager.getMessage("command.player-only"))
            return
        }
        
        if (args.size < 4) {
            sender.sendMessage(plugin.languageManager.getMessage("command.setspawn.usage"))
            return
        }
        
        val name = args[1]
        val permissionsStr = args[2]
        val priorityStr = args[3]
        
        val priority = try {
            priorityStr.toInt()
        } catch (e: NumberFormatException) {
            sender.sendMessage(plugin.languageManager.getMessage("command.setspawn.invalid-priority"))
            return
        }
        
        val permissions = if (permissionsStr == "none") {
            emptyList()
        } else {
            permissionsStr.split(",").map { it.trim() }
        }
        
        val location = sender.location
        val spawnConfig = SpawnPointConfig(
            name = name,
            world = location.world?.name ?: "world",
            x = location.x,
            y = location.y,
            z = location.z,
            yaw = location.yaw,
            pitch = location.pitch,
            permissions = permissions,
            priority = priority
        )
        
        plugin.configManager.saveSpawnPoint(name, spawnConfig)
        
        sender.sendMessage(plugin.languageManager.getMessage(
            "command.setspawn.success",
            name,
            location.world?.name ?: "unknown",
            location.x.toInt(),
            location.y.toInt(),
            location.z.toInt(),
            priority
        ))
    }
    
    private fun handleList(sender: CommandSender) {
        val spawnPoints = plugin.spawnManager.getSpawnPointsList()
        
        if (spawnPoints.isEmpty()) {
            sender.sendMessage(plugin.languageManager.getMessage("command.list.empty"))
            return
        }
        
        sender.sendMessage(plugin.languageManager.getMessage("command.list.header"))
        
        spawnPoints.forEach { spawn ->
            val permissionsStr = if (spawn.permissions.isEmpty()) {
                plugin.languageManager.getMessage("command.list.no-permissions")
            } else {
                spawn.permissions.joinToString(", ")
            }
            
            sender.sendMessage(plugin.languageManager.getMessage(
                "command.list.entry",
                spawn.name,
                spawn.world,
                spawn.x.toInt(),
                spawn.y.toInt(),
                spawn.z.toInt(),
                spawn.priority,
                permissionsStr
            ))
        }
    }
    
    private fun handleReset(sender: CommandSender, args: Array<out String>) {
        if (args.size < 2) {
            sender.sendMessage(plugin.languageManager.getMessage("command.reset.usage"))
            return
        }
        
        val playerName = args[1]
        val targetPlayer = plugin.server.getPlayer(playerName)
        
        if (targetPlayer == null) {
            sender.sendMessage(plugin.languageManager.getMessage("command.reset.player-not-found", playerName))
            return
        }
        
        plugin.spawnManager.resetFirstJoin(targetPlayer)
        sender.sendMessage(plugin.languageManager.getMessage("command.reset.success", targetPlayer.name))
    }
    
    private fun handleInfo(sender: CommandSender, args: Array<out String>) {
        if (args.size < 2) {
            sender.sendMessage(plugin.languageManager.getMessage("command.info.usage"))
            return
        }
        
        val playerName = args[1]
        val targetPlayer = plugin.server.getPlayer(playerName)
        
        if (targetPlayer == null) {
            sender.sendMessage(plugin.languageManager.getMessage("command.info.player-not-found", playerName))
            return
        }
        
        val isFirstJoin = plugin.spawnManager.isFirstJoin(targetPlayer)
        val spawnLocation = plugin.spawnManager.getSpawnLocation(targetPlayer)
        
        sender.sendMessage(plugin.languageManager.getMessage("command.info.header", targetPlayer.name))
        sender.sendMessage(plugin.languageManager.getMessage(
            "command.info.first-join", 
            if (isFirstJoin) plugin.languageManager.getMessage("common.yes") 
            else plugin.languageManager.getMessage("common.no")
        ))
        
        if (spawnLocation != null) {
            sender.sendMessage(plugin.languageManager.getMessage(
                "command.info.spawn-location",
                spawnLocation.world?.name ?: "unknown",
                spawnLocation.x.toInt(),
                spawnLocation.y.toInt(),
                spawnLocation.z.toInt()
            ))
        } else {
            sender.sendMessage(plugin.languageManager.getMessage("command.info.no-spawn"))
        }
    }
    
    override fun onTabComplete(sender: CommandSender, command: Command, alias: String, args: Array<out String>): List<String> {
        if (!sender.hasPermission("permspawnpoint.admin")) {
            return emptyList()
        }
        
        return when (args.size) {
            1 -> listOf("reload", "setspawn", "list", "reset", "info").filter { 
                it.startsWith(args[0], ignoreCase = true) 
            }
            2 -> when (args[0].lowercase()) {
                "reset", "info" -> plugin.server.onlinePlayers.map { it.name }.filter {
                    it.startsWith(args[1], ignoreCase = true)
                }
                else -> emptyList()
            }
            else -> emptyList()
        }
    }
}