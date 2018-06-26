package me.glorantq.aboe.chat.client.chat;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import lombok.Data;
import me.glorantq.aboe.chat.ABOEChat;
import me.glorantq.aboe.chat.common.PacketUserMentioned;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ChatLine;
import net.minecraft.client.gui.GuiNewChat;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.MathHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL11;

import javax.swing.*;
import java.lang.reflect.Field;
import java.util.*;

@SideOnly(Side.CLIENT)
public class ModifiedGuiNewChat extends GuiNewChat {
    private final Logger logger = LogManager.getLogger("ModifiedGuiNewChat");
    private final ChatGUIInjector injector = ABOEChat.getInstance().getChatGUIInjector();

    private Minecraft mc;
    private Field _field_146253_i;
    private Field _field_146250_j;
    private Field _field_146251_k;

    public ModifiedGuiNewChat(Minecraft p_i1022_1_) {
        super(p_i1022_1_);

        this.mc = p_i1022_1_;

        try {
            _field_146253_i = getClass().getSuperclass().getDeclaredField("field_146253_i");
            _field_146250_j = getClass().getSuperclass().getDeclaredField("field_146250_j");
            _field_146251_k = getClass().getSuperclass().getDeclaredField("field_146251_k");

            _field_146253_i.setAccessible(true);
            _field_146250_j.setAccessible(true);
            _field_146251_k.setAccessible(true);

            logger.info("Gave access to all superclass variables!");
        } catch (Exception e) {
            logger.error("Failed to get fields!", e);
        }
    }

