package me.glorantq.aboe.chat.client.chat

import cpw.mods.fml.common.eventhandler.SubscribeEvent
import me.glorantq.aboe.chat.ABOEChat
import me.glorantq.aboe.chat.client.backport.ModifiedGUIngameForge
import me.glorantq.aboe.chat.client.commands.MentionLevelCommand
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiChat
import net.minecraft.client.gui.GuiIngame
import net.minecraft.client.gui.GuiMainMenu
import net.minecraft.client.gui.GuiNewChat
import net.minecraftforge.client.ClientCommandHandler
import net.minecraftforge.client.event.GuiOpenEvent
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.common.config.ConfigCategory
import net.minecraftforge.common.config.Configuration
import net.minecraftforge.common.config.Property
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.lang.reflect.Field

class ChatGUIInjector {
    companion object {
        private const val CATEGORY_MENTION_SETTIGNS: String = "Mentions"
    }

    private val logger: Logger = LogManager.getLogger("ChatGUIInjector")
    private var replacedNewGuiChat: Boolean = false

    private val configuration: Configuration = ABOEChat.instance.configuration
    private val mentionSettingsCategory: ConfigCategory = configuration.getCategory(CATEGORY_MENTION_SETTIGNS)
    var mentionLevel: MentionLevel = MentionLevel.EVERYONE
        private set
    lateinit var modifiedGuiNewChat: ModifiedGuiNewChat
        private set

    init {
        logger.info("ChatGUIInjector loaded!")
    }

    fun initialise() {
        MinecraftForge.EVENT_BUS.register(this)

        if (!mentionSettingsCategory.containsKey("mention_level")) {
            changeMentionLevel(MentionLevel.EVERYONE)
        } else {
            val mentionLevel0 = Math.min(mentionSettingsCategory.get("mention_level").int, MentionLevel.values().size - 1)
            mentionLevel = MentionLevel.values()[mentionLevel0]
        }

        ClientCommandHandler.instance.registerCommand(MentionLevelCommand())

        logger.info("Initialised!")
    }

    @SubscribeEvent
    fun onGuiOpen(event: GuiOpenEvent) {
        if (event.gui is GuiChat && event.gui !is ModifiedGuiChat) {
            var defaultText: String
            try {
                var textField: Field? = null
                val fields: Array<Field> = GuiChat::class.java.declaredFields

                for (field in fields) {
                    if (field.type == String::class.java && !field.name.equals("field_146410_g", ignoreCase = true)) {
                        textField = field
                        break
                    }
                }

                textField!!.isAccessible = true
                defaultText = textField.get(event.gui).toString()
            } catch (e: Exception) {
                logger.error("Failed to copy old GuiChat!", e)
                defaultText = ""
            }

            val minecraft: Minecraft = Minecraft.getMinecraft()
            event.isCanceled = true
            minecraft.displayGuiScreen(ModifiedGuiChat(defaultText))

            logger.info("Opened ModifiedGuiChat!")
        }

        if (event.gui is GuiMainMenu && !replacedNewGuiChat) {
            Minecraft.getMinecraft().ingameGUI = ModifiedGUIngameForge(Minecraft.getMinecraft())

            try {
                val guiIngameClass: Class<GuiIngame> = GuiIngame::class.java

                val fields: Array<Field> = guiIngameClass.declaredFields
                var chatGuiField: Field? = null

                for (field in fields) {
                    if (field.type == GuiNewChat::class.java) {
                        chatGuiField = field
                        break
                    }
                }

                if (chatGuiField == null) {
                    logger.error("Failed to find GuiNewChat field!")
                    return
                }

                chatGuiField.isAccessible = true

                this.modifiedGuiNewChat = ModifiedGuiNewChat(Minecraft.getMinecraft())
                chatGuiField.set(Minecraft.getMinecraft().ingameGUI, this.modifiedGuiNewChat)
            } catch (e: Exception) {
                logger.error("Failed to replace persistentChatGui in GuiIngame!", e)
                return
            }

            replacedNewGuiChat = true
            logger.info("Replaced GuiNewChat in GuiIngame!")
        }
    }

    fun changeMentionLevel(mentionLevel: MentionLevel) {
        this.mentionLevel = mentionLevel
        mentionSettingsCategory["mention_level"] = Property("mention_level", MentionLevel.EVERYONE.ordinal.toString(), Property.Type.INTEGER)
        configuration.save()
    }

    enum class MentionLevel {
        NONE, PLAYER, EVERYONE
    }
}
