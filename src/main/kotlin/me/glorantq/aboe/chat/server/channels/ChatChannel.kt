package me.glorantq.aboe.chat.server.channels

import com.google.common.base.Optional
import me.glorantq.aboe.chat.ABOEChat
import me.glorantq.aboe.chat.common.PacketChannelChanged
import me.glorantq.aboe.chat.common.PacketChannelList
import me.glorantq.aboe.chat.common.PacketUserMentioned
import me.glorantq.aboe.chat.getKeyOrSetDefault
import me.glorantq.aboe.chat.server.permissions.IPermissionProvider
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.ChatComponentText
import net.minecraft.util.IChatComponent
import net.minecraftforge.common.config.Configuration
import net.minecraftforge.common.config.Property
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.stringtemplate.v4.ST
import java.util.regex.Matcher
import java.util.regex.Pattern

open class ChatChannel internal constructor(val channelId: String, val channelName: String) {
    companion object {
        private const val CATEGORY_CHAT_SETTINGS: String = "Chat"
        internal const val NBT_KEY_CHANNEL_DATA: String = "current_chat_channel"

        private var chatFormat: String = "&8<channel> &r<<display_name>\\> <message>"
        private var joinNotificationFormat: String = "&2<display_name> &ahas joined the channel!"
        private var leaveNotificationFormat: String = "&4<display_name> &chas left the channel!"

        internal fun reloadFormatting() {
            val config: Configuration = ABOEChat.instance.configuration
            chatFormat = config.getKeyOrSetDefault(CATEGORY_CHAT_SETTINGS, "format", chatFormat, Property.Type.STRING)
            joinNotificationFormat = config.getKeyOrSetDefault(CATEGORY_CHAT_SETTINGS, "joinNotification", joinNotificationFormat, Property.Type.STRING)
            leaveNotificationFormat = config.getKeyOrSetDefault(CATEGORY_CHAT_SETTINGS, "leaveNotification", leaveNotificationFormat, Property.Type.STRING)
        }
    }

    private val chatMod: ABOEChat = ABOEChat.instance
    private val chatChannelManager: ChatChannelManager = chatMod.chatChannelManager!!
    private val logger: Logger = LogManager.getLogger("Channel-$channelId")

    private val joinedPlayers: ArrayList<EntityPlayer> = ArrayList()

    private val colourCodeCleanPattern: Pattern = Pattern.compile("(?<code1>(?>\u00a7[0-9a-fk-or])+) +(?<code2>(?>\u00a7[0-9a-fk-or])+) *")

    val onlinePlayers: List<EntityPlayer>
        get() = joinedPlayers

    init {
        reloadFormatting()

        chatMod.permissionProvider!!.registerPermission("aboechat.channels.$channelId", IPermissionProvider.PermissionLevel.OP)
    }

    internal fun messageReceived(player: EntityPlayerMP, message: String) {
        val messageTemplate: ST = bindDefaultParameters(player, ST(formatChatString(chatFormat)))
        messageTemplate.add("message", message)

        var renderedMessage: String = messageTemplate.render()

        if (chatMod.permissionProvider!!.hasPermission(player, "aboechat.color")) {
            renderedMessage = formatChatString(renderedMessage)
        }

        renderedMessage = renderedMessage.replace(" +".toRegex(), " ").trim { it <= ' ' }
        var matcher: Matcher = colourCodeCleanPattern.matcher(renderedMessage)

        cleaner@ do {
            if (!matcher.find()) {
                return@cleaner
            }

            val code1: String = matcher.group("code1")
            val code2: String = matcher.group("code2")

            val full: String = matcher.group()

            renderedMessage = renderedMessage.replace(full.toRegex(), "$code1$code2")
            matcher = colourCodeCleanPattern.matcher(renderedMessage)
        } while (matcher.find())

        if (renderedMessage.contains("@everyone") && !chatMod.permissionProvider!!.hasPermission(player, "aboechat.mention.everyone")) {
            val mentions: List<PlayerMention> = getMentions(renderedMessage, "everyone")
            for (i in mentions.indices) {
                val before: String = renderedMessage.substring(0, mentions[i].index + i + 1)
                val after: String = renderedMessage.substring(mentions[i].index + i + 1, renderedMessage.length)

                renderedMessage = "$before\ufeff$after"
            }
        } else if (renderedMessage.contains("@everyone")) {
            synchronized(joinedPlayers) {
                for (entityPlayer in joinedPlayers) {
                    val packetUserMentioned: PacketUserMentioned = PacketUserMentioned(player.gameProfile.name, message, true)
                    chatMod.simpleNetworkWrapper!!.sendTo(packetUserMentioned, entityPlayer as EntityPlayerMP)
                }
            }
        }

        synchronized(joinedPlayers) {
            for (entityPlayer in joinedPlayers) {
                val mentions: List<PlayerMention> = getMentions(renderedMessage, entityPlayer.gameProfile.name)
                if (mentions.isNotEmpty()) {
                    val packetUserMentioned: PacketUserMentioned = PacketUserMentioned(player.gameProfile.name, message, false)
                    chatMod.simpleNetworkWrapper!!.sendTo(packetUserMentioned, entityPlayer as EntityPlayerMP)
                }
            }
        }

        val componentText: IChatComponent = ChatComponentText(renderedMessage)
        synchronized(joinedPlayers) {
            for (otherPlayer in joinedPlayers) {
                otherPlayer.addChatMessage(componentText)
            }
        }

        logger.info("[CHAT] {}: {}", player.gameProfile.name, message.replace(Regex("&[0-9a-fk-or]"), ""))
    }

    fun join(player: EntityPlayer): Boolean = if (!canJoin(player)) { false } else forceJoin(player)

