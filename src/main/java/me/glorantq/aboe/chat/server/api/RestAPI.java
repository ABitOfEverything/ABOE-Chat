package me.glorantq.aboe.chat.server.api;

import cpw.mods.fml.common.FMLCommonHandler;
import me.glorantq.aboe.chat.ABOEChat;
import me.glorantq.aboe.chat.client.commands.ABOEClientCommand;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Property;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;
import spark.*;

import java.io.PrintWriter;
import java.io.StringWriter;

public class RestAPI {
    private final Logger logger = LogManager.getLogger(getClass());
    private final Service spark;

    public RestAPI() {
        spark = Service.ignite().port(getKeyOrSetDefault("port", 8080, Property.Type.INTEGER));

        spark.notFound(new Route() {
            @Override
            public Object handle(Request request, Response response) {
                return generateError(404, "Route not found").toJSONString();
            }
        });

        spark.internalServerError(new Route() {
            @Override
            public Object handle(Request request, Response response) {
                return generateError(500, "Internal Server Error").toJSONString();
            }
        });

        spark.exception(Exception.class, new ExceptionHandler<Exception>() {
            @Override
            public void handle(Exception exception, Request request, Response response) {
                StringWriter stringWriter = new StringWriter();
                PrintWriter printWriter = new PrintWriter(stringWriter);
                exception.printStackTrace(printWriter);

                String htmlTemplate = String.format("<h1>%s </h1><h4><i>%s</i></h4>%s", exception.getClass().getSimpleName(), exception.getMessage(), stringWriter.toString());
                response.body(htmlTemplate);
            }
        });

        spark.get("/", new Route() {
            @Override
            public Object handle(Request request, Response response) throws Exception {
                JSONObject root = new JSONObject();

                MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();

                root.put("motd", server.getMOTD());
                root.put("serverModName", server.getServerModName());
                root.put("port", server.getServerPort());
                root.put("minecraftVersion", server.getMinecraftVersion());

                return root.toJSONString();
            }
        });

        spark.get("/ping", new PingRoute());
        spark.get("/channels", new ChannelsRoute());
        spark.get("/players", new PlayersRoute());

        logger.info("Started internal HTTP server!");
    }

    @SuppressWarnings("unchecked")
    private JSONObject generateError(int code, String message) {
        JSONObject root = new JSONObject();
        root.put("error", message);
        root.put("code", code);

        return root;
    }

    @SuppressWarnings("unchecked")
    private <T> T getKeyOrSetDefault(String key, T defaultValue, Property.Type propertyType) {
        ABOEChat.getInstance().getConfiguration().load();
        ConfigCategory chatCategory = ABOEChat.getInstance().getConfiguration().getCategory("API");

        if (!chatCategory.containsKey(key)) {
            chatCategory.put(key, new Property(key, defaultValue.toString(), propertyType));
            ABOEChat.getInstance().getConfiguration().save();

            return defaultValue;
        }

        Property property = chatCategory.get(key);
        switch (propertyType) {
            case DOUBLE:
                return (T) (Double) property.getDouble();
            case STRING:
                return (T) property.getString();
            case BOOLEAN:
                return (T) (Boolean) property.getBoolean();
            case INTEGER:
                return (T) (Integer) property.getInt();
            default:
                return defaultValue;
        }
    }
}
