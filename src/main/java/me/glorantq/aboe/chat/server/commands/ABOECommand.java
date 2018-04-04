package me.glorantq.aboe.chat.server.commands;

import me.glorantq.aboe.chat.ABOEChat;
import me.glorantq.aboe.chat.server.permissions.PermissionProvider;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;

public abstract class ABOECommand extends CommandBase {
    ABOECommand() {
        ABOEChat.getInstance().getPermissionProvider().registerPermission(getPermission(), PermissionProvider.PermissionLevel.OP);
    }

    void sendMessage(ICommandSender sender, String message) {
        sender.addChatMessage(new ChatComponentText(message.replace("&", "\u00A7")));
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender p_71519_1_) {
        if(!(p_71519_1_ instanceof EntityPlayer)) {
            return true;
        }

        return ABOEChat.getInstance().getPermissionProvider().hasPermission((EntityPlayer) p_71519_1_, getPermission());
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    private String getPermission() {
        return "aboechat.commands." + getCommandName();
    }
}
