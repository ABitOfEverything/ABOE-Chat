package me.glorantq.aboe.chat.server.channels;

import net.minecraft.entity.player.EntityPlayer;

public class DefaultChatChannel extends ChatChannel {

    DefaultChatChannel() {
        super("general", "General");
    }

    @Override
    public boolean canJoin(EntityPlayer entityPlayer) {
        return true;
    }
}
