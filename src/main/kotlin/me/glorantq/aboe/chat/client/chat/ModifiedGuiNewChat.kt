package me.glorantq.aboe.chat.client.chat

import com.google.common.base.Splitter
import com.vdurmont.emoji.EmojiParser
import cpw.mods.fml.common.network.simpleimpl.IMessage
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler
import cpw.mods.fml.common.network.simpleimpl.MessageContext
import cpw.mods.fml.relauncher.Side
import cpw.mods.fml.relauncher.SideOnly
import me.glorantq.aboe.chat.ABOEChat
import me.glorantq.aboe.chat.client.notification.NotificationHandler
import me.glorantq.aboe.chat.common.PacketUserMentioned
import me.glorantq.aboe.chat.convertColours
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.ChatLine
import net.minecraft.client.gui.Gui
import net.minecraft.client.gui.GuiNewChat
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.ChatComponentText
import net.minecraft.util.IChatComponent
import net.minecraft.util.MathHelper
import org.apache.logging.log4j.LogManager
import org.lwjgl.opengl.GL11
import java.lang.reflect.Field
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern

@SideOnly(Side.CLIENT)
class ModifiedGuiNewChat(private val mc: Minecraft) : GuiNewChat(mc) {
    private val logger = LogManager.getLogger("ModifiedGuiNewChat")
    private val injector = ABOEChat.instance.chatGUIInjector!!
    private var field_146253_i: Field? = null
    private var field_146250_j: Field? = null
    private var field_146251_k: Field? = null

    private val emojiFinderRegex: Pattern = Pattern.compile("(?<encoded>(?>[&§]#x[a-f0-9]{5};){1,2})")
    private val hexExtractPattern: Pattern = Pattern.compile("[&§]#x(?<hex>[a-f0-9]{5})")

    init {
        try {
            field_146253_i = javaClass.superclass.getDeclaredField("field_146253_i")
            field_146250_j = javaClass.superclass.getDeclaredField("field_146250_j")
            field_146251_k = javaClass.superclass.getDeclaredField("field_146251_k")

            field_146253_i!!.isAccessible = true
            field_146250_j!!.isAccessible = true
            field_146251_k!!.isAccessible = true

            logger.info("Gave access to all superclass variables!")
        } catch (e: Exception) {
            logger.error("Failed to get fields!", e)
        }

    }

    private fun field_146251_k(): Boolean {
        try {
            return field_146251_k!!.getBoolean(this)
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        }

        return false
    }

    private fun field_146250_j(): Int {
        try {
            return field_146250_j!!.getInt(this)
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        }

        return -1
    }

    private fun field_146253_i(): List<*> {
        try {
            return field_146253_i!!.get(this) as List<*>
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        }

        return arrayListOf<Any>()
    }

    private fun isMentioned(chatComponent: IChatComponent): Int {
        val unformatted = chatComponent.unformattedText
        val username = Minecraft.getMinecraft().session.username

        if (unformatted.contains("@$username") && injector.mentionLevel.ordinal >= ChatGUIInjector.MentionLevel.PLAYER.ordinal) {
            return 1
        }

        return if (unformatted.contains("@everyone") && injector.mentionLevel.ordinal >= ChatGUIInjector.MentionLevel.EVERYONE.ordinal) {
            2
        } else 0

    }

    private fun getMentions(chatComponent: IChatComponent): List<PlayerMention> {
        val unformatted = chatComponent.formattedText.toLowerCase()
        val mentions = LinkedList<PlayerMention>()

        var index = -1

        if (injector.mentionLevel.ordinal >= ChatGUIInjector.MentionLevel.PLAYER.ordinal) {
            val username = Minecraft.getMinecraft().session.username.toLowerCase()

            do {
                index = unformatted.indexOf("@$username", index + 1)
                if (index > 0) {
                    mentions.add(PlayerMention(index, MentionType.PLAYER))
                }
            } while (index > 0)
        }

        if (injector.mentionLevel.ordinal >= ChatGUIInjector.MentionLevel.EVERYONE.ordinal) {
            index = -1

            do {
                index = unformatted.indexOf("@everyone", index + 1)
                if (index > 0) {
                    mentions.add(PlayerMention(index, MentionType.EVERYONE))
                }
            } while (index > 0)
        }

        Collections.sort(mentions, PlayerMentionComparator())

        return mentions
    }

    override fun printChatMessageWithOptionalDeletion(p_146234_1_: IChatComponent, p_146234_2_: Int) {
        var p_146234_1_0 = p_146234_1_
        val mention = isMentioned(p_146234_1_)
        if (mention > 0) {
            var chatText: String
            var charsAdded = 0

            for (playerMention in getMentions(p_146234_1_0)) {
                chatText = p_146234_1_0.formattedText
                val adjustedIndex = playerMention.index + charsAdded

                val mentionText = "@" + if (playerMention.mentionType === MentionType.PLAYER) Minecraft.getMinecraft().session.username else "everyone"

                val before = chatText.substring(0, adjustedIndex)
                val after = chatText.substring(adjustedIndex + mentionText.length, chatText.length)

                p_146234_1_0 = ChatComponentText("$before&9$mentionText&r$after".convertColours())

                charsAdded += 4
            }

            Minecraft.getMinecraft().thePlayer.playSound("note.pling", 1f, 1f)
            logger.debug("We got mentioned!")
        }

        super.printChatMessageWithOptionalDeletion(p_146234_1_0, p_146234_2_)
    }

