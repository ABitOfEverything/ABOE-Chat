package me.glorantq.aboe.chat.client.backport

import me.glorantq.aboe.chat.ABOEChat
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Gui
import net.minecraft.client.gui.GuiPlayerInfo
import net.minecraftforge.client.GuiIngameForge
import org.lwjgl.opengl.GL11
import net.minecraft.client.renderer.Tessellator
import net.minecraft.util.ResourceLocation

class ModifiedGUIngameForge(mc: Minecraft) : GuiIngameForge(mc) {
    override fun renderPlayerList(width: Int, height: Int) {
        val scoreObjective = this.mc.theWorld.scoreboard.func_96539_a(0)

        val chatChannels = ABOEChat.instance.clientChannelManager!!.availableChannels
        var largestSize = -1
        for (channel in chatChannels) {
            val width0 = mc.fontRenderer.getStringWidth(channel.name + " (" + channel.connectedUsers + ")")
            if (width0 > largestSize) {
                largestSize = width0
            }
        }
        Gui.drawRect(0, 0, largestSize + 10, 10 + mc.fontRenderer.FONT_HEIGHT * (2 + chatChannels.size), -0x56000000)
        var index = 2
        drawString(mc.fontRenderer, "#" + ABOEChat.instance.clientChannelManager!!.currentChannel, 5, 5, 0x00FFFF)
        for (channel in chatChannels) {
            drawString(mc.fontRenderer, channel.name + " (" + channel.connectedUsers + ")", 5, 5 + index * mc.fontRenderer.FONT_HEIGHT, if (channel.canConnect) 0x00FF00 else 0xFF0000)
            index++
        }

        if (this.mc.gameSettings.keyBindPlayerList.isKeyPressed/* && (!this.mc.isIntegratedServerRunning || this.mc.thePlayer.sendQueue.playerInfoList.size > 1 || scoreObjective != null)*/) {
            this.mc.mcProfiler.startSection("playerList")
            val netHandlerPlayClient = this.mc.thePlayer.sendQueue
            var players: List<*> = netHandlerPlayClient.playerInfoList

            var maxNameWidth = 0

            for (guiPlayerInfo in players) {
                val playerInfo = guiPlayerInfo as GuiPlayerInfo
                val nameWidth = this.mc.fontRenderer.getStringWidth(playerInfo.name)
                maxNameWidth = Math.max(maxNameWidth, nameWidth)
            }

            players = players.subList(0, Math.min(players.size, 80))
            val onlinePlayerCount = players.size
            var usersInColumn = onlinePlayerCount
            var columns: Int = 1

            while (usersInColumn > 20) {
                ++columns
                usersInColumn = (onlinePlayerCount + columns - 1) / columns
            }

            val entryHeight: Int = 9
            val totalMargin: Int = (columns - 1) * 5
            val borderWidth: Int = 1

            val columnWidth = (Math.min(columns * (maxNameWidth + 23), width - 50) / columns)
            val listStartX = (width / 2 - (columnWidth * columns + totalMargin) / 2)
            val listStartY = 10
            val listWidth = (columnWidth * columns + totalMargin)

            Gui.drawRect(width / 2 - listWidth / 2 - borderWidth, listStartY - borderWidth, width / 2 + listWidth / 2 + borderWidth, listStartY + usersInColumn * entryHeight, Integer.MIN_VALUE)

            for (playerIndex in 0 until onlinePlayerCount) {
                val playerColumn = playerIndex / usersInColumn
                val additionalUsers = playerIndex % usersInColumn
                val entryMargin: Int = playerColumn * 5
                val entryX = listStartX + playerColumn * columnWidth + entryMargin
                val entryY = listStartY + additionalUsers * entryHeight
                Gui.drawRect(entryX, entryY, entryX + columnWidth, entryY + 8, 553648127)
                GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f)
                GL11.glEnable(GL11.GL_ALPHA)
                GL11.glEnable(GL11.GL_BLEND)
                GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)

                if (playerIndex < players.size) {
                    val playerInfo: GuiPlayerInfo = players[playerIndex] as GuiPlayerInfo
                    val playerName: String = playerInfo.name

                    val skinManager: PlayerProfileManager = ABOEChat.instance.playerSkinManager!!
                    if(!skinManager.hasSkinLoaded(playerName)) {
                        skinManager.loadSkinForUser(playerName)
                    }

                    val skinLocation: ResourceLocation = skinManager.getSkinLocation(playerName)
                    this.mc.textureManager.bindTexture(skinLocation)
                    drawScaledCustomSizeModalRect(entryX, entryY, 8.0f, 16.0f, 8, 16, 8, 8, 64.0f, 64.0f)
                    drawScaledCustomSizeModalRect(entryX, entryY, 40.0f, 16.0f, 8, 16, 8, 8, 64.0f, 64.0f)

                    this.mc.fontRenderer.drawStringWithShadow(playerName, entryX + 10, entryY, -1)

                    this.drawPing(columnWidth, entryX, entryY, playerInfo)
                }
            }
        }
    }

    private fun drawPing(columnWidth: Int, x: Int, y: Int, playerInfo: GuiPlayerInfo) {
        GL11.glColor4f(1f, 1f, 1f, 1f)
        this.mc.textureManager.bindTexture(Gui.icons)

        val iconOffset: Byte = when {
            playerInfo.responseTime < 0 -> 5
            playerInfo.responseTime < 150 -> 0
            playerInfo.responseTime < 300 -> 1
            playerInfo.responseTime < 600 -> 2
            playerInfo.responseTime < 1000 -> 3
            else -> 4
        }

        this.zLevel += 100.0f
        this.drawTexturedModalRect(x + columnWidth - 11, y, 0, 176 + iconOffset * 8, 10, 8)
        this.zLevel -= 100.0f
    }

    private fun drawScaledCustomSizeModalRect(x: Int, y: Int, u: Float, v: Float, uWidth: Int, vHeight: Int, width: Int, height: Int, tileWidth: Float, tileHeight: Float) {
        val scaleU = 1.0f / tileWidth
        val scaleV = 1.0f / tileHeight

        val tessellator: Tessellator = Tessellator.instance
        tessellator.startDrawingQuads()
        tessellator.addVertexWithUV(x.toDouble(), (y + height).toDouble(), 0.0, (u * scaleU).toDouble(), ((v + vHeight.toDouble()) * scaleV))
        tessellator.addVertexWithUV((x + width).toDouble(), (y + height).toDouble(), 0.0, ((u + uWidth.toFloat()) * scaleU).toDouble(), ((v + vHeight.toFloat()) * scaleV).toDouble())
        tessellator.addVertexWithUV((x + width).toDouble(), y.toDouble(), 0.0, ((u + uWidth.toFloat()) * scaleU).toDouble(), (v * scaleV).toDouble())
        tessellator.addVertexWithUV(x.toDouble(), y.toDouble(), 0.0, (u * scaleU).toDouble(), (v * scaleV).toDouble())
        tessellator.draw()
    }
}
