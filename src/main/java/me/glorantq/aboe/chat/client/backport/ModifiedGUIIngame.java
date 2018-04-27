package me.glorantq.aboe.chat.client.backport;

import cpw.mods.fml.common.FMLCommonHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.util.Direction;
import net.minecraft.util.MathHelper;
import net.minecraft.util.StringUtils;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.chunk.Chunk;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import java.awt.*;
import java.util.List;

public class ModifiedGUIIngame extends GuiIngame {

    public ModifiedGUIIngame(Minecraft p_i1036_1_) {
        super(p_i1036_1_);
    }

    @Override
    public void renderGameOverlay(float p_73830_1_, boolean p_73830_2_, int p_73830_3_, int p_73830_4_) {
        ScaledResolution scaledresolution = new ScaledResolution(this.mc, this.mc.displayWidth, this.mc.displayHeight);
        int screenWidth = scaledresolution.getScaledWidth();
        int screenHeight = scaledresolution.getScaledHeight();
        FontRenderer fontrenderer = this.mc.fontRenderer;
        this.mc.entityRenderer.setupOverlayRendering();
        GL11.glEnable(GL11.GL_BLEND);

        if (Minecraft.isFancyGraphicsEnabled()) {
            this.renderVignette(this.mc.thePlayer.getBrightness(p_73830_1_), screenWidth, screenHeight);
        } else {
            OpenGlHelper.glBlendFunc(770, 771, 1, 0);
        }

        ItemStack itemstack = this.mc.thePlayer.inventory.armorItemInSlot(3);

        if (this.mc.gameSettings.thirdPersonView == 0 && itemstack != null && itemstack.getItem() == Item.getItemFromBlock(Blocks.pumpkin)) {
            this.renderPumpkinBlur(screenWidth, screenHeight);
        }

        if (!this.mc.thePlayer.isPotionActive(Potion.confusion)) {
            float f1 = this.mc.thePlayer.prevTimeInPortal + (this.mc.thePlayer.timeInPortal - this.mc.thePlayer.prevTimeInPortal) * p_73830_1_;

            if (f1 > 0.0F) {
                this.func_130015_b(f1, screenWidth, screenHeight);
            }
        }

        int i1;
        int j1;
        int k1;

        if (!this.mc.playerController.enableEverythingIsScrewedUpMode()) {
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            this.mc.getTextureManager().bindTexture(widgetsTexPath);
            InventoryPlayer inventoryplayer = this.mc.thePlayer.inventory;
            this.zLevel = -90.0F;
            this.drawTexturedModalRect(screenWidth / 2 - 91, screenHeight - 22, 0, 0, 182, 22);
            this.drawTexturedModalRect(screenWidth / 2 - 91 - 1 + inventoryplayer.currentItem * 20, screenHeight - 22 - 1, 0, 22, 24, 22);
            this.mc.getTextureManager().bindTexture(icons);
            GL11.glEnable(GL11.GL_BLEND);
            OpenGlHelper.glBlendFunc(775, 769, 1, 0);
            this.drawTexturedModalRect(screenWidth / 2 - 7, screenHeight / 2 - 7, 0, 0, 16, 16);
            OpenGlHelper.glBlendFunc(770, 771, 1, 0);
            this.mc.mcProfiler.startSection("bossHealth");
            this.renderBossHealth();
            this.mc.mcProfiler.endSection();

            if (this.mc.playerController.shouldDrawHUD()) {
                this.func_110327_a(screenWidth, screenHeight);
            }

            this.mc.mcProfiler.startSection("actionBar");
            GL11.glEnable(GL12.GL_RESCALE_NORMAL);
            RenderHelper.enableGUIStandardItemLighting();

            for (i1 = 0; i1 < 9; ++i1) {
                j1 = screenWidth / 2 - 90 + i1 * 20 + 2;
                k1 = screenHeight - 16 - 3;
                this.renderInventorySlot(i1, j1, k1, p_73830_1_);
            }

            RenderHelper.disableStandardItemLighting();
            GL11.glDisable(GL12.GL_RESCALE_NORMAL);
            this.mc.mcProfiler.endSection();
            GL11.glDisable(GL11.GL_BLEND);
        }

        int l4;

        if (this.mc.thePlayer.getSleepTimer() > 0) {
            this.mc.mcProfiler.startSection("sleep");
            GL11.glDisable(GL11.GL_DEPTH_TEST);
            GL11.glDisable(GL11.GL_ALPHA_TEST);
            l4 = this.mc.thePlayer.getSleepTimer();
            float f2 = (float) l4 / 100.0F;

            if (f2 > 1.0F) {
                f2 = 1.0F - (float) (l4 - 100) / 10.0F;
            }

            j1 = (int) (220.0F * f2) << 24 | 1052704;
            drawRect(0, 0, screenWidth, screenHeight, j1);
            GL11.glEnable(GL11.GL_ALPHA_TEST);
            GL11.glEnable(GL11.GL_DEPTH_TEST);
            this.mc.mcProfiler.endSection();
        }

        l4 = 16777215;
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        i1 = screenWidth / 2 - 91;
        int l1;
        int cellCount;
        int genericVariableJ2;
        int genericVariableK;
        float f3;
        short short1;

        if (this.mc.thePlayer.isRidingHorse()) {
            this.mc.mcProfiler.startSection("jumpBar");
            this.mc.getTextureManager().bindTexture(Gui.icons);
            f3 = this.mc.thePlayer.getHorseJumpPower();
            short1 = 182;
            l1 = (int) (f3 * (float) (short1 + 1));
            cellCount = screenHeight - 32 + 3;
            this.drawTexturedModalRect(i1, cellCount, 0, 84, short1, 5);

            if (l1 > 0) {
                this.drawTexturedModalRect(i1, cellCount, 0, 89, l1, 5);
            }

            this.mc.mcProfiler.endSection();
        } else if (this.mc.playerController.gameIsSurvivalOrAdventure()) {
            this.mc.mcProfiler.startSection("expBar");
            this.mc.getTextureManager().bindTexture(Gui.icons);
            j1 = this.mc.thePlayer.xpBarCap();

            if (j1 > 0) {
                short1 = 182;
                l1 = (int) (this.mc.thePlayer.experience * (float) (short1 + 1));
                cellCount = screenHeight - 32 + 3;
                this.drawTexturedModalRect(i1, cellCount, 0, 64, short1, 5);

                if (l1 > 0) {
                    this.drawTexturedModalRect(i1, cellCount, 0, 69, l1, 5);
                }
            }

            this.mc.mcProfiler.endSection();

            if (this.mc.thePlayer.experienceLevel > 0) {
                this.mc.mcProfiler.startSection("expLevel");
                boolean flag2 = false;
                l1 = flag2 ? 16777215 : 8453920;
                String s3 = "" + this.mc.thePlayer.experienceLevel;
                genericVariableJ2 = (screenWidth - fontrenderer.getStringWidth(s3)) / 2;
                genericVariableK = screenHeight - 31 - 4;
                boolean flag1 = false;
                fontrenderer.drawString(s3, genericVariableJ2 + 1, genericVariableK, 0);
                fontrenderer.drawString(s3, genericVariableJ2 - 1, genericVariableK, 0);
                fontrenderer.drawString(s3, genericVariableJ2, genericVariableK + 1, 0);
                fontrenderer.drawString(s3, genericVariableJ2, genericVariableK - 1, 0);
                fontrenderer.drawString(s3, genericVariableJ2, genericVariableK, l1);
                this.mc.mcProfiler.endSection();
            }
        }

        String s2;

        if (this.mc.gameSettings.heldItemTooltips) {
            this.mc.mcProfiler.startSection("toolHighlight");

            if (this.remainingHighlightTicks > 0 && this.highlightingItemStack != null) {
                s2 = this.highlightingItemStack.getDisplayName();
                k1 = (screenWidth - fontrenderer.getStringWidth(s2)) / 2;
                l1 = screenHeight - 59;

                if (!this.mc.playerController.shouldDrawHUD()) {
                    l1 += 14;
                }

                cellCount = (int) ((float) this.remainingHighlightTicks * 256.0F / 10.0F);

                if (cellCount > 255) {
                    cellCount = 255;
                }

                if (cellCount > 0) {
                    GL11.glPushMatrix();
                    GL11.glEnable(GL11.GL_BLEND);
                    OpenGlHelper.glBlendFunc(770, 771, 1, 0);
                    fontrenderer.drawStringWithShadow(s2, k1, l1, 16777215 + (cellCount << 24));
                    GL11.glDisable(GL11.GL_BLEND);
                    GL11.glPopMatrix();
                }
            }

            this.mc.mcProfiler.endSection();
        }

        if (this.mc.isDemo()) {
            this.mc.mcProfiler.startSection("demo");
            s2 = "";

            if (this.mc.theWorld.getTotalWorldTime() >= 120500L) {
                s2 = I18n.format("demo.demoExpired", new Object[0]);
            } else {
                s2 = I18n.format("demo.remainingTime", new Object[]{StringUtils.ticksToElapsedTime((int) (120500L - this.mc.theWorld.getTotalWorldTime()))});
            }

            k1 = fontrenderer.getStringWidth(s2);
            fontrenderer.drawStringWithShadow(s2, screenWidth - k1 - 10, 5, 16777215);
            this.mc.mcProfiler.endSection();
        }

        int i3;
        int j3;
        int k3;

        if (this.mc.gameSettings.showDebugInfo) {
            this.mc.mcProfiler.startSection("debug");
            GL11.glPushMatrix();
            fontrenderer.drawStringWithShadow("Minecraft 1.7.10 (" + this.mc.debug + ") [Custom GuiIngame]", 2, 2, 16777215);
            fontrenderer.drawStringWithShadow(this.mc.debugInfoRenders(), 2, 12, 16777215);
            fontrenderer.drawStringWithShadow(this.mc.getEntityDebug(), 2, 22, 16777215);
            fontrenderer.drawStringWithShadow(this.mc.debugInfoEntities(), 2, 32, 16777215);
            fontrenderer.drawStringWithShadow(this.mc.getWorldProviderName(), 2, 42, 16777215);
            long i5 = Runtime.getRuntime().maxMemory();
            long j5 = Runtime.getRuntime().totalMemory();
            long k5 = Runtime.getRuntime().freeMemory();
            long l5 = j5 - k5;
            String s = "Used memory: " + l5 * 100L / i5 + "% (" + l5 / 1024L / 1024L + "MB) of " + i5 / 1024L / 1024L + "MB";
            i3 = 14737632;
            this.drawString(fontrenderer, s, screenWidth - fontrenderer.getStringWidth(s) - 2, 2, 14737632);
            s = "Allocated memory: " + j5 * 100L / i5 + "% (" + j5 / 1024L / 1024L + "MB)";
            this.drawString(fontrenderer, s, screenWidth - fontrenderer.getStringWidth(s) - 2, 12, 14737632);
            int offset = 22;
            for (String brd : FMLCommonHandler.instance().getBrandings(false)) {
                this.drawString(fontrenderer, brd, screenWidth - fontrenderer.getStringWidth(brd) - 2, offset += 10, 14737632);
            }
            j3 = MathHelper.floor_double(this.mc.thePlayer.posX);
            k3 = MathHelper.floor_double(this.mc.thePlayer.posY);
            int l3 = MathHelper.floor_double(this.mc.thePlayer.posZ);
            this.drawString(fontrenderer, String.format("x: %.5f (%d) // c: %d (%d)", new Object[]{Double.valueOf(this.mc.thePlayer.posX), Integer.valueOf(j3), Integer.valueOf(j3 >> 4), Integer.valueOf(j3 & 15)}), 2, 64, 14737632);
            this.drawString(fontrenderer, String.format("y: %.3f (feet pos, %.3f eyes pos)", new Object[]{Double.valueOf(this.mc.thePlayer.boundingBox.minY), Double.valueOf(this.mc.thePlayer.posY)}), 2, 72, 14737632);
            this.drawString(fontrenderer, String.format("z: %.5f (%d) // c: %d (%d)", new Object[]{Double.valueOf(this.mc.thePlayer.posZ), Integer.valueOf(l3), Integer.valueOf(l3 >> 4), Integer.valueOf(l3 & 15)}), 2, 80, 14737632);
            int i4 = MathHelper.floor_double((double) (this.mc.thePlayer.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
            this.drawString(fontrenderer, "f: " + i4 + " (" + Direction.directions[i4] + ") / " + MathHelper.wrapAngleTo180_float(this.mc.thePlayer.rotationYaw), 2, 88, 14737632);

            if (this.mc.theWorld != null && this.mc.theWorld.blockExists(j3, k3, l3)) {
                Chunk chunk = this.mc.theWorld.getChunkFromBlockCoords(j3, l3);
                this.drawString(fontrenderer, "lc: " + (chunk.getTopFilledSegment() + 15) + " b: " + chunk.getBiomeGenForWorldCoords(j3 & 15, l3 & 15, this.mc.theWorld.getWorldChunkManager()).biomeName + " bl: " + chunk.getSavedLightValue(EnumSkyBlock.Block, j3 & 15, k3, l3 & 15) + " sl: " + chunk.getSavedLightValue(EnumSkyBlock.Sky, j3 & 15, k3, l3 & 15) + " rl: " + chunk.getBlockLightValue(j3 & 15, k3, l3 & 15, 0), 2, 96, 14737632);
            }

            this.drawString(fontrenderer, String.format("ws: %.3f, fs: %.3f, g: %b, fl: %d", new Object[]{Float.valueOf(this.mc.thePlayer.capabilities.getWalkSpeed()), Float.valueOf(this.mc.thePlayer.capabilities.getFlySpeed()), Boolean.valueOf(this.mc.thePlayer.onGround), Integer.valueOf(this.mc.theWorld.getHeightValue(j3, l3))}), 2, 104, 14737632);

            if (this.mc.entityRenderer != null && this.mc.entityRenderer.isShaderActive()) {
                this.drawString(fontrenderer, String.format("shader: %s", new Object[]{this.mc.entityRenderer.getShaderGroup().getShaderGroupName()}), 2, 112, 14737632);
            }

            GL11.glPopMatrix();
            this.mc.mcProfiler.endSection();
        }

        if (this.recordPlayingUpFor > 0) {
            this.mc.mcProfiler.startSection("overlayMessage");
            f3 = (float) this.recordPlayingUpFor - p_73830_1_;
            k1 = (int) (f3 * 255.0F / 20.0F);

            if (k1 > 255) {
                k1 = 255;
            }

            if (k1 > 8) {
                GL11.glPushMatrix();
                GL11.glTranslatef((float) (screenWidth / 2), (float) (screenHeight - 68), 0.0F);
                GL11.glEnable(GL11.GL_BLEND);
                OpenGlHelper.glBlendFunc(770, 771, 1, 0);
                l1 = 16777215;

                if (this.recordIsPlaying) {
                    l1 = Color.HSBtoRGB(f3 / 50.0F, 0.7F, 0.6F) & 16777215;
                }

                fontrenderer.drawString(this.recordPlaying, -fontrenderer.getStringWidth(this.recordPlaying) / 2, -4, l1 + (k1 << 24 & -16777216));
                GL11.glDisable(GL11.GL_BLEND);
                GL11.glPopMatrix();
            }

            this.mc.mcProfiler.endSection();
        }

        ScoreObjective scoreobjective = this.mc.theWorld.getScoreboard().func_96539_a(1);

        if (scoreobjective != null) {
            this.func_96136_a(scoreobjective, screenHeight, screenWidth, fontrenderer);
        }

        GL11.glEnable(GL11.GL_BLEND);
        OpenGlHelper.glBlendFunc(770, 771, 1, 0);
        GL11.glDisable(GL11.GL_ALPHA_TEST);
        GL11.glPushMatrix();
        GL11.glTranslatef(0.0F, (float) (screenHeight - 48), 0.0F);
        this.mc.mcProfiler.startSection("chat");
        this.persistantChatGUI.drawChat(this.updateCounter);
        this.mc.mcProfiler.endSection();
        GL11.glPopMatrix();
        scoreobjective = this.mc.theWorld.getScoreboard().func_96539_a(0);

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

            i1 = Math.min(j4 * (i + l + 13), screenWidth - 50) / j4;
            j1 = screenWidth / 2 - (i1 * j4 + (j4 - 1) * 5) / 2;
            k1 = 10;
            l1 = i1 * j4 + (j4 - 1) * 5;

            drawRect(screenWidth / 2 - l1 / 2 - 1, k1 - 1, screenWidth / 2 + l1 / 2 + 1, k1 + i4 * 9, Integer.MIN_VALUE);

            for (int k4 = 0; k4 < l3; ++k4) {
                l4 = k4 / i4;
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

        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_ALPHA_TEST);
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
