package me.glorantq.aboe.chat.server.commands

import cpw.mods.fml.common.FMLCommonHandler
import net.minecraft.command.ICommandSender
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.entity.player.EntityPlayerMP

class PingListCommand : ABOECommand() {
    override fun getCommandName(): String = "pinglist"
    override fun getCommandUsage(p_71518_1_: ICommandSender?): String = "/pinglist"

    @Suppress("UNCHECKED_CAST")
    override fun processCommand(sender: ICommandSender, args: Array<out String>) {
        val response: StringBuilder = StringBuilder()

        val onlinePlayers: List<EntityPlayerMP> = FMLCommonHandler.instance().minecraftServerInstance.configurationManager.playerEntityList as List<EntityPlayerMP>
        for(player: EntityPlayerMP in onlinePlayers) {
            response.append("&2")
                    .append(player.gameProfile.name)
                    .append("&a :: ")

            val ping: Int = player.ping
            val pingLevel: Byte = when {
                ping < 0 -> 5
                ping < 150 -> 0
                ping < 300 -> 1
                ping < 600 -> 2
                ping < 1000 -> 3
                else -> 4
            }

            val pingColour: String = when(pingLevel.toInt()) {
                5 -> "4"
                0 -> "2"
                1 -> "a"
                2 -> "e"
                3 -> "c"
                else -> "4"
            }

            response.append("&")
                    .append(pingColour)
                    .append(ping)
                    .append("ms")

            sendMessage(sender, response.toString())
            response.setLength(0)
        }
    }
}