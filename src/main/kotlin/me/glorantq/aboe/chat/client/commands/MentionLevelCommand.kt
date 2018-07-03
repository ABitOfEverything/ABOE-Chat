package me.glorantq.aboe.chat.client.commands

import me.glorantq.aboe.chat.ABOEChat
import me.glorantq.aboe.chat.client.chat.ChatGUIInjector
import net.minecraft.command.ICommandSender

class MentionLevelCommand : ABOEClientCommand() {
    override fun getCommandName(): String = "mentions"
    override fun getCommandUsage(p_71518_1_: ICommandSender): String ="/mentions [off|player|everyone]"
    override fun getCommandAliases(): List<*> = arrayListOf<Any>()
    override fun addTabCompletionOptions(p_71516_1_: ICommandSender, p_71516_2_: Array<String>): List<*> = arrayListOf("off", "player", "everyone")

    override fun processCommand(sender: ICommandSender, args: Array<String>) {
        if (args.isEmpty()) {
            val mentionLevel: ChatGUIInjector.MentionLevel = ABOEChat.instance.chatGUIInjector!!.mentionLevel
            sendMessage(sender, "&aYour mentions settings are set to \"&2${mentionLevel.name}&a\"!")

            return
        }

        val subcommand: String = args[0]
        val mentionLevel: ChatGUIInjector.MentionLevel

        mentionLevel = when (subcommand) {
            "off" -> ChatGUIInjector.MentionLevel.NONE
            "player" -> ChatGUIInjector.MentionLevel.PLAYER
            "everyone" -> ChatGUIInjector.MentionLevel.EVERYONE
            else -> {
                sendMessage(sender, "&cUsage: /mentions <off|player|everyone>")
                return
            }
        }

        ABOEChat.instance.chatGUIInjector!!.changeMentionLevel(mentionLevel)
        val message: String = when (mentionLevel) {
            ChatGUIInjector.MentionLevel.NONE -> "You are no longer receiving mentions!"
            ChatGUIInjector.MentionLevel.PLAYER -> "You only receive player mentions!"
            ChatGUIInjector.MentionLevel.EVERYONE -> "You are now receiving player and everyone mentions!"
        }

        sendMessage(sender, "&a$message")
    }
}
