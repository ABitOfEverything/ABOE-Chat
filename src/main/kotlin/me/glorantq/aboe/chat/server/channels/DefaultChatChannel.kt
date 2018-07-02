package me.glorantq.aboe.chat.server.channels

import net.minecraft.entity.player.EntityPlayer

class DefaultChatChannel internal constructor() : ChatChannel("general", "General") {
    override fun canJoin(entityPlayer: EntityPlayer): Boolean = true
}
