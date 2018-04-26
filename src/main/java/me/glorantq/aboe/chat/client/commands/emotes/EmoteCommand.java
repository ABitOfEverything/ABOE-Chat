package me.glorantq.aboe.chat.client.commands.emotes;

import me.glorantq.aboe.chat.client.commands.ABOEClientCommand;
import net.minecraft.client.Minecraft;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;

import java.util.ArrayList;
import java.util.List;

public class EmoteCommand extends ABOEClientCommand {
    private final String emoteName;
    private final String emote;

    public EmoteCommand(String emoteName, String emote) {
        this.emoteName = emoteName;
        this.emote = emote;
    }

    @Override
    public String getCommandName() {
        return emoteName;
    }

    @Override
    public String getCommandUsage(ICommandSender p_71518_1_) {
        return "/" + emoteName;
    }

    @Override
    public List getCommandAliases() {
        return new ArrayList();
    }

    @Override
    public void processCommand(ICommandSender p_71515_1_, String[] p_71515_2_) {
        if(!(p_71515_1_ instanceof EntityPlayer)) {
            p_71515_1_.addChatMessage(new ChatComponentText("Only players can use this command!"));
            return;
        }

        Minecraft.getMinecraft().thePlayer.sendChatMessage(emote);
    }

    @Override
    public List addTabCompletionOptions(ICommandSender p_71516_1_, String[] p_71516_2_) {
        return new ArrayList();
    }
}
