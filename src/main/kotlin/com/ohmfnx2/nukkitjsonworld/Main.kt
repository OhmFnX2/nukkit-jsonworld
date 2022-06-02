package com.ohmfnx2.nukkitjsonworld

import cn.nukkit.Player
import cn.nukkit.event.Listener
import cn.nukkit.level.Location
import cn.nukkit.plugin.PluginBase
import java.io.File

class Main : PluginBase(), Listener {
    var jsonWorldDir: File = File(dataFolder, "plugins/Nukkit_Json_World/json_world")

    override fun onEnable() {
        logger.info("$name enabled")
        if (!dataFolder.exists()) {
            dataFolder.mkdir()
        }
        this.server.pluginManager.registerEvents(this, this)
        this.server.commandMap.register("jsonworld",  JsonWorldCommand(this));
    }

    override fun onDisable() {
        logger.info("$name disabled")
    }
}
