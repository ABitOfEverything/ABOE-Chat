package me.glorantq.aboe.chat.server.api;

import com.cedarsoftware.util.io.JsonWriter;
import cpw.mods.fml.common.FMLCommonHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import spark.Request;
import spark.Response;
import spark.Route;

import java.util.List;

public class PlayersRoute implements Route {

    @SuppressWarnings("unchecked")
    @Override
    public Object handle(Request request, Response response) throws Exception {
        JSONObject root = new JSONObject();

        MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();

        int maxPlayers = server.getMaxPlayers();
        int currentPlayers = server.getCurrentPlayerCount();

        List<EntityPlayer> players = server.getConfigurationManager().playerEntityList;

        root.put("maxPlayerCount", maxPlayers);
        root.put("currentPlayerCount", currentPlayers);

        JSONArray playersArray = new JSONArray();

        for(EntityPlayer player : players) {
            JSONObject o = new JSONObject();

            o.put("name", player.getGameProfile().getName());
            o.put("uuid", player.getGameProfile().getId().toString());

            playersArray.add(o);
        }

        root.put("players", playersArray);

        return JsonWriter.formatJson(root.toJSONString());
    }
}
