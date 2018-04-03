package me.glorantq.aboe.chat.server.permissions;

import net.minecraft.entity.player.EntityPlayer;

public interface PermissionProvider {
    boolean hasPermission(EntityPlayer player, String permission);
    String getGroup(EntityPlayer player);
    String getPrefix(EntityPlayer player);
    String getSuffix(EntityPlayer player);
}
