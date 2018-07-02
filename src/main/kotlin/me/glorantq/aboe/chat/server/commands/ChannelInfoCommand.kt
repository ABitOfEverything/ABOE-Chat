package me.glorantq.aboe.chat.server.commands

import com.google.common.base.Optional
import me.glorantq.aboe.chat.ABOEChat
import me.glorantq.aboe.chat.ServerChannelMap
import me.glorantq.aboe.chat.server.channels.ChatChannel
import me.glorantq.aboe.chat.server.channels.ChatChannelManager
import net.minecraft.command.ICommandSender
import net.minecraft.entity.player.EntityPlayer

class ChannelInfoCommand : ABOECommand() {
    private val channelManager: ChatChannelManager = ABOEChat.instance.chatChannelManager!!

    override fun getCommandName(): String = "channel"
    override fun getCommandUsage(sender: ICommandSender): String = "Usage: /channel <info|leave|online|available|join>"

    override fun processCommand(sender: ICommandSender, arguments: Array<String>) {
        if (arguments.isEmpty()) {
            sendMessage(sender, "&cUsage: /channel <info|leave|online|available|join>")
            return
        }

        val subcommand: String = arguments[0]

        if (sender !is EntityPlayer) {
            sendMessage(sender, "&cOnly players can use this command!")
            return
        }

        val playerChannel: Optional<ChatChannel> = channelManager.getChannelForPlayer(sender)

        if (subcommand.equals("info", ignoreCase = true)) {
            if (!playerChannel.isPresent) {
                sendMessage(sender, "&cYou aren't in any channels at the moment!")
                return
            }

            val infoString: String = "&aYou are currently talking in \"&2${playerChannel.get().channelName}&a\" with &2${playerChannel.get().onlinePlayers.size - 1} &aother users!"
            sendMessage(sender, infoString)
            return
        }

        if (subcommand.equals("leave", ignoreCase = true)) {
            if (!playerChannel.isPresent) {
                sendMessage(sender, "&cYou aren't in any channels at the moment!")
                return
            }

            playerChannel.get().leave(sender)
            return
        }

        if (subcommand.equals("online", ignoreCase = true)) {
            if (!playerChannel.isPresent) {
                sendMessage(sender, "&cYou aren't in any channels at the moment!")
                return
            }

            val onlinePlayers: StringBuilder = StringBuilder()

            for (i in 0 until playerChannel.get().onlinePlayers.size) {
                onlinePlayers.append("&a")
                onlinePlayers.append(playerChannel.get().onlinePlayers[i].gameProfile.name)

                if (i != playerChannel.get().onlinePlayers.size - 1) {
                    onlinePlayers.append("&2, ")
                }
            }

            sendMessage(sender, "&aCurrently online players in \"&2${playerChannel.get().channelName}&a\": $onlinePlayers")
            return
        }

        if (subcommand.equals("available", ignoreCase = true)) {
            val availableChannels: ServerChannelMap = channelManager.channels
            val values: ArrayList<ChatChannel> = ArrayList(availableChannels.values)
            val channels: StringBuilder = StringBuilder()

            for (i in 0 until availableChannels.size) {
                val chatChannel: ChatChannel = values[i]
                val canJoin: Boolean = chatChannel.canJoin(sender)

                channels.append(if (canJoin) "&2" else "&4")
                channels.append(chatChannel.channelId)

                if (i != values.size - 1) {
                    channels.append("&r, ")
                }
            }

            sendMessage(sender, "&aAvailable channels: " + channels.toString())
            return
        }

        if (subcommand.equals("join", ignoreCase = true)) {
            if (arguments.size < 2) {
                sendMessage(sender, "&cUsage: /channel join <channel> | /channel available")
                return
            }

            val selectedChannel: Optional<ChatChannel> = channelManager.getChannel(arguments[1])
            if (!selectedChannel.isPresent) {
                sendMessage(sender, "&cThe channel &4" + arguments[1] + "&c is invalid!")
                return
            }

            val joined: Boolean = selectedChannel.get().join(sender)
            if (!joined) {
                sendMessage(sender, "&cYou don't have permission to join the \"&4" + selectedChannel.get().channelName + "&c\" channel!")
            }
            return
        }

        sendMessage(sender, "&cUsage: /channel <info|leave|online|available|join>")
    }
}
