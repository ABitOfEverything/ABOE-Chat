package me.glorantq.aboe.chat.client.channels;

import io.netty.buffer.ByteBuf;
import lombok.Data;
import me.glorantq.aboe.chat.common.PacketUtils;

@Data
public class ClientChatChannel {
    private final String name;
    private final int connectedUsers;
    private final boolean canConnect;

    public ByteBuf toBytes(ByteBuf buf) {
        PacketUtils.writeString(buf, name);
        buf.writeInt(connectedUsers);
        buf.writeBoolean(canConnect);

        return buf;
    }

    public static ClientChatChannel fromBytes(ByteBuf buf) {
        return new ClientChatChannel(PacketUtils.readString(buf), buf.readInt(), buf.readBoolean());
    }
}
