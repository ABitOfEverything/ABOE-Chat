package me.glorantq.aboe.chat.server.api;

import com.cedarsoftware.util.io.JsonWriter;
import me.glorantq.aboe.chat.ABOEChat;
import me.glorantq.aboe.chat.server.channels.ChatChannel;
import me.glorantq.aboe.chat.server.channels.ChatChannelManager;
import net.minecraft.entity.player.EntityPlayer;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import spark.Request;
import spark.Response;
import spark.Route;

import java.util.List;
import java.util.Map;

public class ChannelsRoute implements Route {
    private final ChatChannelManager channelManager = ABOEChat.getInstance().getChatChannelManager();

    @SuppressWarnings("unchecked")
    @Override
    public Object handle(Request request, Response response) {
        JSONObject root = new JSONObject();

        root.put("channel_manager", channelManager.getClass().getCanonicalName());

        JSONArray channelArray = new JSONArray();
        Map<String, ChatChannel> channels = channelManager.getChannels();

        for(Map.Entry<String, ChatChannel> entry : channels.entrySet()) {
            JSONObject channelObject = new JSONObject();
            ChatChannel channel = entry.getValue();
            channelObject.put("name", channel.getChannelName());
            channelObject.put("id", channel.getChannelId());

            JSONArray players = new JSONArray();
            List<EntityPlayer> players0 = channel.getOnlinePlayers();
            for(EntityPlayer player : players0) {
                JSONObject playerObject = new JSONObject();

                playerObject.put("uuid", player.getGameProfile().getId().toString());
                playerObject.put("name", player.getGameProfile().getName());

                players.add(playerObject);
            }

            channelObject.put("players", players);

            channelArray.add(channelObject);
        }

        root.put("channels", channelArray);
        return JsonWriter.formatJson(root.toJSONString());
    }
}
