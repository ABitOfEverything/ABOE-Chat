package me.glorantq.aboe.chat.server.commands

import com.google.common.base.Joiner
import com.google.common.base.Optional
import cpw.mods.fml.common.FMLCommonHandler
import me.glorantq.aboe.chat.ABOEChat
import me.glorantq.aboe.chat.server.channels.ChatChannel
import me.glorantq.aboe.chat.server.channels.ChatChannelManager
import net.minecraft.command.ICommandSender
import net.minecraft.entity.player.EntityPlayer

class ChannelAdminCommand : ABOECommand() {
    private val channelManager: ChatChannelManager = ABOEChat.instance.chatChannelManager!!

    override fun getCommandName(): String ="channeladmin"
    override fun getCommandUsage(sender: ICommandSender): String = "/channeladmin <create|delete|move>"
    override fun getRequiredPermissionLevel(): Int = 4

    override fun processCommand(sender: ICommandSender, arguments: Array<String>) {
        if (arguments.isEmpty()) {
            sendMessage(sender, "&cUsage: /channeladmin <create|delete|move>")
            return
        }

        val subcommand: String = arguments[0]

        if (subcommand.equals("create", ignoreCase = true)) {
            if (arguments.size < 3) {
                sendMessage(sender, "&cUsage: /channeladmin create <channel_id> <name>")
                return
            }

            val id: String = arguments[1]
            val nameParts: Array<String?> = arrayOfNulls(arguments.size - 2)
            System.arraycopy(arguments, 2, nameParts, 0, nameParts.size)

            val name: String = Joiner.on(" ").join(nameParts)

            val createdChannel: Optional<ChatChannel> = channelManager.createChannel(id, name)
            if (!createdChannel.isPresent) {
                sendMessage(sender, "&cFailed to create channel!")
            } else {
                sendMessage(sender, "&aChannel \"&2$name&a\" created successfully!")
                if (sender is EntityPlayer) {
                    createdChannel.get().join(sender)
                }
            }

            return
        }

        if (subcommand.equals("delete", ignoreCase = true)) {
            if (arguments.size != 2) {
                sendMessage(sender, "&cUsage: /channeladmin delete <channel_id>")
                return
            }

            val deleted: Boolean = channelManager.deleteChannel(arguments[1])

            if (deleted) {
                sendMessage(sender, "&aChannel deleted successfully!")
            } else {
                sendMessage(sender, "&cFailed to delete channel!")
            }

            return
        }

        if (subcommand.equals("move", ignoreCase = true)) {
            if (arguments.size != 3) {
                sendMessage(sender, "&cUsage: /channeladmin move <player> <channel_id>")
                return
            }

            val toMove: EntityPlayer? = FMLCommonHandler.instance().minecraftServerInstance.configurationManager.func_152612_a(arguments[1])
            if (toMove == null) {
                sendMessage(sender, "&cThe player &4" + arguments[1] + "&c is invalid!")
                return
            }

            val toMoveTo: Optional<ChatChannel> = channelManager.getChannel(arguments[2])
            if (!toMoveTo.isPresent) {
                sendMessage(sender, "&cThe channel &4" + arguments[2] + "&c is invalid!")
                return
            }

            toMoveTo.get().forceJoin(toMove)

            sendMessage(sender, "&2" + toMove.gameProfile.name + "&a was moved to \"&2" + toMoveTo.get().channelName + "&a\"!")
            return
        }

        sendMessage(sender, "&cUsage: /channeladmin <create|delete|move>")
    }
}