    override fun drawChat(p_146230_1_: Int) {
        val chatLines = field_146253_i()
        val field_146250_j = field_146250_j()
        val field_146251_k = field_146251_k()

        if (mc.gameSettings.chatVisibility != EntityPlayer.EnumChatVisibility.HIDDEN) {
            val j = this.func_146232_i()
            val flag: Boolean
            var k = 0
            val chatLinesSize = chatLines.size
            val f = mc.gameSettings.chatOpacity * 0.9f + 0.1f

            if (chatLinesSize > 0) {
                flag = this.chatOpen

                val f1 = this.func_146244_h()
                val i1 = MathHelper.ceiling_float_int(this.func_146228_f().toFloat() / f1)
                GL11.glPushMatrix()
                GL11.glTranslatef(2.0f, 20.0f, 0.0f)
                GL11.glScalef(f1, f1, 1.0f)
                var j1: Int
                var updatedCounter: Int
                var opacity: Int

                j1 = 0
                while (j1 + field_146250_j < chatLinesSize && j1 < j) {
                    val chatline = chatLines[j1 + field_146250_j] as ChatLine

                    updatedCounter = p_146230_1_ - chatline.updatedCounter

                    if (updatedCounter < 200 || flag) {
                        var d0 = updatedCounter.toDouble() / 200.0
                        d0 = 1.0 - d0
                        d0 *= 10.0

                        if (d0 < 0.0) {
                            d0 = 0.0
                        }

                        if (d0 > 1.0) {
                            d0 = 1.0
                        }

                        d0 *= d0
                        opacity = (255.0 * d0).toInt()

                        if (flag) {
                            opacity = 255
                        }

                        opacity = (opacity.toFloat() * f).toInt()
                        ++k

                        if (opacity > 3) {
                            val b0: Byte = 0
                            val j2 = -j1 * 9
                            Gui.drawRect(b0.toInt(), j2 - 9, b0.toInt() + i1 + 4, j2, (if (isMentioned(chatline.func_151461_a()) > 0) 0xF9A825 else 0x000000) + (opacity / 2 shl 24)) // @glorantq
                            GL11.glEnable(GL11.GL_BLEND) // FORGE: BugFix MC-36812 Chat Opacity Broken in 1.7.x
                            val s = chatline.func_151461_a().formattedText.replace('\ufeff', '\u0000')
                            mc.fontRenderer.drawStringWithShadow(s, b0.toInt(), j2 - 8, 0xFFFFFF + (opacity shl 24))
                            GL11.glDisable(GL11.GL_ALPHA_TEST)
                        }
                    }
                    ++j1
                }

                if (flag) {
                    j1 = mc.fontRenderer.FONT_HEIGHT
                    GL11.glTranslatef(-3.0f, 0.0f, 0.0f)
                    val k2 = chatLinesSize * j1 + chatLinesSize
                    updatedCounter = k * j1 + k
                    val l2 = field_146250_j * updatedCounter / chatLinesSize
                    val l1 = updatedCounter * updatedCounter / k2

                    if (k2 != updatedCounter) {
                        opacity = if (l2 > 0) 170 else 96
                        val i3 = if (field_146251_k) 13382451 else 3355562
                        Gui.drawRect(0, -l2, 2, -l2 - l1, i3 + (opacity shl 24))
                        Gui.drawRect(2, -l2, 1, -l2 - l1, 13421772 + (opacity shl 24))
                    }
                }

                GL11.glPopMatrix()
            }
        }
    }

    private inner class PlayerMentionComparator : Comparator<PlayerMention> {
        override fun compare(o1: PlayerMention, o2: PlayerMention): Int {
            return Integer.compare(o1.index, o2.index)
        }
    }

    private data class PlayerMention(val index: Int, val mentionType: MentionType)

    private enum class MentionType {
        EVERYONE, PLAYER
    }

    private fun onPlayerMentioned(mentioningPlayer: String, message: String, isEveryoneMention: Boolean) {
        if (injector.mentionLevel != ChatGUIInjector.MentionLevel.EVERYONE && isEveryoneMention) {
            return
        }

        if (injector.mentionLevel == ChatGUIInjector.MentionLevel.NONE) {
            return
        }

        NotificationHandler.showMessage("$mentioningPlayer - #${ABOEChat.instance.clientChannelManager!!.currentChannel}", message)
    }

    class PacketUserMentionedHandler : IMessageHandler<PacketUserMentioned, IMessage> {
        override fun onMessage(message: PacketUserMentioned, ctx: MessageContext): IMessage? {
            ABOEChat.instance.chatGUIInjector!!.modifiedGuiNewChat.onPlayerMentioned(message.mentioningUser, message.message, message.isEveryoneMention)

            return null
        }
    }
}
