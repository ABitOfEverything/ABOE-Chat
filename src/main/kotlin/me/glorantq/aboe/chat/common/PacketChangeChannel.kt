package me.glorantq.aboe.chat.common

import cpw.mods.fml.common.network.simpleimpl.IMessage
import io.netty.buffer.ByteBuf

class PacketChangeChannel(var newChannel: String) : IMessage {

    override fun fromBytes(buf: ByteBuf) {
        this.newChannel = PacketUtils.readString(buf)
    }

    override fun toBytes(buf: ByteBuf) {
        PacketUtils.writeString(buf, this.newChannel)
    }
}
