package me.glorantq.aboe.chat.common;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class PacketUserMentioned implements IMessage {
    private String mentioningUser;
    private String message;
    private boolean everyoneMention;

    @Override
    public void fromBytes(ByteBuf buf) {
        this.mentioningUser = PacketUtils.readString(buf);
        this.message = PacketUtils.readString(buf);
        this.everyoneMention = buf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        PacketUtils.writeString(buf, mentioningUser);
        PacketUtils.writeString(buf, message);
        buf.writeBoolean(everyoneMention);
    }
}
