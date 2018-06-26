package me.glorantq.aboe.chat.common;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import me.glorantq.aboe.chat.client.channels.ClientChatChannel;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class PacketChangeChannelResponse implements IMessage {
    private boolean successful;
    private String extraMessage;
    private List<ClientChatChannel> changedChannels;

    @Override
    public void fromBytes(ByteBuf buf) {
        this.successful = buf.readBoolean();
        this.extraMessage = PacketUtils.readString(buf);

        this.changedChannels = new ArrayList<>();
        int length = buf.readInt();
        for(int i = 0; i < length; i++) {
            this.changedChannels.add(ClientChatChannel.fromBytes(buf));
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeBoolean(successful);
        PacketUtils.writeString(buf, extraMessage);

        buf.writeInt(changedChannels.size());
        for(ClientChatChannel channel : changedChannels) {
            channel.toBytes(buf);
        }
    }
}
