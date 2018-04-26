package me.glorantq.aboe.chat.client.commands.emotes;

import me.glorantq.aboe.chat.client.commands.ABOEClientCommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.client.ClientCommandHandler;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.ByteArrayOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EmoteHandler {
    private static final String EMOTE_LIST_URL = "https://raw.githubusercontent.com/ABitOfEverything/ABitOfEverythingConfigs/master/emotes.json";

    private final Logger logger = LogManager.getLogger(getClass());
    private final Map<String, String> registeredEmotes;

    public EmoteHandler() {
        registeredEmotes = new HashMap<>();

        refreshEmoteList();

        ClientCommandHandler.instance.registerCommand(new ABOEClientCommand() {
            @Override
            public String getCommandName() {
                return "refresh_emotes";
            }

            @Override
            public String getCommandUsage(ICommandSender p_71518_1_) {
                return "/" + getCommandName();
            }

            @Override
            public List getCommandAliases() {
                return new ArrayList();
            }

            @Override
            public void processCommand(ICommandSender p_71515_1_, String[] p_71515_2_) {
                p_71515_1_.addChatMessage(new ChatComponentText("\u00A7aRefreshing emotes..."));
                refreshEmoteList();
                p_71515_1_.addChatMessage(new ChatComponentText("\u00A7aLoaded \u00A72" + registeredEmotes.size() + "\u00A7a emotes!"));
            }

            @Override
            public List addTabCompletionOptions(ICommandSender p_71516_1_, String[] p_71516_2_) {
                return new ArrayList();
            }
        });
    }

    private void refreshEmoteList() {
        try {
            URL listUrl = new URL(EMOTE_LIST_URL);
            HttpURLConnection urlConnection = (HttpURLConnection) listUrl.openConnection();

            ByteArrayOutputStream responseStream = new ByteArrayOutputStream();
            IOUtils.copy(urlConnection.getInputStream(), responseStream);

            String response = new String(responseStream.toByteArray(), "UTF-8");

            JSONObject root = (JSONObject) new JSONParser().parse(response);
            JSONArray emotes = (JSONArray) root.get("emotes");

            for(Object o : emotes) {
                JSONObject emoteObject = (JSONObject) o;

                String name = emoteObject.get("name").toString();
                String emote = emoteObject.get("emote").toString();

                if(!registeredEmotes.containsKey(name)) {
                    registeredEmotes.put(name, emote);
                    ClientCommandHandler.instance.registerCommand(new EmoteCommand(name, emote));

                    logger.info("Registered emote: {}", name);
                }
            }
        } catch (Exception e) {
            logger.error("Failed to download emotes!", e);
        }
    }

    public Map<String, String> getRegisteredEmotes() {
        return registeredEmotes;
    }
}
