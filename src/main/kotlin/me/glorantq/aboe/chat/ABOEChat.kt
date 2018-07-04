package me.glorantq.aboe.chat

import cpw.mods.fml.common.FMLCommonHandler
import cpw.mods.fml.common.Loader
import cpw.mods.fml.common.Mod
import cpw.mods.fml.common.event.FMLInitializationEvent
import cpw.mods.fml.common.event.FMLPreInitializationEvent
import cpw.mods.fml.common.event.FMLServerStartingEvent
import cpw.mods.fml.common.network.NetworkRegistry
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper
import cpw.mods.fml.relauncher.Side
import me.glorantq.aboe.chat.client.backport.PlayerProfileManager
import me.glorantq.aboe.chat.client.channels.ClientChannelManager
import me.glorantq.aboe.chat.client.chat.ChatGUIInjector
import me.glorantq.aboe.chat.client.chat.ModifiedGuiNewChat
import me.glorantq.aboe.chat.client.commands.MentionLevelCommand
import me.glorantq.aboe.chat.client.commands.NotificationsCommand
import me.glorantq.aboe.chat.client.commands.emotes.EmoteHandler
import me.glorantq.aboe.chat.common.*
import me.glorantq.aboe.chat.server.channels.ChatChannelManager
import me.glorantq.aboe.chat.server.commands.ChannelAdminCommand
import me.glorantq.aboe.chat.server.commands.ChannelInfoCommand
import me.glorantq.aboe.chat.server.commands.ReloadCommand
import me.glorantq.aboe.chat.server.permissions.DefaultPermissionProvider
import me.glorantq.aboe.chat.server.permissions.ForgeEssentialsPermissionProvider
import me.glorantq.aboe.chat.server.permissions.IPermissionProvider
import net.minecraft.command.ServerCommandManager
import net.minecraft.server.MinecraftServer
import net.minecraftforge.client.ClientCommandHandler
import net.minecraftforge.common.config.Configuration
import org.apache.logging.log4j.LogManager

const val modID: String = "aboe-chat"

@Mod(modid = modID, name = "ABOE Chat", version = ABOEChat.MOD_VERSION, canBeDeactivated = true)
class ABOEChat {
    companion object {
        const val MOD_VERSION = "2.0.0"

        lateinit var instance: ABOEChat
            private set
    }

    private val logger = LogManager.getLogger("ABOE-Chat")

    var chatGUIInjector: ChatGUIInjector? = null
        private set

    private var emoteHandler: EmoteHandler? = null

    var chatChannelManager: ChatChannelManager? = null
        private set

    var clientChannelManager: ClientChannelManager? = null
        private set

    var permissionProvider: IPermissionProvider? = null
        private set

    var simpleNetworkWrapper: SimpleNetworkWrapper? = null
        private set

    var playerSkinManager: PlayerProfileManager? = null
        private set

    lateinit var configuration: Configuration
        private set

    @Mod.EventHandler
    fun onModPreInit(event: FMLPreInitializationEvent) {
        instance = this

        configuration = Configuration(event.suggestedConfigurationFile)
        configuration.load()

        simpleNetworkWrapper = NetworkRegistry.INSTANCE.newSimpleChannel("ABOE-Chat")

        if (FMLCommonHandler.instance().side == Side.CLIENT) {
            logger.info("Started on a client!")
            chatGUIInjector = ChatGUIInjector()
            emoteHandler = EmoteHandler()
            clientChannelManager = ClientChannelManager()
            playerSkinManager = PlayerProfileManager()

            ClientCommandHandler.instance.registerCommand(MentionLevelCommand())
            ClientCommandHandler.instance.registerCommand(NotificationsCommand())
        }

        if (FMLCommonHandler.instance().side == Side.SERVER) {
            logger.info("Started on a server!")
            chatChannelManager = ChatChannelManager()
        }

        PacketChannelList::class.register(simpleNetworkWrapper!!, ClientChannelManager.PacketChannelListHandler::class, 0, Side.CLIENT)
        PacketChangeChannelResponse::class.register(simpleNetworkWrapper!!, ClientChannelManager.PacketChangeChannelResponseHandler::class, 1, Side.CLIENT)
        PacketChannelChanged::class.register(simpleNetworkWrapper!!, ClientChannelManager.PacketChannelChangedHandler::class, 2, Side.CLIENT)
        PacketChangeChannel::class.register(simpleNetworkWrapper!!, ChatChannelManager.PacketChangeChannelHandler::class, 3, Side.SERVER)
        PacketUserMentioned::class.register(simpleNetworkWrapper!!, ModifiedGuiNewChat.PacketUserMentionedHandler::class, 4, Side.CLIENT)
    }

    @Mod.EventHandler
    fun onModInit(event: FMLInitializationEvent) {
        if (chatGUIInjector != null) {
            chatGUIInjector!!.initialise()
        }

        if (chatChannelManager != null) {
            if (Loader.isModLoaded("ForgeEssentials")) {
                permissionProvider = ForgeEssentialsPermissionProvider()
                logger.info("Using ForgeEssentials for permissions!")
            } else {
                permissionProvider = DefaultPermissionProvider()
                logger.info("Using the default permission provider!")
            }

            chatChannelManager!!.initialise()

            permissionProvider!!.registerPermission("aboechat.color", IPermissionProvider.PermissionLevel.OP)
            permissionProvider!!.registerPermission("aboechat.mention.everyone", IPermissionProvider.PermissionLevel.OP)
        }
    }

    @Mod.EventHandler
    fun onServerStarting(event: FMLServerStartingEvent) {
        if (FMLCommonHandler.instance().side != Side.SERVER) {
            return
        }

        val commandManager = MinecraftServer.getServer().commandManager as ServerCommandManager

        commandManager.registerCommand(ChannelInfoCommand())
        commandManager.registerCommand(ChannelAdminCommand())
        commandManager.registerCommand(ReloadCommand())
    }
}
