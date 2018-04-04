package me.glorantq.aboe.chat.server.permissions;

import com.forgeessentials.api.APIRegistry;
import com.forgeessentials.api.UserIdent;
import com.forgeessentials.api.permissions.FEPermissions;
import com.forgeessentials.api.permissions.GroupEntry;
import com.forgeessentials.api.permissions.Zone;
import com.forgeessentials.chat.ModuleChat;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.permission.PermissionLevel;
import net.minecraftforge.permission.PermissionManager;

import java.util.Set;

public class ForgeEssentialsPermissionProvider implements PermissionProvider {

    @Override
    public boolean hasPermission(EntityPlayer player, String permission) {
        return APIRegistry.perms.checkPermission(player, permission);
    }

    @Override
    public String getGroup(EntityPlayer player) {
        return APIRegistry.perms.getPrimaryGroup(UserIdent.get(player));
    }

    @Override
    public String getPrefix(EntityPlayer player) {
        return searchPrefixSuffix(player, false);
    }

    @Override
    public String getSuffix(EntityPlayer player) {
        return searchPrefixSuffix(player, true);
    }

    private String searchPrefixSuffix(EntityPlayer player, boolean isSuffix) {
        UserIdent userIdent = UserIdent.get(player);
        Zone serverZone = APIRegistry.perms.getServerZone();

        String fix = ModuleChat.getPlayerPrefixSuffix(userIdent, isSuffix);
        if(fix != null && !fix.isEmpty()) {
            return fix;
        }

        int highestPriorityFix = Integer.MIN_VALUE;

        Set<GroupEntry> groupEntries = APIRegistry.perms.getPlayerGroups(userIdent);

        for(GroupEntry entry : groupEntries) {
            String groupFix = serverZone.getGroupPermission(entry.getGroup(), isSuffix ? FEPermissions.SUFFIX : FEPermissions.PREFIX);
            if(groupFix != null && !groupFix.isEmpty() && entry.getPriority() > highestPriorityFix) {
                fix = groupFix;

                highestPriorityFix = entry.getPriority();
            }
        }

        if(fix == null) {
            return "";
        }

        return fix;
    }

    @Override
    public void registerPermission(String permission, PermissionLevel permissionLevel) {
        PermissionManager.registerPermission(permission, net.minecraftforge.permission.PermissionLevel.fromInteger(permissionLevel.ordinal()));
    }
}
