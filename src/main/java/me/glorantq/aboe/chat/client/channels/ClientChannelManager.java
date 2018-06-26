package me.glorantq.aboe.chat.client.channels;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import lombok.Getter;
import me.glorantq.aboe.chat.ABOEChat;
import me.glorantq.aboe.chat.common.PacketChangeChannel;
import me.glorantq.aboe.chat.common.PacketChangeChannelResponse;
import me.glorantq.aboe.chat.common.PacketChannelChanged;
import me.glorantq.aboe.chat.common.PacketChannelList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class ClientChannelManager {
    private final Logger logger = LogManager.getLogger("ClientChannelManager");

    private final ABOEChat aboeChat = ABOEChat.getInstance();

    private @Getter String currentChannel = "#unknown";
    private final  @Getter List<ClientChatChannel> availableChannels = new ArrayList<>();

    private void onChannelChanged(String newChannel) {
        this.currentChannel = newChannel;
        logger.info("Channel changed to: {}", newChannel);
    }

    private void onChannelStatusChanged(List<ClientChatChannel> changed) {
        synchronized (availableChannels) {
            availableChannels.clear();
            availableChannels.addAll(changed);
        }
    }

    private void onChangeChannelResponse(boolean successful, String extraMessage, List<ClientChatChannel> changedChannels) {
        System.out.println("Change: " + successful + "  " + extraMessage + "  " + changedChannels.size());
    }

    public void changeToChannel(String channelName) {
        aboeChat.getSimpleNetworkWrapper().sendToServer(new PacketChangeChannel(channelName));
    }

    public static class PacketChannelChangedHandler implements IMessageHandler<PacketChannelChanged, IMessage> {
        private final ClientChannelManager clientChannelManager = ABOEChat.getInstance().getClientChannelManager();

        @Override
        public IMessage onMessage(PacketChannelChanged message, MessageContext ctx) {
            clientChannelManager.onChannelChanged(message.getNewChannel());

            return null;
        }
    }

    public static class PacketChannelListHandler implements IMessageHandler<PacketChannelList, IMessage> {
        private final ClientChannelManager clientChannelManager = ABOEChat.getInstance().getClientChannelManager();

        @Override
        public IMessage onMessage(PacketChannelList message, MessageContext ctx) {
            clientChannelManager.onChannelStatusChanged(message.getChannels());

            return null;
        }
    }

    public static class PacketChangeChannelResponseHandler implements IMessageHandler<PacketChangeChannelResponse, IMessage> {
        private final ClientChannelManager clientChannelManager = ABOEChat.getInstance().getClientChannelManager();

        @Override
        public IMessage onMessage(PacketChangeChannelResponse message, MessageContext ctx) {
            clientChannelManager.onChangeChannelResponse(message.isSuccessful(), message.getExtraMessage(), message.getChangedChannels());

            return null;
        }
    }
}
