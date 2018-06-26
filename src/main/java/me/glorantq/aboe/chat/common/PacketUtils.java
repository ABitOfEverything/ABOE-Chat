package me.glorantq.aboe.chat.common;

import io.netty.buffer.ByteBuf;

import java.nio.charset.Charset;

public class PacketUtils {
    public static ByteBuf writeString(ByteBuf buffer, String value) {
        buffer.writeInt(value.length());
        buffer.writeBytes(value.getBytes());

        return buffer;
    }

    public static String readString(ByteBuf buffer) {
        int length = buffer.readInt();
        byte[] data = new byte[length];

        buffer.readBytes(data);

        return new String(data, Charset.forName("UTF-8"));
    }
}
