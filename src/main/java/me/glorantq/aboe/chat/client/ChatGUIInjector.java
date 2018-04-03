package me.glorantq.aboe.chat.client;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiNewChat;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.common.MinecraftForge;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Field;

public class ChatGUIInjector {
    private final Logger logger = LogManager.getLogger("ChatGUIInjector");
    private boolean replacedNewGuiChat = false;

    public ChatGUIInjector() {
        logger.info("ChatGUIInjector loaded!");
    }

    public void initialise() {
        MinecraftForge.EVENT_BUS.register(this);
        logger.info("Initialised!");
    }

    @SubscribeEvent
    @SuppressWarnings("unchecked")
    public void onGuiOpen(GuiOpenEvent event) {
        if (event.gui instanceof GuiChat && !(event.gui instanceof ModifiedGuiChat)) {
            Minecraft minecraft = Minecraft.getMinecraft();
            event.setCanceled(true);
            minecraft.displayGuiScreen(new ModifiedGuiChat());

            logger.info("Opened ModifiedGuiChat!");
        }

        if(event.gui instanceof GuiMainMenu && !replacedNewGuiChat) {
            try {
                Class<GuiIngame> guiIngameClass = GuiIngame.class;

                Field[] fields = guiIngameClass.getDeclaredFields();
                Field chatGuiField = null;

                for(Field field : fields) {
                    if(field.getType() == GuiNewChat.class) {
                        chatGuiField = field;
                        break;
                    }
                }

                if(chatGuiField == null) {
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
}
