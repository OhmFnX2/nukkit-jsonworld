package com.ohmfnx2.nukkitjsonworld

import cn.nukkit.Player
import cn.nukkit.block.Block
import cn.nukkit.blockstate.BlockState
import cn.nukkit.command.Command
import cn.nukkit.command.CommandSender
import cn.nukkit.command.data.CommandEnum
import cn.nukkit.command.data.CommandParamType
import cn.nukkit.command.data.CommandParameter
import cn.nukkit.level.Location
import org.json.simple.JSONArray
import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser
import java.io.File
import kotlin.math.max
import kotlin.math.min


class JsonWorldCommand(plugin: Main) :
    Command("jsonworld", "Convert World to JsonWorld", "/jsonworld [save] ... ex", arrayOf("jw")) {
    private var plugins: Main = plugin

    init {
        plugins = plugin

        commandParameters.clear()
        commandParameters["save"] = arrayOf(
            CommandParameter.newEnum("save", arrayOf("save")),
            CommandParameter.newType("pos1", true, CommandParamType.BLOCK_POSITION),
            CommandParameter.newType("pos2", true, CommandParamType.BLOCK_POSITION),
            CommandParameter.newType("filename", true, CommandParamType.STRING),
            CommandParameter.newEnum("save_air", false, CommandEnum.ENUM_BOOLEAN)
        )
        commandParameters["load"] = arrayOf(
            CommandParameter.newEnum("load", arrayOf("load")),
            CommandParameter.newEnum("filename", true, filelist()),
            CommandParameter.newEnum("load_air", false, CommandEnum.ENUM_BOOLEAN)
        )
        commandParameters["list"] = arrayOf(
            CommandParameter.newEnum("list", arrayOf("list"))
        )
    }

    override fun execute(sender: CommandSender, commandLabel: String, args: Array<String>): Boolean {
        if (!Main().jsonWorldDir.exists()) {
            Main().jsonWorldDir.mkdirs()
        }
        if (sender is Player) {
            if (args[0] == "save") {
                val pos1 = Location(
                    args[1].toDouble(),
                    args[2].toDouble(),
                    args[3].toDouble(),
                    sender.level
                )
                val pos2 = Location(
                    args[4].toDouble(),
                    args[5].toDouble(),
                    args[6].toDouble(),
                    sender.level
                )
                var save_air = false
                try { save_air = args[8].toBoolean() } catch (_: Exception) { }
                var x1 = min(pos1.floorX, pos2.floorX)
                var x2 = max(pos1.floorX, pos2.floorX)
                var y1 = min(pos1.floorY, pos2.floorY)
                var y2 = max(pos1.floorY, pos2.floorY)
                var z1 = min(pos1.floorZ, pos2.floorZ)
                var z2 = max(pos1.floorZ, pos2.floorZ)
                val blockDataFile = File(Main().jsonWorldDir, "${args[7]}.json")
                blockDataFile.writeText("")
                blockDataFile.appendText("{\"json_world\":[")
                for (x in x1..x2) {
                    for (y in y1..y2) {
                        for (z in z1..z2) {
                            val location = Location(x.toDouble(), y.toDouble(), z.toDouble(), sender.level)
                            val block = sender.level.getBlock(location)
                            val blockS = BlockState.of(block.id).stateId.replaceAfter(";", "").replace(";", "")
                            if (blockS == "minecraft:air" && !save_air) continue
                            blockDataFile.appendText("{\"block_id\":\"${blockS}\",\"block_data\":${block.dataStorage},\"position\":{\"x\":${x},\"y\":${y},\"z\":${z}}},")
                        }
                    }
                }
                blockDataFile.appendText("{\"block_id\":\"OhmFn X2\",\"block_data\":69,\"position\":{\"x\":0,\"y\":500,\"z\":0}}]}")
                sender.sendMessage("save complete!!")
            }
            if (args[0] == "load") {
                sender.sendMessage("load")
                val blockDataFile = File(Main().jsonWorldDir, "${args[1]}.json")
                if (!blockDataFile.exists()) {
                    sender.sendMessage("${args[1]}: file not found")
                    return false
                }
                var load_air = false
                try { load_air = args[2].toBoolean() } catch (_: Exception) { }
                val parser = JSONParser()
                val jsonObject = parser.parse(blockDataFile.readText()) as JSONObject
                val blocks = jsonObject["json_world"] as JSONArray
                for (block in blocks) {
                    val blockObject = block as JSONObject
                    val block_id = blockObject["block_id"] as String
                    val block_data = Math.toIntExact(blockObject["block_data"] as Long)
                    val position = blockObject["position"] as JSONObject
                    val x = Math.toIntExact(position["x"] as Long)
                    val y = Math.toIntExact(position["y"] as Long)
                    val z = Math.toIntExact(position["z"] as Long)
                    val location = Location(x.toDouble(), y.toDouble(), z.toDouble(), sender.level)
                    try {
                        val blockState = BlockState.of(block_id)
                        if(block_id == "minecraft:air" && !load_air) continue
                        if(y >= 500) continue
                        sender.level.setBlock(location, Block.get(blockState.blockId, block_data))
                    } catch (_: Exception) {
                        sender.sendMessage("$block_id: block not found")
                    }
                }
                sender.sendMessage("loaded!")
            }
            if (args[0] == "list") {
                val blockDataFileList = Main().jsonWorldDir.listFiles()
                if (blockDataFileList != null) {
                    if (blockDataFileList.none()) {
                        sender.sendMessage("no file")
                        return false
                    }
                }
                for (blockDataFile in blockDataFileList!!) {
                    sender.sendMessage("List: " + blockDataFile.name.replace(".json", ""))
                }
            }
        }
        return false
    }

    private fun filelist(): Array<String> {
        var list: Array<String> = arrayOf("")
        val a = Main().jsonWorldDir.listFiles()
        if (a != null) {
            list = a.map { it.name.replace(".json", "") }.toTypedArray()
        }
        return list
    }
}