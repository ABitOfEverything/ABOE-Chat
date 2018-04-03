package me.glorantq.aboe.chat.client.chat;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiPlayerInfo;
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
        super.keyTyped(p_73869_1_, p_73869_2_);

        if (p_73869_1_ == '@') {
            bestNameMatch = "";
        }

        String chatText = inputField.getText();

        if (drawPlayerTooltip) {
            if (bestNameMatch.length() > 0 && p_73869_1_ == '\t') {
                inputField.deleteWords(-1);
                inputField.writeText("@" + bestNameMatch);

                logger.info("Successful autocompelte for: {}", bestNameMatch);

                bestNameMatch = "";
                currentPlayerName = "";
                drawPlayerTooltip = false;
                return;
            }

            List<GuiPlayerInfo> onlinePlayers = Minecraft.getMinecraft().thePlayer.sendQueue.playerInfoList; // GuiIngame#L441

            System.out.println(chatText);

            int wordStart = inputField.getNthWordFromCursor(-1);

            int substringStart = wordStart + 1;
            int substringEnd = Math.min(chatText.length(), inputField.getCursorPosition());

            System.out.printf("%d %d %d\n", substringStart, substringEnd, inputField.getCursorPosition());

            if (substringStart > substringEnd) {
                drawPlayerTooltip = false;
            } else {
                currentPlayerName = chatText.substring(substringStart, substringEnd).trim();

                System.out.println(currentPlayerName);

                bestNameMatch = "";
                if (currentPlayerName.length() > 0) {
                    for (GuiPlayerInfo playerInfo : onlinePlayers) {
                        if (currentPlayerName.length() > playerInfo.name.length()) {
                            continue;
                        }

                        String playerNameSubstring = playerInfo.name.substring(0, currentPlayerName.length());

                        if (playerNameSubstring.equalsIgnoreCase(currentPlayerName)) {
                            bestNameMatch = playerInfo.name;
                        }
                    }
                }
            }
        }
    }

    @Override
    public void drawScreen(int p_73863_1_, int p_73863_2_, float p_73863_3_) {
        drawPlayerTooltip =
                inputField.getText().length() > 0 &&
                        inputField.getText().charAt(inputField.getNthWordFromCursor(-1)) == '@' &&
                        inputField.getText().charAt(inputField.getCursorPosition() - 1) != ' ';

        if (drawPlayerTooltip) {
            int cursorX = inputField.xPosition + fontRendererObj.getStringWidth(inputField.getText().substring(0, inputField.getCursorPosition())) - fontRendererObj.getStringWidth("_");
            cursorX = Math.min(cursorX, inputField.xPosition + inputField.width + fontRendererObj.getStringWidth("_"));

            String renderedName;

            if (bestNameMatch.length() > 0) {
                renderedName = "\u00A7f" + bestNameMatch.substring(0, currentPlayerName.length()) + "\u00A78" + bestNameMatch.substring(currentPlayerName.length(), bestNameMatch.length());
            } else {
                renderedName = bestNameMatch;
            }

            func_146283_a(Collections.singletonList(renderedName), cursorX, inputField.yPosition - inputField.height / 2);
        }
        super.drawScreen(p_73863_1_, p_73863_2_, p_73863_3_);
    }
}
