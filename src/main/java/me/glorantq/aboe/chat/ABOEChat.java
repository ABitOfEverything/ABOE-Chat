package me.glorantq.aboe.chat;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.relauncher.Side;
import lombok.Getter;
import me.glorantq.aboe.chat.client.ChatGUIInjector;
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
import net.minecraftforge.permission.PermissionLevel;
import net.minecraftforge.permission.PermissionManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(modid = "aboe-chat", name = "ABOE Chat", version = ABOEChat.MOD_VERSION, canBeDeactivated = true, acceptableRemoteVersions = "*")
public class ABOEChat {
    static final String MOD_VERSION = "1.6.1";

    private static ABOEChat INSTANCE;
    public static ABOEChat getInstance() {
        return INSTANCE;
    }

    private final Logger logger = LogManager.getLogger("ABOE-Chat");

    private @Getter ChatGUIInjector chatGUIInjector = null;
    private @Getter ChatChannelManager chatChannelManager = null;
    private @Getter PermissionProvider permissionProvider;

    private @Getter Configuration configuration;

    @Mod.EventHandler
    public void onModPreInit(FMLPreInitializationEvent event) {
        INSTANCE = this;

        configuration = new Configuration(event.getSuggestedConfigurationFile());
        configuration.load();

        if(FMLCommonHandler.instance().getSide() == Side.CLIENT) {
            logger.info("Started on a client, instantiating ChatGUIInjector...");
            chatGUIInjector = new ChatGUIInjector();
        }

        if(FMLCommonHandler.instance().getSide() == Side.SERVER) {
            logger.info("Started on a server, instantiating ChatChannelManager...");
            chatChannelManager = new ChatChannelManager();
        }
    }

    @Mod.EventHandler
    public void onModInit(FMLInitializationEvent event) {
        if(chatGUIInjector != null) {
            chatGUIInjector.initialise();
        }

        if(chatChannelManager != null) {
            chatChannelManager.initialise();

            if(Loader.isModLoaded("ForgeEssentials")) {
                permissionProvider = new ForgeEssentialsPermissionProvider();
                logger.info("Using ForgeEssentials for permissions!");
            } else {
                permissionProvider = new DefaultPermissionProvider();
                logger.info("Using the default permission provider!");
            }

            PermissionManager.registerPermission("aboechat.color", PermissionLevel.OP);
            PermissionManager.registerPermission("aboechat.mention.everyone", PermissionLevel.OP);
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

    @Mod.EventHandler
    public void onModPostInit(FMLPostInitializationEvent event) {

    }
}
