package me.glorantq.aboe.chat.server.permissions

import cpw.mods.fml.common.FMLCommonHandler
import net.minecraft.entity.player.EntityPlayer

class DefaultPermissionProvider : IPermissionProvider {

    override fun hasPermission(player: EntityPlayer, permission: String): Boolean {
        return FMLCommonHandler.instance().minecraftServerInstance.configurationManager.func_152596_g(player.gameProfile)
    }

    override fun getGroup(player: EntityPlayer): String {
        return "No Group"
    }

    override fun getPrefix(player: EntityPlayer): String {
        return ""
    }

    override fun getSuffix(player: EntityPlayer): String {
        return ""
    }

    override fun registerPermission(permission: String, permissionLevel: IPermissionProvider.PermissionLevel) {

    }

    override fun getDisplayName(player: EntityPlayer): String {
        return player.displayName
    }
}
