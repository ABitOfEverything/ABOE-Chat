package me.glorantq.aboe.chat.client.commands.emotes

import me.glorantq.aboe.chat.client.commands.ABOEClientCommand
import net.minecraft.client.Minecraft
import net.minecraft.command.ICommandSender
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.ChatComponentText

class EmoteCommand(private val emoteName: String, private val emote: String) : ABOEClientCommand() {
    override fun getCommandName(): String = emoteName
    override fun getCommandUsage(p_71518_1_: ICommandSender): String = "/$emoteName"
    override fun getCommandAliases(): List<*> = arrayListOf<Any>()
    override fun addTabCompletionOptions(p_71516_1_: ICommandSender, p_71516_2_: Array<String>): List<*> = arrayListOf<Any>()

    override fun processCommand(p_71515_1_: ICommandSender, p_71515_2_: Array<String>) {
        if (p_71515_1_ !is EntityPlayer) {
            p_71515_1_.addChatMessage(ChatComponentText("Only players can use this command!"))
            return
        }

        Minecraft.getMinecraft().thePlayer.sendChatMessage(emote)
    }
}
