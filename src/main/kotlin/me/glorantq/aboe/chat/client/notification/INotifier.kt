package me.glorantq.aboe.chat.client.notification

interface INotifier {
    fun showNotification(header: String, message: String)
}