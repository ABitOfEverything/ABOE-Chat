package me.glorantq.aboe.chat.client.chat;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import lombok.Getter;
import me.glorantq.aboe.chat.ABOEChat;
import me.glorantq.aboe.chat.client.commands.MentionLevelCommand;
import me.glorantq.aboe.chat.client.commands.emotes.TableFlipEmote;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Field;

public class ChatGUIInjector {
    private static final String CATEGORY_MENTION_SETTIGNS = "Mentions";

    private final Logger logger = LogManager.getLogger("ChatGUIInjector");
    private boolean replacedNewGuiChat = false;

    private Configuration configuration = ABOEChat.getInstance().getConfiguration();
    private ConfigCategory mentionSettingsCategory = configuration.getCategory(CATEGORY_MENTION_SETTIGNS);
    private @Getter
    MentionLevel mentionLevel = MentionLevel.EVERYONE;

    public ChatGUIInjector() {
        logger.info("ChatGUIInjector loaded!");
    }

    public void initialise() {
        MinecraftForge.EVENT_BUS.register(this);

        if (!mentionSettingsCategory.containsKey("mention_level")) {
            changeMentionLevel(MentionLevel.EVERYONE);
        } else {
            int mentionLevel0 = Math.min(mentionSettingsCategory.get("mention_level").getInt(), MentionLevel.values().length - 1);
            mentionLevel = MentionLevel.values()[mentionLevel0];
        }

        ClientCommandHandler.instance.registerCommand(new MentionLevelCommand());
        ClientCommandHandler.instance.registerCommand(new TableFlipEmote());

        logger.info("Initialised!");
    }

    @SubscribeEvent
    @SuppressWarnings("unchecked")
    public void onGuiOpen(GuiOpenEvent event) {
        if (event.gui instanceof GuiChat && !(event.gui instanceof ModifiedGuiChat)) {
            String defaultText;
            try {
                Field textField = null;
                Field[] fields = GuiChat.class.getDeclaredFields();

                for(Field field : fields) {
                    if(field.getType() == String.class && !field.getName().equalsIgnoreCase("field_146410_g")) {
                        textField = field;
                        break;
                    }
                }

                textField.setAccessible(true);
                defaultText = textField.get(event.gui).toString();
            } catch (Exception e) {
                logger.error("Failed to copy old GuiChat!", e);
                defaultText = "";
            }

            Minecraft minecraft = Minecraft.getMinecraft();
            event.setCanceled(true);
            minecraft.displayGuiScreen(new ModifiedGuiChat(defaultText));

            logger.info("Opened ModifiedGuiChat!");
        }

        if (event.gui instanceof GuiMainMenu && !replacedNewGuiChat) {
            try {
                Class<GuiIngame> guiIngameClass = GuiIngame.class;

                Field[] fields = guiIngameClass.getDeclaredFields();
                Field chatGuiField = null;

                for (Field field : fields) {
                    if (field.getType() == GuiNewChat.class) {
                        chatGuiField = field;
                        break;
                    }
                }

                if (chatGuiField == null) {
                    logger.error("Failed to find GuiNewChat field!");
                    return;
                }

                chatGuiField.setAccessible(true);

                chatGuiField.set(Minecraft.getMinecraft().ingameGUI, new ModifiedGuiNewChat(Minecraft.getMinecraft()));
            } catch (Exception e) {
                logger.error("Failed to replace persistentChatGui in GuiIngame!", e);
                return;
            }

            replacedNewGuiChat = true;
            logger.info("Replaced GuiNewChat in GuiIngame!");
        }
    }

    public void changeMentionLevel(MentionLevel mentionLevel) {
        this.mentionLevel = mentionLevel;
        mentionSettingsCategory.put("mention_level", new Property("mention_level", String.valueOf(MentionLevel.EVERYONE.ordinal()), Property.Type.INTEGER));
        configuration.save();
    }

    public enum MentionLevel {
        NONE, PLAYER, EVERYONE
    }
}
