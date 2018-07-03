package me.glorantq.aboe.chat.client.notification

import java.awt.PopupMenu
import java.awt.SystemTray
import java.awt.Toolkit
import java.awt.TrayIcon

class DefaultNotifier : INotifier {
    private val systemTray: SystemTray = SystemTray.getSystemTray()
    private val trayIcon: TrayIcon = TrayIcon(Toolkit.getDefaultToolkit().createImage("https://avatars1.githubusercontent.com/u/35854210.png"), "A Bit of Everything")

    init {
        val popupMenu: PopupMenu = PopupMenu("ABOE Notifier")
        trayIcon.popupMenu = popupMenu

        systemTray.add(trayIcon)
    }

    override fun showNotification(header: String, message: String) {
        trayIcon.displayMessage(header, message, TrayIcon.MessageType.NONE)
    }
}