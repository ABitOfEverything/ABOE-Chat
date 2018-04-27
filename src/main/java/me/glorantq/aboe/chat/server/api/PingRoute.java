package me.glorantq.aboe.chat.server.api;

import com.cedarsoftware.util.io.JsonWriter;
import org.json.simple.JSONObject;
import spark.Request;
import spark.Response;
import spark.Route;

public class PingRoute implements Route {

    @Override
    public Object handle(Request request, Response response) throws Exception {
        JSONObject root = new JSONObject();
        root.put("response", "Pong!");

        return JsonWriter.formatJson(root.toJSONString());
    }
}
