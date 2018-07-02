package me.glorantq.aboe.chat.client.commands

import me.glorantq.aboe.chat.convertColours
import net.minecraft.command.ICommand
import net.minecraft.command.ICommandSender
import net.minecraft.util.ChatComponentText

abstract class ABOEClientCommand : ICommand {

    internal fun sendMessage(sender: ICommandSender, message: String) = sender.addChatMessage(ChatComponentText(message.convertColours()))
    override fun canCommandSenderUseCommand(p_71519_1_: ICommandSender): Boolean = true
    override fun isUsernameIndex(p_82358_1_: Array<String>, p_82358_2_: Int): Boolean = false
    override fun compareTo(other: Any?): Int = 0
}
