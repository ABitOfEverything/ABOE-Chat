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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.stringtemplate.v4.ST;

import java.util.ArrayList;
import java.util.List;

public class ChatChannel {
    private static final String CATEGORY_CHAT_SETTINGS = "Chat";
    static final String NBT_KEY_CHANNEL_DATA = "current_chat_channel";

    private static String chatFormat = "&8<channel> &r\\<<display_name>\\> <message>";
    private static String joinNotificationFormat = "&2<display_name> &ahas joined the channel!";
    private static String leaveNotificationFormat = "&4<display_name> &chas left the channel!";

    private final ABOEChat chatMod = ABOEChat.getInstance();
    private final ChatChannelManager chatChannelManager = chatMod.getChatChannelManager();
    private final Logger logger;

    private final @Getter String channelId;
    private final @Getter String channelName;

    private final List<EntityPlayer> joinedPlayers = new ArrayList<>();

    ChatChannel(String id, String name) {
        this.channelId = id;
        this.channelName = name;

        this.logger = LogManager.getLogger("Channel-" + channelId);

        reloadFormatting();

        chatMod.getPermissionProvider().registerPermission("aboechat.channels." + channelId, PermissionProvider.PermissionLevel.OP);
    }

    void messageReceived(EntityPlayerMP player, String message) {
        ST messageTemplate = bindDefaultParameters(player, new ST(formatChatString(chatFormat)));
        messageTemplate.add("message", message);

        String renderedMessage = messageTemplate.render();

        if (chatMod.getPermissionProvider().hasPermission(player, "aboechat.color")) {
            renderedMessage = formatChatString(renderedMessage);
        }

        renderedMessage = renderedMessage.replaceAll(" +", " ").trim();

        if (renderedMessage.contains("@everyone") && !chatMod.getPermissionProvider().hasPermission(player, "aboechat.mention.everyone")) {
            List<PlayerMention> mentions = getMentions(renderedMessage);
            for (int i = 0; i < mentions.size(); i++) {
                String before = renderedMessage.substring(0, mentions.get(i).index + i + 1);
                String after = renderedMessage.substring(mentions.get(i).index + i + 1, renderedMessage.length());

                renderedMessage = before + "\ufeff" + after;
            }
        }

        ChatComponentText componentText = new ChatComponentText(renderedMessage);
        synchronized (joinedPlayers) {
            for (EntityPlayer otherPlayer : joinedPlayers) {
                otherPlayer.addChatMessage(componentText);
            }
        }

        logger.info("[CHAT] {}: {}", player.getGameProfile().getName(), message);
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

            if (channel.isPresent() && !channel.get().getChannelId().equalsIgnoreCase(channelId)) {
                channel.get().leave(player);
            }
        }

        entityData.setString(NBT_KEY_CHANNEL_DATA, channelId);

        logger.info("Player {} joined channel {}", player.getDisplayName(), channelId);

        ST joinTemplate = bindDefaultParameters(player, new ST(joinNotificationFormat));
        String renderedMessage = renderInternalMessage(joinTemplate);

        synchronized (joinedPlayers) {
            for(EntityPlayer onlinePlayer : joinedPlayers) {
                onlinePlayer.addChatMessage(new ChatComponentText(renderedMessage));
            }

            joinedPlayers.add(player);
        }

        player.addChatMessage(new ChatComponentText(formatChatString("&aYou are now talking in \"&2" + channelName + "&a\"!")));

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

        ST leaveTemplate = bindDefaultParameters(player, new ST(leaveNotificationFormat));
        String renderedMessage = renderInternalMessage(leaveTemplate);

        synchronized (joinedPlayers) {
            joinedPlayers.remove(player);

            for(EntityPlayer onlinePlayer : joinedPlayers) {
                onlinePlayer.addChatMessage(new ChatComponentText(renderedMessage));
            }
        }

        logger.info("Player {} left channel {}", player.getDisplayName(), channelId);

        player.addChatMessage(new ChatComponentText(formatChatString("&cYou left the \"&4" + channelName + "&c\" channel!")));
    }

    void deathOrDisconnect(EntityPlayer player, boolean disconnect) {
        synchronized (joinedPlayers) {
            joinedPlayers.remove(player);

            if(disconnect) {
                ST leaveTemplate = bindDefaultParameters(player, new ST(leaveNotificationFormat));
                String renderedMessage = renderInternalMessage(leaveTemplate);

                synchronized (joinedPlayers) {
                    joinedPlayers.remove(player);

                    for(EntityPlayer onlinePlayer : joinedPlayers) {
                        onlinePlayer.addChatMessage(new ChatComponentText(renderedMessage));
                    }
                }
            }
        }
    }

    void onPlayerRespawn(EntityPlayer player) {
        synchronized (joinedPlayers) {
            if (joinedPlayers.contains(player)) {
                return;
            }

            NBTTagCompound entityData = player.getEntityData();
            entityData.setString(NBT_KEY_CHANNEL_DATA, channelId);

            joinedPlayers.add(player);
        }
    }

    static void reloadFormatting() {
        chatFormat = getKeyOrSetDefault("format", chatFormat, Property.Type.STRING);
        joinNotificationFormat = getKeyOrSetDefault("joinNotification", joinNotificationFormat, Property.Type.STRING);
        leaveNotificationFormat = getKeyOrSetDefault("leaveNotification", leaveNotificationFormat, Property.Type.STRING);
    }

    @SuppressWarnings("unchecked")
    private static <T> T getKeyOrSetDefault(String key, T defaultValue, Property.Type propertyType) {
        ABOEChat.getInstance().getConfiguration().load();
        ConfigCategory chatCategory = ABOEChat.getInstance().getConfiguration().getCategory(CATEGORY_CHAT_SETTINGS);

        if (!chatCategory.containsKey(key)) {
            chatCategory.put(key, new Property(key, defaultValue.toString(), propertyType));
            ABOEChat.getInstance().getConfiguration().save();

            return defaultValue;
        }

        Property property = chatCategory.get(key);
        switch (propertyType) {
            case DOUBLE:
                return (T) (Double) property.getDouble();
            case STRING:
                return (T) property.getString();
            case BOOLEAN:
                return (T) (Boolean) property.getBoolean();
            case INTEGER:
                return (T) (Integer) property.getInt();
            default:
                return defaultValue;
        }
    }

    private ST bindDefaultParameters(EntityPlayer player, ST template) {
        template.add("username", player.getGameProfile().getName());
        template.add("display_name", formatChatString(chatMod.getPermissionProvider().getDisplayName(player)));
        template.add("group", chatMod.getPermissionProvider().getGroup(player));
        template.add("channel_name", channelName);
        template.add("channel_id", channelId);
        template.add("prefix", formatChatString(chatMod.getPermissionProvider().getPrefix(player)));
        template.add("suffix", formatChatString(chatMod.getPermissionProvider().getSuffix(player)));

        return template;
    }

    private String renderInternalMessage(ST template) {
        String message = template.render();
        message = formatChatString(message);
        return message.replaceAll(" +", " ").trim();
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

    private String formatChatString(String message) {
        return message.replaceAll("&", "\u00A7");
    }

    @Data
    private class PlayerMention {
        private final int index;
    }
}
