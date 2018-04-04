package me.glorantq.aboe.chat.server.permissions;

import net.minecraft.entity.player.EntityPlayer;

public interface PermissionProvider {
    boolean hasPermission(EntityPlayer player, String permission);

    String getGroup(EntityPlayer player);

    String getPrefix(EntityPlayer player);

    String getSuffix(EntityPlayer player);

    void registerPermission(String permission, PermissionLevel permissionLevel);

    String getDisplayName(EntityPlayer player);

    enum PermissionLevel {
        TRUE,

        @Deprecated
        OP_1,

        @Deprecated
        OP_2,

        @Deprecated
        OP_3,

        OP,

        FALSE;
    }
}
