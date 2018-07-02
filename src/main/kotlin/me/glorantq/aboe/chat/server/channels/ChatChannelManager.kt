package me.glorantq.aboe.chat.server.channels

import com.google.common.base.Optional
import cpw.mods.fml.common.FMLCommonHandler
import cpw.mods.fml.common.eventhandler.SubscribeEvent
import cpw.mods.fml.common.gameevent.PlayerEvent
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler
import cpw.mods.fml.common.network.simpleimpl.MessageContext
import me.glorantq.aboe.chat.ABOEChat
import me.glorantq.aboe.chat.ClientChannelList
import me.glorantq.aboe.chat.ServerChannelMap
import me.glorantq.aboe.chat.client.channels.ClientChatChannel
import me.glorantq.aboe.chat.common.PacketChangeChannel
import me.glorantq.aboe.chat.common.PacketChangeChannelResponse
import me.glorantq.aboe.chat.common.PacketChannelList
import me.glorantq.aboe.chat.convertColours
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.ChatComponentText
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.common.config.ConfigCategory
import net.minecraftforge.common.config.Property
import net.minecraftforge.event.ServerChatEvent
import org.apache.logging.log4j.LogManager

class ChatChannelManager {
    companion object {
        const val CATEGORY_CHANNELS = "Channels"
    }

    private val logger = LogManager.getLogger("ChatChannelManager")
    private val config = ABOEChat.instance.configuration

    private lateinit var defaultChannel: ChatChannel

    val channels: ServerChannelMap = hashMapOf()

    fun initialise() {
        FMLCommonHandler.instance().bus().register(this)
        MinecraftForge.EVENT_BUS.register(this)

        defaultChannel = DefaultChatChannel()
        channels["general"] = defaultChannel

        val channelsCategory: ConfigCategory = config.getCategory(CATEGORY_CHANNELS)

        var availableChannels: Array<String> = emptyArray()
        if (channelsCategory.containsKey("available_channels")) {
            availableChannels = channelsCategory.get("available_channels").stringList
        } else {
            channelsCategory["available_channels"] = Property("available_channels", arrayOf("staff:Staff"), Property.Type.STRING)
            config.save()
        }

        for (channelName in availableChannels) {
            val parts: Array<String> = channelName.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            if (parts.size != 2) {
                continue
            }

            val channelId: String = parts[0]
            val channelName0: String = parts[1]

            if (channels.containsKey(channelId)) {
                logger.error("Channel with ID {} already exists!", channelId)
                continue
            }

            val chatChannel: ChatChannel = ChatChannel(channelId, channelName0)

            channels[channelId] = chatChannel
            logger.info("Loaded channel: {}", channelId)
        }
    }

    @SubscribeEvent
    fun onPlayerJoinServer(event: PlayerEvent.PlayerLoggedInEvent) {
        val channelList: PacketChannelList = PacketChannelList(getClientChannels(event.player as EntityPlayerMP))
        ABOEChat.instance.simpleNetworkWrapper!!.sendTo(channelList, event.player as EntityPlayerMP)

        val entityData: NBTTagCompound = event.player.entityData

        if (entityData.hasKey(ChatChannel.NBT_KEY_CHANNEL_DATA)) {
            val channelId: String = entityData.getString(ChatChannel.NBT_KEY_CHANNEL_DATA)
            val chatChannel: Optional<ChatChannel> = getChannel(channelId)

            if (chatChannel.isPresent) {
                val joined: Boolean = chatChannel.get().join(event.player)
                if (!joined) {
                    defaultChannel.join(event.player)
                    event.player.addChatMessage(ChatComponentText("&4[&cABOE-Chat&4]&c We were unable to connect you to the \"&4${chatChannel.get().channelName}&c\" channel. You're now talking in \"&4${defaultChannel.channelName}&c\".".convertColours()))
                }
            } else {
                defaultChannel.join(event.player)
            }
        } else {
            defaultChannel.join(event.player)
        }
    }

    @SubscribeEvent
    fun onPlayerDisconnectServer(event: PlayerEvent.PlayerLoggedOutEvent) {
        val playerChannel: Optional<ChatChannel> = getChannelForPlayer(event.player)
        if (playerChannel.isPresent) {
            playerChannel.get().deathOrDisconnect(event.player, true)
        }
    }

