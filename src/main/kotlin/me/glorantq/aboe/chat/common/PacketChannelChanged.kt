package me.glorantq.aboe.chat.common

import cpw.mods.fml.common.network.simpleimpl.IMessage
import io.netty.buffer.ByteBuf

class PacketChannelChanged(var newChannel: String) : IMessage {
    constructor() : this("")

    override fun fromBytes(buf: ByteBuf) {
        this.newChannel = PacketUtils.readString(buf)
    }

    override fun toBytes(buf: ByteBuf) {
        PacketUtils.writeString(buf, this.newChannel)
    }
}
