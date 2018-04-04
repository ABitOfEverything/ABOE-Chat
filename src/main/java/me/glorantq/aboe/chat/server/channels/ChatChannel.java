package me.glorantq.aboe.chat.server.channels;

import com.google.common.base.Optional;
import lombok.Data;
import lombok.Getter;
import me.glorantq.aboe.chat.ABOEChat;
import me.glorantq.aboe.chat.server.permissions.PermissionProvider;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.permission.PermissionLevel;
import net.minecraftforge.permission.PermissionManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.stringtemplate.v4.ST;

import java.util.ArrayList;
import java.util.List;

public class ChatChannel {
    private static final String CATEGORY_CHAT_SETTINGS = "Chat";
    static final String NBT_KEY_CHANNEL_DATA = "current_chat_channel";

    private static String chatFormat;

    private final ABOEChat chatMod = ABOEChat.getInstance();
    private final ChatChannelManager chatChannelManager = chatMod.getChatChannelManager();
    private final Logger logger;

    private final @Getter
    String channelId;
    private final @Getter
    String channelName;

    private final List<EntityPlayer> joinedPlayers = new ArrayList<>();

    ChatChannel(String id, String name) {
        this.channelId = id;
        this.channelName = name;

        this.logger = LogManager.getLogger("Channel-" + channelId);

        reloadFormatting();

        chatMod.getPermissionProvider().registerPermission("abochat.channels." + channelId, PermissionProvider.PermissionLevel.OP);
    }

    void messageReceived(EntityPlayerMP player, String message) {
        ST messageTemplate = new ST(chatFormat.replaceAll("&", "\u00A7"));
        messageTemplate.add("username", player.getGameProfile().getName());
        messageTemplate.add("display_name", player.getDisplayName());
        messageTemplate.add("group", chatMod.getPermissionProvider().getGroup(player));
        messageTemplate.add("channel_name", channelName);
        messageTemplate.add("channel_id", channelId);
        messageTemplate.add("message", message);
        messageTemplate.add("prefix", chatMod.getPermissionProvider().getPrefix(player).replaceAll("&", "\u00A7"));
        messageTemplate.add("suffix", chatMod.getPermissionProvider().getSuffix(player).replaceAll("&", "\u00A7"));

        String renderedMessage = messageTemplate.render();

        if (chatMod.getPermissionProvider().hasPermission(player, "aboechat.color")) {
            renderedMessage = renderedMessage.replaceAll("&", "\u00A7");
        }

        renderedMessage = renderedMessage.replaceAll(" +", " ");

        if (renderedMessage.contains("@everyone") && !chatMod.getPermissionProvider().hasPermission(player, "aboechat.mention.everyone")) {
            List<PlayerMention> mentions = getMentions(renderedMessage);
            for (int i = 0; i < mentions.size(); i++) {
                String before = renderedMessage.substring(0, mentions.get(i).index + i + 1);
                String after = renderedMessage.substring(mentions.get(i).index + i + 1, renderedMessage.length());

                renderedMessage = before + "\ufeff" + after;
            }
        }

        synchronized (joinedPlayers) {
            for (EntityPlayer otherPlayer : joinedPlayers) {
                otherPlayer.addChatMessage(new ChatComponentText(renderedMessage));
            }
        }
    }

    public boolean join(EntityPlayer player) {
        if (!canJoin(player)) {
            return false;
        }

        return forceJoin(player);
    }

    public boolean forceJoin(EntityPlayer player) {
        NBTTagCompound entityData = player.getEntityData();
        if (entityData.hasKey(NBT_KEY_CHANNEL_DATA)) {
            String currentChannel = entityData.getString(NBT_KEY_CHANNEL_DATA);
            Optional<ChatChannel> channel = chatChannelManager.getChannel(currentChannel);

            if (channel.isPresent()) {
                channel.get().leave(player);
            }
        }

        entityData.setString(NBT_KEY_CHANNEL_DATA, channelId);

        logger.info("Player {} joined channel {}", player.getDisplayName(), channelId);

        synchronized (joinedPlayers) {
            joinedPlayers.add(player);
        }

        player.addChatMessage(new ChatComponentText("\u00A7aYou are now talking in \"\u00A72" + channelName + "\u00A7a\"!"));

        return true;
    }

    public void leave(EntityPlayer player) {
        if (!joinedPlayers.contains(player)) {
            logger.warn("Player {} wasn't in the channel they're leaving!", player.getDisplayName());
            return;
        }

        NBTTagCompound entityData = player.getEntityData();
        if (!entityData.hasKey(NBT_KEY_CHANNEL_DATA)) {
            logger.error("Player is leaving the channel, but they don't have an NBT tag!");
        } else {
            entityData.removeTag(NBT_KEY_CHANNEL_DATA);
        }

        synchronized (joinedPlayers) {
            joinedPlayers.remove(player);
        }

        logger.info("Player {} left channel {}", player.getDisplayName(), channelId);

        player.addChatMessage(new ChatComponentText("\u00A7cYou left the \"\u00A74" + channelName + "\u00A7c\" channel!"));
    }

    void serverDisconnect(EntityPlayer player) {
        synchronized (joinedPlayers) {
            joinedPlayers.remove(player);
        }
    }

    static void reloadFormatting() {
        ABOEChat.getInstance().getConfiguration().load();
        ConfigCategory chatCategory = ABOEChat.getInstance().getConfiguration().getCategory(CATEGORY_CHAT_SETTINGS);
        if (!chatCategory.containsKey("format")) {
            chatFormat = "<prefix> &r<display_name>&r > <message>";
            chatCategory.put("format", new Property("format", chatFormat, Property.Type.STRING));
            ABOEChat.getInstance().getConfiguration().save();
        } else {
            chatFormat = chatCategory.get("format").getString();
        }
    }

    public List<EntityPlayer> getOnlinePlayers() {
        return joinedPlayers;
    }

    public boolean canJoin(EntityPlayer entityPlayer) {
        return chatMod.getPermissionProvider().hasPermission(entityPlayer, "aboechat.channels." + channelId);
    }

    private List<PlayerMention> getMentions(String unformatted) {
        List<PlayerMention> mentions = new ArrayList<>();

        int index = -1;

        do {
            index = unformatted.indexOf("@everyone", index + 1);
            if (index > 0) {
                mentions.add(new PlayerMention(index));
            }
        } while (index > 0);

        return mentions;
    }

    @Data
    private class PlayerMention {
        private final int index;
    }
}
