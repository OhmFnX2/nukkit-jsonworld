package com.ohmfnx2.nukkitjsonworld

import cn.nukkit.Player
import cn.nukkit.block.Block
import cn.nukkit.block.BlockID
import cn.nukkit.blockstate.BlockState
import cn.nukkit.command.Command
import cn.nukkit.command.CommandSender
import cn.nukkit.command.data.CommandParamType
import cn.nukkit.command.data.CommandParameter
import cn.nukkit.event.Listener
import cn.nukkit.level.Level
import cn.nukkit.level.Location
import cn.nukkit.plugin.PluginBase
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import java.io.File

class Main: PluginBase(), Listener {
    private var jsonWorldDir: File = File(dataFolder,"json_world")

    private var pos1: HashMap<Player, Location> = HashMap()

    private var pos2: HashMap<Player, Location> = HashMap()

    override fun onEnable() {
        logger.info("$name enabled")
        if (!dataFolder.exists()) {
            dataFolder.mkdir()
        }
        this.server.pluginManager.registerEvents(this, this)
    }

    override fun onDisable() {
        logger.info("$name disabled")
    }

    override fun onCommand(
        sender: CommandSender?,
        command: Command?,
        label: String?,
        args: Array<out String>?
    ): Boolean {
        command?.addCommandParameters("jsonworld", arrayOf(CommandParameter.newEnum("mode", arrayOf("pos1","pos2","save","load","list")) ,CommandParameter.newType("filename", CommandParamType.STRING), CommandParameter.newType("air_save", CommandParamType.STRING)))
        if (command?.name == "jsonworld") {
            if (!jsonWorldDir.exists()) {
                jsonWorldDir.mkdirs()
            }
            if (sender is Player) {
                if (args?.get(0) == "pos1") {
                    pos1[sender] = sender.location
                    sender.sendMessage("pos1 set")
                }
                if (args?.get(0) == "pos2") {
                    pos2[sender] = sender.location
                    sender.sendMessage("pos2 set")
                }
                if (args?.get(0) == "save") {
                    if (pos1[sender] == null) return false
                    if (pos2[sender] == null) return false
                    if (args[1].none()) return false
                    var save_air = false
                    if (args[2].none()) save_air = false else if (args[2] == "true") save_air = true
                    var xMin: Int
                    var xMax: Int
                    var yMin: Int
                    var yMax: Int
                    var zMin: Int
                    var zMax: Int
                    if (pos1[sender]!!.x < pos2[sender]!!.x) {
                        xMin = pos1[sender]?.floorX ?: 0
                        xMax = pos2[sender]?.floorX ?: 0
                    } else {
                        xMin = pos2[sender]?.floorX ?: 0
                        xMax = pos1[sender]?.floorX ?: 0
                    }
                    if (pos1[sender]!!.y < pos2[sender]!!.y) {
                        yMin = pos1[sender]?.floorY ?: 0
                        yMax = pos2[sender]?.floorY ?: 0
                    } else {
                        yMin = pos2[sender]?.floorY ?: 0
                        yMax = pos1[sender]?.floorY ?: 0
                    }
                    if (pos1[sender]!!.z < pos2[sender]!!.z) {
                        zMin = pos1[sender]?.floorZ ?: 0
                        zMax = pos2[sender]?.floorZ ?: 0
                    } else {
                        zMin = pos2[sender]?.floorZ ?: 0
                        zMax = pos1[sender]?.floorZ ?: 0
                    }
                    val json_world = JsonArray()

                    for(x in xMin..xMax) {
                        for(y in yMin..yMax) {
                            for(z in zMin..zMax) {
                                val location = Location(x.toDouble(), y.toDouble(), z.toDouble(), sender.level)
                                val block = sender.level.getBlock(location)
                                val jsonObject = JsonObject()
                                jsonObject.addProperty("block_id", BlockState.of(block.id).stateId.replaceAfter(";","").replace(";",""))
                                jsonObject.addProperty("block_data", block.dataStorage)
                                jsonObject.add("position", JsonObject().apply {
                                    addProperty("x", location.floorX)
                                    addProperty("y", location.floorY)
                                    addProperty("z", location.floorZ)
                                })
                                sender.sendMessage("${location.floorX} ${location.floorY} ${location.floorZ} ${BlockState.of(block.id).stateId.replaceAfter(";","").replace(";","")} ${block.dataStorage}")
                                json_world.add(jsonObject)
                            }
                        }
                    }
                    val blockDataFile = File(jsonWorldDir, "${args[1]}.json")
                    val json_world2: JsonObject = JsonObject()
                    json_world2.add("json_world", json_world)
                    blockDataFile.writeText(Gson().toJson(json_world2))
                    sender.sendMessage("saved!")
                }
                if (args?.get(0) == "load") {
                    if (args[1].none()) return false
                    val blockDataFile = File(jsonWorldDir, "${args[1]}.json")
                    if (!blockDataFile.exists()) {
                        sender.sendMessage("file not found")
                        return false
                    }
                    val json_world: JsonObject = Gson().fromJson(blockDataFile.readText(), JsonObject::class.java)
                    for (jsonObject in json_world.get("json_world").asJsonArray) {
                        val block_id = jsonObject.asJsonObject.get("block_id").asString
                        val block_data = jsonObject.asJsonObject.get("block_data").asInt
                        val position = jsonObject.asJsonObject.get("position").asJsonObject
                        val x = position.get("x").asInt
                        val y = position.get("y").asInt
                        val z = position.get("z").asInt
                        if (y >= 500) continue
                        val location = Location(x.toDouble(), y.toDouble(), z.toDouble(), sender.level)
                        sender.level.setBlock(location, Block.get(BlockState.of(block_id).blockId, block_data))
                        sender.sendMessage("${location.floorX} ${location.floorY} ${location.floorZ} ${block_id} ${block_data}")
                    }
                    sender.sendMessage("loaded!")
                }
                if (args?.get(0) == "list") {
                    val blockDataFileList = jsonWorldDir.listFiles()
                    if (blockDataFileList.none()) {
                        sender.sendMessage("no file")
                        return false
                    }
                    for (blockDataFile in blockDataFileList) {
                        sender.sendMessage("List: " + blockDataFile.name.replace(".json", ""))
                    }
                }
            }
        }
        return false
    }
}