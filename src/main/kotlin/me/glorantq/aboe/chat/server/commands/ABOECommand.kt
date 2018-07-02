package me.glorantq.aboe.chat.server.commands

import me.glorantq.aboe.chat.ABOEChat
import me.glorantq.aboe.chat.convertColours
import me.glorantq.aboe.chat.server.permissions.IPermissionProvider
import net.minecraft.command.CommandBase
import net.minecraft.command.ICommandSender
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.ChatComponentText

abstract class ABOECommand internal constructor() : CommandBase() {

    private val permission: String
        get() = "aboechat.commands.$commandName"

    init {
        ABOEChat.instance.permissionProvider!!.registerPermission(permission, IPermissionProvider.PermissionLevel.OP)
    }

    internal fun sendMessage(sender: ICommandSender, message: String) = sender.addChatMessage(ChatComponentText(message.convertColours()))
    override fun getRequiredPermissionLevel(): Int = 0

    override fun canCommandSenderUseCommand(p_71519_1_: ICommandSender): Boolean =
            if (p_71519_1_ !is EntityPlayer) {
                true
            } else {
                ABOEChat.instance.permissionProvider!!.hasPermission(p_71519_1_, permission)
            }

}
