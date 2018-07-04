package me.glorantq.aboe.chat.client.commands

import me.glorantq.aboe.chat.client.notification.NotificationHandler
import net.minecraft.command.ICommandSender

class NotificationsCommand : ABOEClientCommand() {
    override fun getCommandName(): String = "notifications"
    override fun getCommandUsage(p_71518_1_: ICommandSender?): String = "/notifications [on|off]"
    override fun getCommandAliases(): MutableList<Any?> = arrayListOf()
    override fun addTabCompletionOptions(p_71516_1_: ICommandSender?, p_71516_2_: Array<out String>?): MutableList<Any?> = arrayListOf("on", "off")

    override fun processCommand(sender: ICommandSender, args: Array<out String>) {
        if(args.isEmpty()) {
            sendMessage(sender, "&aNotifications are currently &2${if(NotificationHandler.notificationsEnabled) { "enabled" } else { "disabled" }}.")
            return
        }

        if(args[0] == "handler") {
            sendMessage(sender, "&aThe notification handler is &2${NotificationHandler.notifierClass().qualifiedName}")
            return
        }

        val enabled: Boolean = args[0].equals("on", ignoreCase = true)
        NotificationHandler.notificationsEnabled = enabled

        val message: String = when(enabled) {
            true -> "on"
            else -> "off"
        }

        sendMessage(sender, "&aYour notifications have been turned &2$message&a!")
    }
}