    private boolean field_146251_k() {
        try {
            return _field_146251_k.getBoolean(this);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        return false;
    }

    private int field_146250_j() {
        try {
            return _field_146250_j.getInt(this);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        return -1;
    }

    private List field_146253_i() {
        try {
            return (List) _field_146253_i.get(this);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        return new ArrayList();
    }

    private int isMentioned(IChatComponent chatComponent) {
        String unformatted = chatComponent.getUnformattedText();
        String username = Minecraft.getMinecraft().getSession().getUsername();

        if (unformatted.contains("@" + username) && injector.getMentionLevel().ordinal() >= ChatGUIInjector.MentionLevel.PLAYER.ordinal()) {
            return 1;
        }

        if (unformatted.contains("@everyone") && injector.getMentionLevel().ordinal() >= ChatGUIInjector.MentionLevel.EVERYONE.ordinal()) {
            return 2;
        }

        return 0;
    }

    private List<PlayerMention> getMentions(IChatComponent chatComponent) {
        String unformatted = chatComponent.getFormattedText().toLowerCase();
        List<PlayerMention> mentions = new LinkedList<>();

        int index = -1;

        if(injector.getMentionLevel().ordinal() >= ChatGUIInjector.MentionLevel.PLAYER.ordinal()) {
            String username = Minecraft.getMinecraft().getSession().getUsername().toLowerCase();

            do {
                index = unformatted.indexOf("@" + username, index + 1);
                if(index > 0) {
                    mentions.add(new PlayerMention(index, MentionType.PLAYER));
                }
            } while(index > 0);
        }

        if(injector.getMentionLevel().ordinal() >= ChatGUIInjector.MentionLevel.EVERYONE.ordinal()) {
            index = -1;

            do {
                index = unformatted.indexOf("@everyone", index + 1);
                if(index > 0) {
                    mentions.add(new PlayerMention(index, MentionType.EVERYONE));
                }
            } while(index > 0);
        }

        Collections.sort(mentions, new PlayerMentionComparator());

        return mentions;
    }

    @Override
    public void printChatMessageWithOptionalDeletion(IChatComponent p_146234_1_, int p_146234_2_) {
        int mention = isMentioned(p_146234_1_);
        if (mention > 0) {
            String chatText;
            int charsAdded = 0;

            for (PlayerMention playerMention : getMentions(p_146234_1_)) {
                chatText = p_146234_1_.getFormattedText();
                int adjustedIndex = playerMention.getIndex() + charsAdded;

                String mentionText = "@" + (playerMention.getMentionType() == MentionType.PLAYER ? Minecraft.getMinecraft().getSession().getUsername() : "everyone");

                String before = chatText.substring(0, adjustedIndex);
                String after = chatText.substring(adjustedIndex + mentionText.length(), chatText.length());

                p_146234_1_ = new ChatComponentText(before + "\u00A79" + mentionText + "\u00A7r" + after);

                charsAdded += 4;
            }

            Minecraft.getMinecraft().thePlayer.playSound("note.pling", 1, 1);
            logger.debug("We got mentioned!");
        }

        super.printChatMessageWithOptionalDeletion(p_146234_1_, p_146234_2_);
    }

    @Override
    public void drawChat(int p_146230_1_) {
        List chatLines = field_146253_i();
        int field_146250_j = field_146250_j();
        boolean field_146251_k = field_146251_k();

        if (mc.gameSettings.chatVisibility != EntityPlayer.EnumChatVisibility.HIDDEN) {
            int j = this.func_146232_i();
            boolean flag;
            int k = 0;
            int chatLinesSize = chatLines.size();
            float f = mc.gameSettings.chatOpacity * 0.9F + 0.1F;

            if (chatLinesSize > 0) {
                flag = this.getChatOpen();

                float f1 = this.func_146244_h();
                int i1 = MathHelper.ceiling_float_int((float) this.func_146228_f() / f1);
                GL11.glPushMatrix();
                GL11.glTranslatef(2.0F, 20.0F, 0.0F);
                GL11.glScalef(f1, f1, 1.0F);
                int j1;
                int updatedCounter;
                int opacity;

                for (j1 = 0; j1 + field_146250_j < chatLinesSize && j1 < j; ++j1) {
                    ChatLine chatline = (ChatLine) chatLines.get(j1 + field_146250_j);

                    if (chatline != null) {
                        updatedCounter = p_146230_1_ - chatline.getUpdatedCounter();

                        if (updatedCounter < 200 || flag) {
                            double d0 = (double) updatedCounter / 200.0D;
                            d0 = 1.0D - d0;
                            d0 *= 10.0D;

                            if (d0 < 0.0D) {
                                d0 = 0.0D;
                            }

                            if (d0 > 1.0D) {
                                d0 = 1.0D;
                            }

                            d0 *= d0;
                            opacity = (int) (255.0D * d0);

                            if (flag) {
                                opacity = 255;
                            }

                            opacity = (int) ((float) opacity * f);
                            ++k;

                            if (opacity > 3) {
                                byte b0 = 0;
                                int j2 = -j1 * 9;
                                drawRect(b0, j2 - 9, b0 + i1 + 4, j2, (isMentioned(chatline.func_151461_a()) > 0 ? 0xF9A825 : 0x000000) + (opacity / 2 << 24)); // @glorantq
                                GL11.glEnable(GL11.GL_BLEND); // FORGE: BugFix MC-36812 Chat Opacity Broken in 1.7.x
                                String s = chatline.func_151461_a().getFormattedText().replaceAll("\ufeff", "");
                                mc.fontRenderer.drawStringWithShadow(s, b0, j2 - 8, 0xFFFFFF + (opacity << 24));
                                GL11.glDisable(GL11.GL_ALPHA_TEST);
                            }
                        }
                    }
                }

                if (flag) {
                    j1 = mc.fontRenderer.FONT_HEIGHT;
                    GL11.glTranslatef(-3.0F, 0.0F, 0.0F);
                    int k2 = chatLinesSize * j1 + chatLinesSize;
                    updatedCounter = k * j1 + k;
                    int l2 = field_146250_j * updatedCounter / chatLinesSize;
                    int l1 = updatedCounter * updatedCounter / k2;

                    if (k2 != updatedCounter) {
                        opacity = l2 > 0 ? 170 : 96;
                        int i3 = field_146251_k ? 13382451 : 3355562;
                        drawRect(0, -l2, 2, -l2 - l1, i3 + (opacity << 24));
                        drawRect(2, -l2, 1, -l2 - l1, 13421772 + (opacity << 24));
                    }
                }

                GL11.glPopMatrix();
            }
        }
    }

    private class PlayerMentionComparator implements Comparator<PlayerMention> {
        @Override
        public int compare(PlayerMention o1, PlayerMention o2) {
            return Integer.compare(o1.index, o2.index);
        }
    }

    @Data
    private class PlayerMention {
        private final int index;
        private final MentionType mentionType;
    }

    private enum MentionType {
        EVERYONE, PLAYER
    }

    private void onPlayerMentioned(final String mentioningPlayer, final String message, boolean isEveryoneMention) {
        if(injector.getMentionLevel() != ChatGUIInjector.MentionLevel.EVERYONE && isEveryoneMention) {
            return;
        }

        if(injector.getMentionLevel() == ChatGUIInjector.MentionLevel.NONE) {
            return;
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                JOptionPane.showMessageDialog(null, "Mention by @" + mentioningPlayer + ": " + message, "New Mention", JOptionPane.INFORMATION_MESSAGE);
            }
        }).start();

        System.out.println(message);
    }

    public static class PacketUserMentionedHandler implements IMessageHandler<PacketUserMentioned, IMessage> {
        @Override
        public IMessage onMessage(PacketUserMentioned message, MessageContext ctx) {
            ABOEChat.getInstance().getChatGUIInjector().getModifiedGuiNewChat().onPlayerMentioned(message.getMentioningUser(), message.getMessage(), message.isEveryoneMention());

            return null;
        }
    }
}
