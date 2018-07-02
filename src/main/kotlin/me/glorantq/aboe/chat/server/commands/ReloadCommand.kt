package me.glorantq.aboe.chat.server.commands

import me.glorantq.aboe.chat.ABOEChat
import net.minecraft.command.ICommandSender

class ReloadCommand : ABOECommand() {
    override fun getCommandName(): String = "aboechat_reload"
    override fun getCommandUsage(p_71518_1_: ICommandSender): String = "/aboechat_reload"
    override fun processCommand(p_71515_1_: ICommandSender, p_71515_2_: Array<String>) = ABOEChat.instance.chatChannelManager!!.reload()
}
