package me.glorantq.aboe.chat.common

import cpw.mods.fml.common.network.simpleimpl.IMessage
import io.netty.buffer.ByteBuf
import me.glorantq.aboe.chat.ClientChannelList
import me.glorantq.aboe.chat.client.channels.ClientChatChannel
import java.util.*

class PacketChangeChannelResponse(var successful: Boolean, var extraMessage: String, var changedChannels: ClientChannelList) : IMessage {
    constructor() : this(false, "", mutableListOf())

    override fun fromBytes(buf: ByteBuf) {
        this.successful = buf.readBoolean()
        this.extraMessage = PacketUtils.readString(buf)

        this.changedChannels = ArrayList()
        val length = buf.readInt()
        for (i in 0 until length) {
            this.changedChannels.add(ClientChatChannel.fromBytes(buf))
        }
    }

    override fun toBytes(buf: ByteBuf) {
        buf.writeBoolean(successful)
        PacketUtils.writeString(buf, extraMessage)

        buf.writeInt(changedChannels.size)
        for (channel in changedChannels) {
            channel.toBytes(buf)
        }
    }
}
