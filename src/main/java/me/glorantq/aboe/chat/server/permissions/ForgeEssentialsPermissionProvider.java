package me.glorantq.aboe.chat.server.permissions;

import com.forgeessentials.api.APIRegistry;
import com.forgeessentials.api.UserIdent;
import com.forgeessentials.api.permissions.FEPermissions;
import com.forgeessentials.chat.ModuleChat;
import net.minecraft.entity.player.EntityPlayer;

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
        String prefix = ModuleChat.getPlayerPrefixSuffix(UserIdent.get(player), false);
        if(prefix == null || prefix.trim().length() == 0) {
            prefix = APIRegistry.perms.getGroupPermissionProperty(APIRegistry.perms.getPrimaryGroup(UserIdent.get(player)), FEPermissions.PREFIX);
        }

        if(prefix == null) {
            return "";
        }

        return prefix;
    }

    @Override
    public String getSuffix(EntityPlayer player) {
        String suffix = ModuleChat.getPlayerPrefixSuffix(UserIdent.get(player), true);
        if(suffix == null || suffix.trim().length() == 0) {
            suffix = APIRegistry.perms.getGroupPermissionProperty(APIRegistry.perms.getPrimaryGroup(UserIdent.get(player)), FEPermissions.PREFIX);
        }

        if(suffix == null) {
            return "";
        }

        return suffix;
    }
}
