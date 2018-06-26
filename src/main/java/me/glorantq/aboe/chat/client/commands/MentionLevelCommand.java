package me.glorantq.aboe.chat.client.commands;

import me.glorantq.aboe.chat.ABOEChat;
import me.glorantq.aboe.chat.client.chat.ChatGUIInjector;
import net.minecraft.command.ICommandSender;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MentionLevelCommand extends ABOEClientCommand {

    @Override
    public String getCommandName() {
        return "mentions";
    }

    @Override
    public String getCommandUsage(ICommandSender p_71518_1_) {
        return "/mentions [off|player|everyone]";
    }

    @Override
    public List getCommandAliases() {
        return new ArrayList();
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if (args.length < 1) {
            ChatGUIInjector.MentionLevel mentionLevel = ABOEChat.getInstance().getChatGUIInjector().getMentionLevel();
            sendMessage(sender, "&aYour mentions settings are set to \"&2" + mentionLevel.name() + "&a\"!");

            return;
        }

        String subcommand = args[0];
        ChatGUIInjector.MentionLevel mentionLevel;

        switch (subcommand) {
            case "off":
                mentionLevel = ChatGUIInjector.MentionLevel.NONE;
                break;
            case "player":
                mentionLevel = ChatGUIInjector.MentionLevel.PLAYER;
                break;
            case "everyone":
                mentionLevel = ChatGUIInjector.MentionLevel.EVERYONE;
                break;
            default:
                sendMessage(sender, "&cUsage: /mentions <off|player|everyone>");
                return;
        }

        ABOEChat.getInstance().getChatGUIInjector().changeMentionLevel(mentionLevel);
        String message;

        switch (mentionLevel) {
            case NONE:
                message = "You are no longer receiving mentions!";
                break;
            case PLAYER:
                message = "You only receive player mentions!";
                break;
            case EVERYONE:
                message = "You are now receiving player and everyone mentions!";
                break;
            default:
                return;
        }

        sendMessage(sender, "&a" + message);
    }

    @Override
    public List addTabCompletionOptions(ICommandSender p_71516_1_, String[] p_71516_2_) {
        return Arrays.asList("off", "player", "everyone");
    }
}
