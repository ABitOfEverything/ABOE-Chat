package me.glorantq.aboe.chat.client.channels

import io.netty.buffer.ByteBuf
import me.glorantq.aboe.chat.common.PacketUtils

class ClientChatChannel(val name: String, val connectedUsers: Int, val canConnect: Boolean) {
    companion object {
        fun fromBytes(buf: ByteBuf): ClientChatChannel = ClientChatChannel(PacketUtils.readString(buf), buf.readInt(), buf.readBoolean())
    }

    fun toBytes(buf: ByteBuf): ByteBuf {
        PacketUtils.writeString(buf, name)
        buf.writeInt(connectedUsers)
        buf.writeBoolean(canConnect)

        return buf
    }
}
