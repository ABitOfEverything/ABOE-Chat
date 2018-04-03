package me.glorantq.aboe.chat.server.permissions;

import com.forgeessentials.api.APIRegistry;
import com.forgeessentials.api.UserIdent;
import com.forgeessentials.api.permissions.FEPermissions;
import com.forgeessentials.api.permissions.GroupEntry;
import com.forgeessentials.api.permissions.Zone;
import com.forgeessentials.chat.ModuleChat;
import net.minecraft.entity.player.EntityPlayer;

import java.util.Set;
import java.util.SortedSet;

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
            System.out.println("player fix " + fix);
            return fix;
        }

        Set<GroupEntry> groupEntries = APIRegistry.perms.getPlayerGroups(userIdent);
        if(groupEntries.size() == 0) {
            return "";
        } else {
            fix = serverZone.getGroupPermission(((SortedSet<GroupEntry>) groupEntries).first().getGroup(), isSuffix ? FEPermissions.SUFFIX : FEPermissions.PREFIX);
        }

        if(fix == null) {
            System.out.println("null fix");
            return "";
        }

        return fix;
    }
}
