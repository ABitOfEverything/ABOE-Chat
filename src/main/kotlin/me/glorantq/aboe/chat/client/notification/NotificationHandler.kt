package me.glorantq.aboe.chat.client.notification

import org.apache.logging.log4j.LogManager
import java.awt.SystemTray
import kotlin.reflect.KClass

object NotificationHandler {
    var notificationsEnabled: Boolean = true
    private val notifier: INotifier

    init {
        val osName: String = System.getProperty("os.name")
        notifier = if(
                (osName.equals("Windows 8", ignoreCase = true) || osName.equals("Windows 8.1", ignoreCase = true) || osName.equals("Windows 10", ignoreCase = true))
            && System.getProperty("os.arch") == "amd64"
        ) {
            WindowsToastNotifier()
        } else if(SystemTray.isSupported()) {
            DefaultNotifier()
        } else {
            NoOpNotifier()
        }

        LogManager.getLogger(this::class.java).info("Notification handler set to: {}", notifier::class.qualifiedName)
    }

    fun showMessage(header: String, message: String) {
        if(!notificationsEnabled) {
            return
        }

        notifier.showNotification(header, message)
    }

    fun notifierClass(): KClass<out INotifier> = notifier::class
}