package me.glorantq.aboe.chat.client.channels

import cpw.mods.fml.common.network.simpleimpl.IMessage
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler
import cpw.mods.fml.common.network.simpleimpl.MessageContext
import me.glorantq.aboe.chat.ABOEChat
import me.glorantq.aboe.chat.ClientChannelList
import me.glorantq.aboe.chat.common.PacketChangeChannel
import me.glorantq.aboe.chat.common.PacketChangeChannelResponse
import me.glorantq.aboe.chat.common.PacketChannelChanged
import me.glorantq.aboe.chat.common.PacketChannelList
import org.apache.logging.log4j.LogManager
import java.util.*

class ClientChannelManager {
    private val logger = LogManager.getLogger("ClientChannelManager")

    private val aboeChat: ABOEChat = ABOEChat.instance
    var currentChannel = "#unknown"
        private set
    val availableChannels: ClientChannelList = ArrayList()

    private fun onChannelChanged(newChannel: String) {
        this.currentChannel = newChannel
        logger.info("Channel changed to: {}", newChannel)
    }

    private fun onChannelStatusChanged(changed: List<ClientChatChannel>) {
        synchronized(availableChannels) {
            availableChannels.clear()
            availableChannels.addAll(changed)
        }
    }

    private fun onChangeChannelResponse(successful: Boolean, extraMessage: String, changedChannels: List<ClientChatChannel>) {
        println("Change: " + successful + "  " + extraMessage + "  " + changedChannels.size)
    }

    fun changeToChannel(channelName: String) {
        aboeChat.simpleNetworkWrapper!!.sendToServer(PacketChangeChannel(channelName))
    }

    class PacketChannelChangedHandler : IMessageHandler<PacketChannelChanged, IMessage> {
        private val clientChannelManager: ClientChannelManager? = ABOEChat.instance.clientChannelManager

        override fun onMessage(message: PacketChannelChanged, ctx: MessageContext): IMessage? {
            clientChannelManager!!.onChannelChanged(message.newChannel)

            return null
        }
    }

    class PacketChannelListHandler : IMessageHandler<PacketChannelList, IMessage> {
        private val clientChannelManager: ClientChannelManager? = ABOEChat.instance.clientChannelManager

        override fun onMessage(message: PacketChannelList, ctx: MessageContext): IMessage? {
            clientChannelManager!!.onChannelStatusChanged(message.channels)

            return null
        }
    }

    class PacketChangeChannelResponseHandler : IMessageHandler<PacketChangeChannelResponse, IMessage> {
        private val clientChannelManager: ClientChannelManager? = ABOEChat.instance.clientChannelManager

        override fun onMessage(message: PacketChangeChannelResponse, ctx: MessageContext): IMessage? {
            clientChannelManager!!.onChangeChannelResponse(message.successful, message.extraMessage, message.changedChannels)

            return null
        }
    }
}
