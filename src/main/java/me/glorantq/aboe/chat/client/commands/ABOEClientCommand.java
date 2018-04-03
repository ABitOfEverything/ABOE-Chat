package me.glorantq.aboe.chat.client.commands;

import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;

public abstract class ABOEClientCommand implements ICommand {

    void sendMessage(ICommandSender sender, String message) {
        sender.addChatMessage(new ChatComponentText(message.replace("&", "\u00A7")));
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender p_71519_1_) {
        return true;
    }

    @Override
    public boolean isUsernameIndex(String[] p_82358_1_, int p_82358_2_) {
        return false;
    }

    @Override
    public int compareTo(Object o) {
        return 0;
    }
}