    fun forceJoin(player: EntityPlayer): Boolean {
        val entityData: NBTTagCompound = player.entityData
        if (entityData.hasKey(NBT_KEY_CHANNEL_DATA)) {
            val currentChannel: String = entityData.getString(NBT_KEY_CHANNEL_DATA)
            val channel: Optional<ChatChannel> = chatChannelManager.getChannel(currentChannel)

            if (channel.isPresent && !channel.get().channelId.equals(channelId, ignoreCase = true)) {
                channel.get().leave(player)
            }
        }

        entityData.setString(NBT_KEY_CHANNEL_DATA, channelId)

        logger.info("Player {} joined channel {}", player.displayName, channelId)

        val joinTemplate: ST = bindDefaultParameters(player, ST(joinNotificationFormat))
        val renderedMessage: String = renderInternalMessage(joinTemplate)

        synchronized(joinedPlayers) {
            for (onlinePlayer in joinedPlayers) {
                onlinePlayer.addChatMessage(ChatComponentText(renderedMessage))
            }

            joinedPlayers.add(player)
            val channelChanged: PacketChannelChanged = PacketChannelChanged(channelId)
            chatMod.simpleNetworkWrapper!!.sendTo(channelChanged, player as EntityPlayerMP)

            for (player0 in joinedPlayers) {
                val channelList: PacketChannelList = PacketChannelList(chatChannelManager.getClientChannels(player0 as EntityPlayerMP))
                chatMod.simpleNetworkWrapper!!.sendTo(channelList, player0)
            }
        }

        player.addChatMessage(ChatComponentText(formatChatString("&aYou are now talking in \"&2$channelName&a\"!")))

        return true
    }

    fun leave(player: EntityPlayer) {
        if (!joinedPlayers.contains(player)) {
            logger.warn("Player {} wasn't in the channel they're leaving!", player.displayName)
            return
        }

        val entityData: NBTTagCompound = player.entityData
        if (!entityData.hasKey(NBT_KEY_CHANNEL_DATA)) {
            logger.error("Player is leaving the channel, but they don't have an NBT tag!")
        } else {
            entityData.removeTag(NBT_KEY_CHANNEL_DATA)
        }

        val leaveTemplate: ST = bindDefaultParameters(player, ST(leaveNotificationFormat))
        val renderedMessage: String = renderInternalMessage(leaveTemplate)

        synchronized(joinedPlayers) {
            joinedPlayers.remove(player)

            for (onlinePlayer in joinedPlayers) {
                onlinePlayer.addChatMessage(ChatComponentText(renderedMessage))

                val channelList = PacketChannelList(chatChannelManager.getClientChannels(onlinePlayer as EntityPlayerMP))
                chatMod.simpleNetworkWrapper!!.sendTo(channelList, onlinePlayer)
            }

            val channelChanged: PacketChannelChanged = PacketChannelChanged("None")
            val channelList: PacketChannelList = PacketChannelList(chatChannelManager.getClientChannels(player as EntityPlayerMP))
            chatMod.simpleNetworkWrapper!!.sendTo(channelChanged, player)
            chatMod.simpleNetworkWrapper!!.sendTo(channelList, player)
        }

        logger.info("Player {} left channel {}", player.displayName, channelId)

        player.addChatMessage(ChatComponentText(formatChatString("&cYou left the \"&4$channelName&c\" channel!")))
    }

    internal fun deathOrDisconnect(player: EntityPlayer, disconnect: Boolean) {
        synchronized(joinedPlayers) {
            joinedPlayers.remove(player)

            if (disconnect) {
                val leaveTemplate: ST = bindDefaultParameters(player, ST(leaveNotificationFormat))
                val renderedMessage: String = renderInternalMessage(leaveTemplate)

                synchronized(joinedPlayers) {
                    joinedPlayers.remove(player)

                    for (onlinePlayer in joinedPlayers) {
                        onlinePlayer.addChatMessage(ChatComponentText(renderedMessage))
                    }
                }
            }
        }
    }

    internal fun onPlayerRespawn(player: EntityPlayer) {
        synchronized(joinedPlayers) {
            if (joinedPlayers.contains(player)) {
                return
            }

            val entityData: NBTTagCompound = player.entityData
            entityData.setString(NBT_KEY_CHANNEL_DATA, channelId)

            joinedPlayers.add(player)
        }
    }

    private fun bindDefaultParameters(player: EntityPlayer, template: ST): ST {
        template.add("username", player.gameProfile.name)
        template.add("display_name", formatChatString(chatMod.permissionProvider!!.getDisplayName(player)))
        template.add("group", chatMod.permissionProvider!!.getGroup(player))
        template.add("channel_name", channelName)
        template.add("channel_id", channelId)
        template.add("prefix", formatChatString(chatMod.permissionProvider!!.getPrefix(player)))
        template.add("suffix", formatChatString(chatMod.permissionProvider!!.getSuffix(player)))

        return template
    }

    private fun renderInternalMessage(template: ST): String {
        var message: String = template.render()
        message = formatChatString(message)
        return message.replace(" +".toRegex(), " ").trim { it <= ' ' }
    }


    private fun getMentions(unformatted: String, playerName: String): List<PlayerMention> {
        val mentions: ArrayList<PlayerMention> = ArrayList()

        var index: Int = -1

        do {
            index = unformatted.indexOf("@$playerName", index + 1)
            if (index > 0) {
                mentions.add(PlayerMention(index))
            }
        } while (index > 0)

        return mentions
    }

    open fun canJoin(entityPlayer: EntityPlayer): Boolean = chatMod.permissionProvider!!.hasPermission(entityPlayer, "aboechat.channels.$channelId")
    private fun formatChatString(message: String): String = message.replace("&".toRegex(), "\u00A7")
    private inner class PlayerMention(val index: Int)
}
