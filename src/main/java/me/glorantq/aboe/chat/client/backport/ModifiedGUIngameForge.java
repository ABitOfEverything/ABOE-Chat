package me.glorantq.aboe.chat.client.backport;

import me.glorantq.aboe.chat.ABOEChat;
import me.glorantq.aboe.chat.client.channels.ClientChatChannel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiPlayerInfo;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraftforge.client.GuiIngameForge;
import org.lwjgl.opengl.GL11;

import java.util.List;

public class ModifiedGUIngameForge extends GuiIngameForge {

    public ModifiedGUIngameForge(Minecraft mc) {
        super(mc);
    }

    @Override
    protected void renderPlayerList(int width, int height) {
        ScoreObjective scoreobjective = this.mc.theWorld.getScoreboard().func_96539_a(0);

        List<ClientChatChannel> chatChannels = ABOEChat.getInstance().getClientChannelManager().getAvailableChannels();
        int largestSize = -1;
        for(ClientChatChannel channel : chatChannels) {
            int width0 = mc.fontRenderer.getStringWidth(channel.getName() + " (" + channel.getConnectedUsers() + ")");
            if(width0 > largestSize) {
                largestSize = width0;
            }
        }
        drawRect(0, 0, largestSize + 10, 10 + mc.fontRenderer.FONT_HEIGHT * (2 + chatChannels.size()), 0xAA000000);
        int index = 2;
        drawString(mc.fontRenderer, "#" + ABOEChat.getInstance().getClientChannelManager().getCurrentChannel(), 5, 5, 0x00FFFF);
        for(ClientChatChannel channel : chatChannels) {
            drawString(mc.fontRenderer, channel.getName() + " (" + channel.getConnectedUsers() + ")", 5, 5 + index * mc.fontRenderer.FONT_HEIGHT, (channel.isCanConnect() ? 0x00FF00 : 0xFF0000));
            index++;
        }

        if (this.mc.gameSettings.keyBindPlayerList.getIsKeyPressed() && (!this.mc.isIntegratedServerRunning() || this.mc.thePlayer.sendQueue.playerInfoList.size() > 1 || scoreobjective != null)) {
            this.mc.mcProfiler.startSection("playerList");
            NetHandlerPlayClient nethandlerplayclient = this.mc.thePlayer.sendQueue;
            List list = nethandlerplayclient.playerInfoList;

            int i = 0;

            for (Object networkplayerinfo : list) {
                GuiPlayerInfo playerInfo = (GuiPlayerInfo) networkplayerinfo;
                int k = this.mc.fontRenderer.getStringWidth(playerInfo.name);
                i = Math.max(i, k);
            }

            list = list.subList(0, Math.min(list.size(), 80));
            int l3 = list.size();
            int i4 = l3;
            int j4;

            for (j4 = 1; i4 > 20; i4 = (l3 + j4 - 1) / j4) {
                ++j4;
            }

            int l = 0;

            int i1 = Math.min(j4 * (i + l + 13), width - 50) / j4;
            int j1 = width / 2 - (i1 * j4 + (j4 - 1) * 5) / 2;
            int k1 = 10;
            int l1 = i1 * j4 + (j4 - 1) * 5;

            drawRect(width / 2 - l1 / 2 - 1, k1 - 1, width / 2 + l1 / 2 + 1, k1 + i4 * 9, Integer.MIN_VALUE);

            for (int k4 = 0; k4 < l3; ++k4) {
                int l4 = k4 / i4;
                int i5 = k4 % i4;
                int j2 = j1 + l4 * i1 + l4 * 5;
                int k2 = k1 + i5 * 9;
                drawRect(j2, k2, j2 + i1, k2 + 8, 553648127);
                GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
                GL11.glEnable(GL11.GL_ALPHA);
                GL11.glEnable(GL11.GL_BLEND);
                GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

                if (k4 < list.size()) {
                    GuiPlayerInfo networkplayerinfo1 = (GuiPlayerInfo) list.get(k4);

                    String s4 = networkplayerinfo1.name;

                    this.mc.fontRenderer.drawStringWithShadow(s4, j2, k2, -1);

                    this.drawPing(i1, j2, k2, networkplayerinfo1);
                }
            }
        }
    }

    private void drawPing(int p_175245_1_, int p_175245_2_, int p_175245_3_, GuiPlayerInfo playerInfo) {
        GL11.glColor4f(1, 1, 1, 1);
        this.mc.getTextureManager().bindTexture(icons);
        byte b2;

        if (playerInfo.responseTime < 0) {
            b2 = 5;
        } else if (playerInfo.responseTime < 150) {
            b2 = 0;
        } else if (playerInfo.responseTime < 300) {
            b2 = 1;
        } else if (playerInfo.responseTime < 600) {
            b2 = 2;
        } else if (playerInfo.responseTime < 1000) {
            b2 = 3;
        } else {
            b2 = 4;
        }

        this.zLevel += 100.0F;
        this.drawTexturedModalRect(p_175245_2_ + p_175245_1_ - 11, p_175245_3_, 0, 176 + b2 * 8, 10, 8);
        this.zLevel -= 100.0F;
    }
}
