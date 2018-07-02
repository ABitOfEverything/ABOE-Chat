package me.glorantq.aboe.chat.common

import cpw.mods.fml.common.network.simpleimpl.IMessage
import io.netty.buffer.ByteBuf
import me.glorantq.aboe.chat.ClientChannelList
import me.glorantq.aboe.chat.client.channels.ClientChatChannel
import java.util.*

data class PacketChannelList(var channels: ClientChannelList) : IMessage {
    constructor() : this(mutableListOf())

    override fun fromBytes(buf: ByteBuf) {
        this.channels = ArrayList()
        val length = buf.readInt()
        for (i in 0 until length) {
            this.channels.add(ClientChatChannel.fromBytes(buf))
        }
    }

    override fun toBytes(buf: ByteBuf) {
        buf.writeInt(channels.size)
        for (channel in channels) {
            channel.toBytes(buf)
        }
    }
}
