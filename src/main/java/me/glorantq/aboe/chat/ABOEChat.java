package me.glorantq.aboe.chat;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;
import lombok.Getter;
import me.glorantq.aboe.chat.client.channels.ClientChannelManager;
import me.glorantq.aboe.chat.client.channels.ClientChatChannel;
import me.glorantq.aboe.chat.client.chat.ChatGUIInjector;
import me.glorantq.aboe.chat.client.chat.ModifiedGuiNewChat;
import me.glorantq.aboe.chat.client.commands.emotes.EmoteHandler;
import me.glorantq.aboe.chat.common.*;
import me.glorantq.aboe.chat.server.api.RestAPI;
import me.glorantq.aboe.chat.server.channels.ChatChannelManager;
import me.glorantq.aboe.chat.server.commands.ChannelAdminCommand;
import me.glorantq.aboe.chat.server.commands.ChannelInfoCommand;
import me.glorantq.aboe.chat.server.commands.ReloadCommand;
import me.glorantq.aboe.chat.server.permissions.DefaultPermissionProvider;
import me.glorantq.aboe.chat.server.permissions.ForgeEssentialsPermissionProvider;
import me.glorantq.aboe.chat.server.permissions.PermissionProvider;
import net.minecraft.command.ServerCommandManager;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.config.Configuration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(modid = "aboe-chat", name = "ABOE Chat", version = ABOEChat.MOD_VERSION, canBeDeactivated = true, acceptableRemoteVersions = "*")
public class ABOEChat {
    static final String MOD_VERSION = "1.7.0";

    private static ABOEChat INSTANCE;
    public static ABOEChat getInstance() {
        return INSTANCE;
    }

    private final Logger logger = LogManager.getLogger("ABOE-Chat");

    private @Getter ChatGUIInjector chatGUIInjector = null;
    private @Getter EmoteHandler emoteHandler = null;
    private @Getter ChatChannelManager chatChannelManager = null;
    private @Getter ClientChannelManager clientChannelManager = null;
    private @Getter PermissionProvider permissionProvider = null;
    private @Getter RestAPI restAPI = null;

    private @Getter SimpleNetworkWrapper simpleNetworkWrapper = null;

    private @Getter Configuration configuration;

    @Mod.EventHandler
    public void onModPreInit(FMLPreInitializationEvent event) {
        INSTANCE = this;

        configuration = new Configuration(event.getSuggestedConfigurationFile());
        configuration.load();

        simpleNetworkWrapper = NetworkRegistry.INSTANCE.newSimpleChannel("ABOE-Chat");

        if(FMLCommonHandler.instance().getSide() == Side.CLIENT) {
            logger.info("Started on a client, instantiating ChatGUIInjector...");
            chatGUIInjector = new ChatGUIInjector();
            emoteHandler = new EmoteHandler();
            clientChannelManager = new ClientChannelManager();
        }

        if(FMLCommonHandler.instance().getSide() == Side.SERVER) {
            logger.info("Started on a server, instantiating ChatChannelManager...");
            chatChannelManager = new ChatChannelManager();
            restAPI = new RestAPI();
        }

        simpleNetworkWrapper.registerMessage(ClientChannelManager.PacketChannelListHandler.class, PacketChannelList.class, 0, Side.CLIENT);
        simpleNetworkWrapper.registerMessage(ClientChannelManager.PacketChangeChannelResponseHandler.class, PacketChangeChannelResponse.class, 1, Side.CLIENT);
        simpleNetworkWrapper.registerMessage(ClientChannelManager.PacketChannelChangedHandler.class, PacketChannelChanged.class, 2, Side.CLIENT);
        simpleNetworkWrapper.registerMessage(ChatChannelManager.PacketChangeChannelHandler.class, PacketChangeChannel.class, 3, Side.SERVER);
        simpleNetworkWrapper.registerMessage(ModifiedGuiNewChat.PacketUserMentionedHandler.class, PacketUserMentioned.class, 4, Side.CLIENT);
    }

    @Mod.EventHandler
    public void onModInit(FMLInitializationEvent event) {
        if(chatGUIInjector != null) {
            chatGUIInjector.initialise();
        }

        if(chatChannelManager != null) {
            if(Loader.isModLoaded("ForgeEssentials")) {
                permissionProvider = new ForgeEssentialsPermissionProvider();
                logger.info("Using ForgeEssentials for permissions!");
            } else {
                permissionProvider = new DefaultPermissionProvider();
                logger.info("Using the default permission provider!");
            }

            chatChannelManager.initialise();

            permissionProvider.registerPermission("aboechat.color", PermissionProvider.PermissionLevel.OP);
            permissionProvider.registerPermission("aboechat.mention.everyone", PermissionProvider.PermissionLevel.OP);
        }
    }

    @Mod.EventHandler
    public void onServerStarting(FMLServerStartingEvent event) {
        if(FMLCommonHandler.instance().getSide() != Side.SERVER) {
            return;
        }

        ServerCommandManager commandManager = (ServerCommandManager) MinecraftServer.getServer().getCommandManager();

        commandManager.registerCommand(new ChannelInfoCommand());
        commandManager.registerCommand(new ChannelAdminCommand());
        commandManager.registerCommand(new ReloadCommand());
    }
}
