package me.glorantq.aboe.chat.server.commands;

import me.glorantq.aboe.chat.ABOEChat;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.permission.PermissionLevel;
import net.minecraftforge.permission.PermissionManager;

public abstract class ABOECommand extends CommandBase {
    public ABOECommand() {
        PermissionManager.registerCommandPermission(this, getPermission(), PermissionLevel.OP);
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

    public String getPermission() {
        return "aboechat.commands." + getCommandName();
    }
}
