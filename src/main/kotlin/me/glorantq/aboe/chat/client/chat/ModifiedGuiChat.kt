package me.glorantq.aboe.chat.client.chat

import cpw.mods.fml.relauncher.Side
import cpw.mods.fml.relauncher.SideOnly
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiChat
import net.minecraft.client.gui.GuiPlayerInfo
import org.apache.logging.log4j.LogManager

@SideOnly(Side.CLIENT)
class ModifiedGuiChat internal constructor(defaultText: String) : GuiChat(defaultText) {
    private val logger = LogManager.getLogger("ModifiedGuiChat")

    private var drawPlayerTooltip = false
    private var bestNameMatch = ""
    private var currentPlayerName = ""

    override fun keyTyped(p_73869_1_: Char, p_73869_2_: Int) {
        super.keyTyped(p_73869_1_, p_73869_2_)

        if (p_73869_1_ == '@') {
            bestNameMatch = ""
        }

        val chatText = inputField.text

        if (drawPlayerTooltip) {
            if (bestNameMatch.isNotEmpty() && p_73869_1_ == '\t') {
                inputField.deleteWords(-1)
                inputField.writeText("@$bestNameMatch")

                logger.info("Successful autocompelte for: {}", bestNameMatch)

                bestNameMatch = ""
                currentPlayerName = ""
                drawPlayerTooltip = false
                return
            }

            val onlinePlayers: List<GuiPlayerInfo> = Minecraft.getMinecraft().thePlayer.sendQueue.playerInfoList as List<GuiPlayerInfo>

            val wordStart = inputField.getNthWordFromCursor(-1)

            val substringStart = wordStart + 1
            val substringEnd = Math.min(chatText.length, inputField.cursorPosition)

            if (substringStart > substringEnd) {
                drawPlayerTooltip = false
            } else {
                currentPlayerName = chatText.substring(substringStart, substringEnd).trim { it <= ' ' }

                bestNameMatch = ""
                if (currentPlayerName.isNotEmpty()) {
                    for (playerInfo in onlinePlayers) {
                        if (currentPlayerName.length > playerInfo.name.length) {
                            continue
                        }

                        val playerNameSubstring = playerInfo.name.substring(0, currentPlayerName.length)

                        if (playerNameSubstring.equals(currentPlayerName, ignoreCase = true)) {
                            bestNameMatch = playerInfo.name
                        }
                    }
                }
            }
        }
    }

    override fun drawScreen(p_73863_1_: Int, p_73863_2_: Int, p_73863_3_: Float) {
        drawPlayerTooltip = inputField.text.isNotEmpty() &&
                inputField.text[inputField.getNthWordFromCursor(-1)] == '@' &&
                inputField.text[inputField.cursorPosition - 1] != ' '

        if (drawPlayerTooltip) {
            var cursorX = inputField.xPosition + fontRendererObj.getStringWidth(inputField.text.substring(0, inputField.cursorPosition)) - fontRendererObj.getStringWidth("_")
            cursorX = Math.min(cursorX, inputField.xPosition + inputField.width + fontRendererObj.getStringWidth("_"))

            val renderedName: String =
                    if (bestNameMatch.isNotEmpty()) {
                        "\u00A7f" + bestNameMatch.substring(0, currentPlayerName.length) + "\u00A78" + bestNameMatch.substring(currentPlayerName.length, bestNameMatch.length)
                    } else {
                        bestNameMatch
                    }

            func_146283_a(listOf(renderedName), cursorX, inputField.yPosition - inputField.height / 2)
        }
        super.drawScreen(p_73863_1_, p_73863_2_, p_73863_3_)
    }
}
