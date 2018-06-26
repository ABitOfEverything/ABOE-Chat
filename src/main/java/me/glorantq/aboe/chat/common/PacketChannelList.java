package me.glorantq.aboe.chat.common;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import me.glorantq.aboe.chat.client.channels.ClientChatChannel;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class PacketChannelList implements IMessage {
    private List<ClientChatChannel> channels;

    @Override
    public void fromBytes(ByteBuf buf) {
        this.channels = new ArrayList<>();
        int length = buf.readInt();
        for(int i = 0; i < length; i++) {
            this.channels.add(ClientChatChannel.fromBytes(buf));
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(channels.size());
        for(ClientChatChannel channel : channels) {
            channel.toBytes(buf);
        }
    }
}
