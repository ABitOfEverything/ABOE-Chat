package me.glorantq.aboe.chat.common

import io.netty.buffer.ByteBuf

import java.nio.charset.Charset

object PacketUtils {
    @JvmStatic fun writeString(buffer: ByteBuf, value: String): ByteBuf {
        buffer.writeInt(value.length)
        buffer.writeBytes(value.toByteArray())

        return buffer
    }

    @JvmStatic fun readString(buffer: ByteBuf): String {
        val length = buffer.readInt()
        val data = ByteArray(length)

        buffer.readBytes(data)

        return String(data, Charset.forName("UTF-8"))
    }
}