    @SubscribeEvent
    fun onPlayerChat(event: ServerChatEvent) {
        event.isCanceled = true

        val playerChannel: Optional<ChatChannel> = getChannelForPlayer(event.player)

        if (!playerChannel.isPresent) {
            event.player.addChatMessage(ChatComponentText("\u00A7cYou aren't connected to a channel! Join one using \u00A74/channel join <channel>\u00A7c!"))
            return
        }

        playerChannel.get().messageReceived(event.player, event.message)
    }

    @SubscribeEvent
    fun onEntityPlayerClone(event: net.minecraftforge.event.entity.player.PlayerEvent.Clone) {
        val original: EntityPlayer = event.original
        val newPlayer: EntityPlayer = event.entityPlayer

        val oldChannel: Optional<ChatChannel> = getChannelForPlayer(original)
        if (oldChannel.isPresent) {
            oldChannel.get().deathOrDisconnect(original, false)
            oldChannel.get().onPlayerRespawn(newPlayer)
        }
    }

    fun reload() {
        ChatChannel.reloadFormatting()
    }

    fun createChannel(id: String, name: String): Optional<ChatChannel> {
        if (channels.containsKey(id)) {
            return Optional.absent()
        }

        val chatChannel: ChatChannel = ChatChannel(id, name)
        channels[id] = chatChannel

        saveChannels()

        return Optional.of(chatChannel)
    }

    fun deleteChannel(id: String): Boolean {
        if (!channels.containsKey(id)) {
            return false
        }

        var chatChannel: ChatChannel? = null
        var successful: Boolean = false

        synchronized(channels) {
            chatChannel = channels.remove(id)

            if (chatChannel is DefaultChatChannel) {
                successful = false
                channels.put("general", chatChannel as ChatChannel)
            } else {
                successful = chatChannel != null
            }
        }

        if (successful) {
            val players: ArrayList<EntityPlayer> = ArrayList(chatChannel!!.onlinePlayers)
            for (player in players) {
                chatChannel!!.leave(player)
                player.addChatMessage(ChatComponentText("\u00A7cThe channel you were in got deleted."))
            }
        }

        saveChannels()

        return successful
    }

    private fun saveChannels() {
        val values: ArrayList<ChatChannel> = ArrayList(channels.values)
        val final0: ArrayList<String> = arrayListOf()

        for (chatChannel in values) {
            final0.add(chatChannel.channelId + ":" + chatChannel.channelName)
        }

        val channelsCategory: ConfigCategory = config.getCategory(CATEGORY_CHANNELS)
        channelsCategory["available_channels"] = Property("available_channels", final0.toTypedArray(), Property.Type.STRING)
        config.save()

        logger.info("Saved channels!")
    }

    fun getChannelForPlayer(player: EntityPlayer): Optional<ChatChannel> {
        val entityData: NBTTagCompound = player.entityData

        return if (!entityData.hasKey(ChatChannel.NBT_KEY_CHANNEL_DATA)) {
            Optional.absent()
        } else {
            getChannel(entityData.getString(ChatChannel.NBT_KEY_CHANNEL_DATA))
        }
    }

    fun getChannel(id: String): Optional<ChatChannel> = Optional.fromNullable(channels[id])

    class PacketChangeChannelHandler : IMessageHandler<PacketChangeChannel, PacketChangeChannelResponse> {
        private val channelManager: ChatChannelManager? = ABOEChat.instance.chatChannelManager

        override fun onMessage(message: PacketChangeChannel, ctx: MessageContext): PacketChangeChannelResponse {
            val channelOptional: Optional<ChatChannel> = channelManager!!.getChannel(message.newChannel)
            if (!channelOptional.isPresent) {
                return PacketChangeChannelResponse(false, "Invalid channel", channelManager.getClientChannels(ctx.serverHandler.playerEntity))
            }

            val chatChannel: ChatChannel = channelOptional.get()

            val joined: Boolean = chatChannel.join(ctx.serverHandler.playerEntity)
            return if (!joined) {
                PacketChangeChannelResponse(false, "Failed to join", channelManager.getClientChannels(ctx.serverHandler.playerEntity))
            } else PacketChangeChannelResponse(true, "Changed", channelManager.getClientChannels(ctx.serverHandler.playerEntity))

        }
    }

    internal fun getClientChannels(sender: EntityPlayerMP): MutableList<ClientChatChannel> {
        val chatChannels: ClientChannelList =  ArrayList()
        synchronized(channels) {
            for ((_, channel) in channels) {
                chatChannels.add(ClientChatChannel(channel.channelId, channel.onlinePlayers.size, channel.canJoin(sender)))
            }
        }

        return chatChannels
    }
}
