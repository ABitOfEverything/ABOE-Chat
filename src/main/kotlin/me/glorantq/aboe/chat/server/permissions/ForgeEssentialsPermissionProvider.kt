package me.glorantq.aboe.chat.server.permissions

import com.forgeessentials.api.APIRegistry
import com.forgeessentials.api.UserIdent
import com.forgeessentials.api.permissions.FEPermissions
import com.forgeessentials.api.permissions.GroupEntry
import com.forgeessentials.api.permissions.ServerZone
import com.forgeessentials.chat.ModuleChat
import net.minecraft.entity.player.EntityPlayer
import net.minecraftforge.permission.PermissionManager
import java.util.*

class ForgeEssentialsPermissionProvider : IPermissionProvider {

    override fun hasPermission(player: EntityPlayer, permission: String): Boolean {
        return APIRegistry.perms.checkPermission(player, permission)
    }

    override fun getGroup(player: EntityPlayer): String {
        return APIRegistry.perms.getPrimaryGroup(UserIdent.get(player))
    }

    override fun getPrefix(player: EntityPlayer): String {
        return searchPrefixSuffix(player, false)
    }

    override fun getSuffix(player: EntityPlayer): String {
        return searchPrefixSuffix(player, true)
    }

    private fun searchPrefixSuffix(player: EntityPlayer, isSuffix: Boolean): String {
        val userIdent: UserIdent = UserIdent.get(player)
        val serverZone: ServerZone = APIRegistry.perms.serverZone

        var fix: String? = ModuleChat.getPlayerPrefixSuffix(userIdent, isSuffix)
        if (fix != null && !fix.isEmpty()) {
            return fix
        }

        var highestPriorityFix: Int = Integer.MIN_VALUE

        val groupEntries: SortedSet<GroupEntry> = APIRegistry.perms.getPlayerGroups(userIdent)

        for (entry in groupEntries) {
            val groupFix: String? = serverZone.getGroupPermission(entry.group, if (isSuffix) FEPermissions.SUFFIX else FEPermissions.PREFIX)
            if (groupFix != null && !groupFix.isEmpty() && entry.priority > highestPriorityFix) {
                fix = groupFix

                highestPriorityFix = entry.priority
            }
        }

        return fix ?: ""
    }

    override fun registerPermission(permission: String, permissionLevel: IPermissionProvider.PermissionLevel) {
        PermissionManager.registerPermission(permission, net.minecraftforge.permission.PermissionLevel.fromInteger(permissionLevel.ordinal))
    }

    override fun getDisplayName(player: EntityPlayer): String {
        return ModuleChat.getPlayerNickname(player)
    }
}
