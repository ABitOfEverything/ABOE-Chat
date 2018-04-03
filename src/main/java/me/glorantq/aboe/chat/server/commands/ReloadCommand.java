package me.glorantq.aboe.chat.server.commands;

import me.glorantq.aboe.chat.ABOEChat;
import net.minecraft.command.ICommandSender;

public class ReloadCommand extends ABOECommand {

    @Override
    public String getCommandName() {
        return "aboechat_reload";
    }

    @Override
    public String getCommandUsage(ICommandSender p_71518_1_) {
        return "/aboechat_reload";
    }

    @Override
    public void processCommand(ICommandSender p_71515_1_, String[] p_71515_2_) {
        ABOEChat.getInstance().getChatChannelManager().reload();
    }
}
