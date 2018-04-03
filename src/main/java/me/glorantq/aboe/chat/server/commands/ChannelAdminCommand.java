package me.glorantq.aboe.chat.server.commands;

import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import cpw.mods.fml.common.FMLCommonHandler;
import me.glorantq.aboe.chat.ABOEChat;
import me.glorantq.aboe.chat.server.channels.ChatChannel;
import me.glorantq.aboe.chat.server.channels.ChatChannelManager;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;

public class ChannelAdminCommand extends ABOECommand {
    private final ChatChannelManager channelManager = ABOEChat.getInstance().getChatChannelManager();

    @Override
    public String getCommandName() {
        return "channeladmin";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/channeladmin <create|delete|move>";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] arguments) {
        if(arguments.length == 0) {
            sendMessage(sender, "&cUsage: /channeladmin <create|delete|move>");
            return;
        }

        String subcommand = arguments[0];

        if(subcommand.equalsIgnoreCase("create")) {
            if(arguments.length < 3) {
                sendMessage(sender, "&cUsage: /channeladmin create <channel_id> <name>");
                return;
            }

            String id = arguments[1];
            String[] nameParts = new String[arguments.length - 2];
            System.arraycopy(arguments, 2, nameParts,  0, nameParts.length);

            String name = Joiner.on(" ").join(nameParts);

            Optional<ChatChannel> createdChannel = channelManager.createChannel(id, name);
            if(!createdChannel.isPresent()) {
                sendMessage(sender, "&cFailed to create channel!");
            } else {
                sendMessage(sender, "&aChannel \"&2" + name + "&a\" created successfully!");
                if(sender instanceof EntityPlayer) {
                    createdChannel.get().join((EntityPlayer) sender);
                }
            }

            return;
        }

        if(subcommand.equalsIgnoreCase("delete")) {
            if(arguments.length != 2) {
                sendMessage(sender, "&cUsage: /channeladmin delete <channel_id>");
                return;
            }

            boolean deleted = channelManager.deleteChannel(arguments[1]);

            if(deleted) {
                sendMessage(sender, "&aChannel deleted successfully!");
            } else {
                sendMessage(sender, "&cFailed to delete channel!");
            }

            return;
        }

        if(subcommand.equalsIgnoreCase("move")) {
            if(arguments.length != 3) {
                sendMessage(sender, "&cUsage: /channeladmin move <player> <channel_id>");
                return;
            }

            EntityPlayer toMove = FMLCommonHandler.instance().getMinecraftServerInstance().getConfigurationManager().func_152612_a(arguments[1]);
            if(toMove == null) {
                sendMessage(sender, "&cThe player &4" + arguments[1] + "&c is invalid!");
                return;
            }

            Optional<ChatChannel> toMoveTo = channelManager.getChannel(arguments[2]);
            if(!toMoveTo.isPresent()) {
                sendMessage(sender, "&cThe channel &4" + arguments[2] + "&c is invalid!");
                return;
            }

            toMoveTo.get().forceJoin(toMove);

            sendMessage(sender, "&2" + toMove.getGameProfile().getName() + "&a was moved to \"&2" + toMoveTo.get().getChannelName() + "&a\"!");
            return;
        }

        sendMessage(sender, "&cUsage: /channeladmin <create|delete|move>");
    }
}
