package me.glorantq.aboe.chat.client.backport

import me.glorantq.aboe.chat.ABOEChat
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Gui
import net.minecraft.client.gui.GuiPlayerInfo
import net.minecraftforge.client.GuiIngameForge
import org.lwjgl.opengl.GL11

class ModifiedGUIngameForge(mc: Minecraft) : GuiIngameForge(mc) {
    override fun renderPlayerList(width: Int, height: Int) {
        val scoreobjective = this.mc.theWorld.scoreboard.func_96539_a(0)

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

        if (this.mc.gameSettings.keyBindPlayerList.isKeyPressed && (!this.mc.isIntegratedServerRunning || this.mc.thePlayer.sendQueue.playerInfoList.size > 1 || scoreobjective != null)) {
            this.mc.mcProfiler.startSection("playerList")
            val nethandlerplayclient = this.mc.thePlayer.sendQueue
            var list: List<*> = nethandlerplayclient.playerInfoList

            var i = 0

            for (networkplayerinfo in list) {
                val playerInfo = networkplayerinfo as GuiPlayerInfo
                val k = this.mc.fontRenderer.getStringWidth(playerInfo.name)
                i = Math.max(i, k)
            }

            list = list.subList(0, Math.min(list.size, 80))
            val l3 = list.size
            var i4 = l3
            var j4: Int = 1

            while (i4 > 20) {
                ++j4
                i4 = (l3 + j4 - 1) / j4
            }

            val l = 0

            val i1 = Math.min(j4 * (i + l + 13), width - 50) / j4
            val j1 = width / 2 - (i1 * j4 + (j4 - 1) * 5) / 2
            val k1 = 10
            val l1 = i1 * j4 + (j4 - 1) * 5

            Gui.drawRect(width / 2 - l1 / 2 - 1, k1 - 1, width / 2 + l1 / 2 + 1, k1 + i4 * 9, Integer.MIN_VALUE)

            for (k4 in 0 until l3) {
                val l4 = k4 / i4
                val i5 = k4 % i4
                val j2 = j1 + l4 * i1 + l4 * 5
                val k2 = k1 + i5 * 9
                Gui.drawRect(j2, k2, j2 + i1, k2 + 8, 553648127)
                GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f)
                GL11.glEnable(GL11.GL_ALPHA)
                GL11.glEnable(GL11.GL_BLEND)
                GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)

                if (k4 < list.size) {
                    val networkplayerinfo1 = list[k4] as GuiPlayerInfo

                    val s4 = networkplayerinfo1.name

                    this.mc.fontRenderer.drawStringWithShadow(s4, j2, k2, -1)

                    this.drawPing(i1, j2, k2, networkplayerinfo1)
                }
            }
        }
    }

    private fun drawPing(p_175245_1_: Int, p_175245_2_: Int, p_175245_3_: Int, playerInfo: GuiPlayerInfo) {
        GL11.glColor4f(1f, 1f, 1f, 1f)
        this.mc.textureManager.bindTexture(Gui.icons)
        val b2: Byte

        if (playerInfo.responseTime < 0) {
            b2 = 5
        } else if (playerInfo.responseTime < 150) {
            b2 = 0
        } else if (playerInfo.responseTime < 300) {
            b2 = 1
        } else if (playerInfo.responseTime < 600) {
            b2 = 2
        } else if (playerInfo.responseTime < 1000) {
            b2 = 3
        } else {
            b2 = 4
        }

        this.zLevel += 100.0f
        this.drawTexturedModalRect(p_175245_2_ + p_175245_1_ - 11, p_175245_3_, 0, 176 + b2 * 8, 10, 8)
        this.zLevel -= 100.0f
    }
}
