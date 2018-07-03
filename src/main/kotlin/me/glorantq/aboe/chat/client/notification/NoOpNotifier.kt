package me.glorantq.aboe.chat.client.notification

class NoOpNotifier : INotifier {
    override fun showNotification(header: String, message: String) {

    }
}