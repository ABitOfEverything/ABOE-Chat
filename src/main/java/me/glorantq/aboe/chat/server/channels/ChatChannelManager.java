package me.glorantq.aboe.chat.server.channels;

import com.google.common.base.Optional;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import lombok.Getter;
import me.glorantq.aboe.chat.ABOEChat;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.event.ServerChatEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatChannelManager {
    public static final String CATEGORY_CHANNELS = "Channels";

    private Logger logger = LogManager.getLogger("ChatChannelManager");
    private Configuration config = ABOEChat.getInstance().getConfiguration();

    private ChatChannel defaultChannel;

    public ChatChannelManager() {
        channels = new HashMap<>();
    }

    private final @Getter
    Map<String, ChatChannel> channels;

    public void initialise() {
        FMLCommonHandler.instance().bus().register(this);
        MinecraftForge.EVENT_BUS.register(this);

        defaultChannel = new DefaultChatChannel();
        channels.put("general", defaultChannel);

        ConfigCategory channelsCategory = config.getCategory(CATEGORY_CHANNELS);

        String[] availableChannels = new String[0];
        if (channelsCategory.containsKey("available_channels")) {
            availableChannels = channelsCategory.get("available_channels").getStringList();
        } else {
            channelsCategory.put("available_channels", new Property("available_channels", new String[]{"staff:Staff"}, Property.Type.STRING));
            config.save();
        }

        for (String channelName : availableChannels) {
            String[] parts = channelName.split(":");
            if (parts.length != 2) {
                continue;
            }

            String channelId = parts[0];
            String channelName0 = parts[1];

            if (channels.containsKey(channelId)) {
                logger.error("Channel with ID {} already exists!", channelId);
                continue;
            }

            ChatChannel chatChannel = new ChatChannel(channelId, channelName0);

            channels.put(channelId, chatChannel);
            logger.info("Loaded channel: {}", channelId);
        }
    }

    @SubscribeEvent
    public void onPlayerJoinServer(PlayerEvent.PlayerLoggedInEvent event) {
        NBTTagCompound entityData = event.player.getEntityData();

        if (entityData.hasKey(ChatChannel.NBT_KEY_CHANNEL_DATA)) {
            String channelId = entityData.getString(ChatChannel.NBT_KEY_CHANNEL_DATA);
            Optional<ChatChannel> chatChannel = getChannel(channelId);

            if (chatChannel.isPresent()) {
                boolean joined = chatChannel.get().join(event.player);
                if (!joined) {
                    defaultChannel.join(event.player);
                    event.player.addChatMessage(new ChatComponentText("\u00A74[\u00A7cABOE-Chat\u00A74]\u00A7c We were unable to connect you to the \"\u00A74" + chatChannel.get().getChannelName() + "\u00A7c\" channel. You're now talking in \"\u00A74" + defaultChannel.getChannelName() + "\u00A7c\"."));
                }
            } else {
                defaultChannel.join(event.player);
            }
        } else {
            defaultChannel.join(event.player);
        }
    }

    @SubscribeEvent
    public void onPlayerDisconnectServer(PlayerEvent.PlayerLoggedOutEvent event) {
        Optional<ChatChannel> playerChannel = getChannelForPlayer(event.player);
        if (playerChannel.isPresent()) {
            playerChannel.get().serverDisconnect(event.player);
        }
    }

    @SubscribeEvent
    public void onPlayerChat(ServerChatEvent event) {
        event.setCanceled(true);

        Optional<ChatChannel> playerChannel = getChannelForPlayer(event.player);

        if (!playerChannel.isPresent()) {
            event.player.addChatMessage(new ChatComponentText("\u00A7cYou aren't connected to a channel! Join one using \u00A74/channel join <channel>\u00A7c!"));
            return;
        }

        playerChannel.get().messageReceived(event.player, event.message);
    }

    public void reload() {
        ChatChannel.reloadFormatting();
    }

    public Optional<ChatChannel> createChannel(String id, String name) {
        if (channels.containsKey(id)) {
            return Optional.absent();
        }

        ChatChannel chatChannel = new ChatChannel(id, name);
        channels.put(id, chatChannel);

        saveChannels();

        return Optional.of(chatChannel);
    }

    public boolean deleteChannel(String id) {
        if (!channels.containsKey(id)) {
            return false;
        }

        ChatChannel chatChannel;
        boolean successful;

        synchronized (channels) {
            chatChannel = channels.remove(id);

            if(chatChannel != null && chatChannel instanceof DefaultChatChannel) {
                successful = false;
                channels.put("general", chatChannel);
            } else {
                successful = chatChannel != null;
            }
        }

        if (successful) {
            List<EntityPlayer> players = new ArrayList<>(chatChannel.getOnlinePlayers());
            for (EntityPlayer player : players) {
                chatChannel.leave(player);
                player.addChatMessage(new ChatComponentText("\u00A7cThe channel you were in got deleted."));
            }
        }

        saveChannels();

        return successful;
    }

    private void saveChannels() {
        List<ChatChannel> values = new ArrayList<>(channels.values());
        List<String> final0 = new ArrayList<>();

        for (ChatChannel chatChannel : values) {
            final0.add(chatChannel.getChannelId() + ":" + chatChannel.getChannelName());
        }

        ConfigCategory channelsCategory = config.getCategory(CATEGORY_CHANNELS);
        channelsCategory.put("available_channels", new Property("available_channels", final0.toArray(new String[0]), Property.Type.STRING));
        config.save();

        logger.info("Saved channels!");
    }

    public Optional<ChatChannel> getChannelForPlayer(EntityPlayer player) {
        NBTTagCompound entityData = player.getEntityData();

        if (!entityData.hasKey(ChatChannel.NBT_KEY_CHANNEL_DATA)) {
            return Optional.absent();
        }

        return getChannel(entityData.getString(ChatChannel.NBT_KEY_CHANNEL_DATA));
    }

    public Optional<ChatChannel> getChannel(String id) {
        return Optional.fromNullable(channels.get(id));
    }

    public ChatChannel getDefaultChannel() {
        return defaultChannel;
    }
}
