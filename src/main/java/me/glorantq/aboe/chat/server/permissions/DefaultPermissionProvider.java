package me.glorantq.aboe.chat.server.permissions;

import cpw.mods.fml.common.FMLCommonHandler;
import net.minecraft.entity.player.EntityPlayer;

public class DefaultPermissionProvider implements PermissionProvider {

    @Override
    public boolean hasPermission(EntityPlayer player, String permission) {
        return FMLCommonHandler.instance().getMinecraftServerInstance().getConfigurationManager().func_152596_g(player.getGameProfile());
    }

    @Override
    public String getGroup(EntityPlayer player) {
        return "No Group";
    }

    @Override
    public String getPrefix(EntityPlayer player) {
        return "";
    }

    @Override
    public String getSuffix(EntityPlayer player) {
        return "";
    }

    @Override
    public void registerPermission(String permission, PermissionLevel permissionLevel) {

    }
}
