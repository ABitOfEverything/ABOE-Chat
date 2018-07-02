package me.glorantq.aboe.chat.common

import cpw.mods.fml.common.network.simpleimpl.IMessage
import io.netty.buffer.ByteBuf

data class PacketUserMentioned(var mentioningUser: String, var message: String, var isEveryoneMention: Boolean) : IMessage {
    constructor() : this("", "", false)

    override fun fromBytes(buf: ByteBuf) {
        this.mentioningUser = PacketUtils.readString(buf)
        this.message = PacketUtils.readString(buf)
        this.isEveryoneMention = buf.readBoolean()
    }

    override fun toBytes(buf: ByteBuf) {
        PacketUtils.writeString(buf, mentioningUser)
        PacketUtils.writeString(buf, message)
        buf.writeBoolean(isEveryoneMention)
    }
}
