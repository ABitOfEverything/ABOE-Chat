package me.glorantq.aboe.chat.common;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import io.netty.buffer.ByteBuf;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class PacketChannelChanged implements IMessage {
    private String newChannel;

    @Override
    public void fromBytes(ByteBuf buf) {
        this.newChannel = PacketUtils.readString(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        PacketUtils.writeString(buf, this.newChannel);
    }
}
