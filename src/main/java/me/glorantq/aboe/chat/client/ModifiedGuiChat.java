package me.glorantq.aboe.chat.client;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.entity.player.EntityPlayer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collections;
import java.util.List;

@SideOnly(Side.CLIENT)
public class ModifiedGuiChat extends GuiChat {
    private final Logger logger = LogManager.getLogger("ModifiedGuiChat");

    private boolean drawPlayerTooltip = false;
    private String bestNameMatch = "";
    private String currentPlayerName = "";

    ModifiedGuiChat() {
        super();
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void keyTyped(char p_73869_1_, int p_73869_2_) {
        if(drawPlayerTooltip) {
            if(bestNameMatch.length() > 0 &&p_73869_1_ == '\t') {
                inputField.deleteWords(-1);
                inputField.writeText("@" + bestNameMatch);

                logger.info("Successful autocompelte for: {}", bestNameMatch);

                bestNameMatch = "";
                currentPlayerName = "";
                drawPlayerTooltip = false;
                return;
            }

            List<EntityPlayer> onlinePlayers = Minecraft.getMinecraft().theWorld.playerEntities;

            String chatText = inputField.getText();

            if (!Character.isISOControl(p_73869_1_)) {
                chatText += p_73869_1_;
            } else if (p_73869_1_ == '\b' && chatText.length() > 0) {
                chatText = chatText.substring(0, chatText.length() - 1);
            }

            int cursorPos = inputField.getCursorPosition();
            int wordStart = inputField.getNthWordFromCursor(-1);

            int substringStart = wordStart + 1;
            int substringEnd = Math.min(chatText.length(), cursorPos + 1);

            if (substringStart > substringEnd) {
                drawPlayerTooltip = false;
            } else {
                currentPlayerName = chatText.substring(substringStart, substringEnd).split(" ")[0].trim();

                bestNameMatch = "";
                if (currentPlayerName.length() > 0) {
                    for (EntityPlayer entityPlayer : onlinePlayers) {
                        if (currentPlayerName.length() > entityPlayer.getDisplayName().length()) {
                            continue;
                        }

                        String playerNameSubstring = entityPlayer.getGameProfile().getName().substring(0, currentPlayerName.length());

                        if (playerNameSubstring.equalsIgnoreCase(currentPlayerName)) {
                            bestNameMatch = entityPlayer.getGameProfile().getName();
                        }
                    }
                }
            }
        }

        super.keyTyped(p_73869_1_, p_73869_2_);
    }

    @Override
    public void drawScreen(int p_73863_1_, int p_73863_2_, float p_73863_3_) {
        drawPlayerTooltip =
                inputField.getText().length() > 0 &&
                inputField.getText().charAt(inputField.getNthWordFromCursor(-1)) == '@' &&
                inputField.getText().charAt(inputField.getCursorPosition() - 1) != ' ';

        if(drawPlayerTooltip) {
            int cursorX = inputField.xPosition + fontRendererObj.getStringWidth(inputField.getText().substring(0, inputField.getCursorPosition())) - fontRendererObj.getStringWidth("_");
            cursorX = Math.min(cursorX, inputField.xPosition + inputField.width + fontRendererObj.getStringWidth("_"));

            String renderedName;

            if(bestNameMatch.length() > 0) {
                renderedName = "\u00A7f" + bestNameMatch.substring(0, currentPlayerName.length()) + "\u00A78" + bestNameMatch.substring(currentPlayerName.length(), bestNameMatch.length());
            } else {
                renderedName = bestNameMatch;
            }

            func_146283_a(Collections.singletonList(renderedName), cursorX, inputField.yPosition - inputField.height / 2);
        }
        super.drawScreen(p_73863_1_, p_73863_2_, p_73863_3_);
    }
}
