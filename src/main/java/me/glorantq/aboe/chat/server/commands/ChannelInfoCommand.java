package me.glorantq.aboe.chat.server.commands;

import com.google.common.base.Optional;
import me.glorantq.aboe.chat.ABOEChat;
import me.glorantq.aboe.chat.server.channels.ChatChannel;
import me.glorantq.aboe.chat.server.channels.ChatChannelManager;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ChannelInfoCommand extends ABOECommand {
    private final ChatChannelManager channelManager = ABOEChat.getInstance().getChatChannelManager();

    @Override
    public String getCommandName() {
        return "channel";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "Usage: /channel <info|leave|online|available|join>";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] arguments) {
        if(arguments.length == 0) {
            sendMessage(sender, "&cUsage: /channel <info|leave|online|available|join>");
            return;
        }

        String subcommand = arguments[0];

        if(!(sender instanceof EntityPlayer)) {
            sendMessage(sender, "&cOnly players can use this command!");
            return;
        }

        EntityPlayer player = (EntityPlayer) sender;
        Optional<ChatChannel> playerChannel = channelManager.getChannelForPlayer(player);

        if(subcommand.equalsIgnoreCase("info")) {
            if(!playerChannel.isPresent()) {
                sendMessage(player, "&cYou aren't in any channels at the moment!");
                return;
            }

            String infoString = String.format("&aYou are currently talking in \"&2%s&a\" with &2%d &aother users!", playerChannel.get().getChannelName(), playerChannel.get().getOnlinePlayers().size() - 1);
            sendMessage(player, infoString);
            return;
        }

        if(subcommand.equalsIgnoreCase("leave")) {
            if(!playerChannel.isPresent()) {
                sendMessage(player, "&cYou aren't in any channels at the moment!");
                return;
            }

            playerChannel.get().leave(player);
            return;
        }

        if(subcommand.equalsIgnoreCase("online")) {
            if(!playerChannel.isPresent()) {
                sendMessage(player, "&cYou aren't in any channels at the moment!");
                return;
            }

            StringBuilder onlinePlayers = new StringBuilder();

            for(int i = 0; i < playerChannel.get().getOnlinePlayers().size(); i++) {
                onlinePlayers.append("&a");
                onlinePlayers.append(playerChannel.get().getOnlinePlayers().get(i).getGameProfile().getName());

                if(i != playerChannel.get().getOnlinePlayers().size() - 1) {
                    onlinePlayers.append("&2, ");
                }
            }

            sendMessage(player, "&aCurrently online players in \"&2" + playerChannel.get().getChannelName() + "&a\": " + onlinePlayers.toString());
            return;
        }

        if(subcommand.equalsIgnoreCase("available")) {
            Map<String, ChatChannel> availableChannels = channelManager.getChannels();
            List<ChatChannel> values = new ArrayList<>(availableChannels.values());
            StringBuilder channels = new StringBuilder();

            for(int i = 0; i < availableChannels.size(); i++) {
                ChatChannel chatChannel = values.get(i);
                boolean canJoin = chatChannel.canJoin(player);

                channels.append(canJoin ? "&2" : "&4");
                channels.append(chatChannel.getChannelId());

                if(i != values.size() - 1) {
                    channels.append("&r, ");
                }
            }

            sendMessage(player, "&aAvailable channels: " + channels.toString());
            return;
        }

        if(subcommand.equalsIgnoreCase("join")) {
            if(arguments.length < 2) {
                sendMessage(player, "&cUsage: /channel join <channel> | /channel available");
                return;
            }

            Optional<ChatChannel> selectedChannel = channelManager.getChannel(arguments[1]);
            if(!selectedChannel.isPresent()) {
                sendMessage(player, "&cThe channel &4" + arguments[1] + "&c is invalid!");
                return;
            }

            boolean joined = selectedChannel.get().join(player);
            if(!joined) {
                sendMessage(player, "&cYou don't have permission to join the \"&4" + selectedChannel.get().getChannelName() + "&c\" channel!");
            }

            return;
        }

        sendMessage(sender, "&cUsage: /channel <info|leave|online|available|join>");
    }
}
