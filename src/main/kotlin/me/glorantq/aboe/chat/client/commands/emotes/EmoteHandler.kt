package me.glorantq.aboe.chat.client.commands.emotes

import me.glorantq.aboe.chat.client.commands.ABOEClientCommand
import me.glorantq.aboe.chat.convertColours
import net.minecraft.command.ICommandSender
import net.minecraft.util.ChatComponentText
import net.minecraftforge.client.ClientCommandHandler
import org.apache.commons.io.IOUtils
import org.apache.logging.log4j.LogManager
import org.json.simple.JSONArray
import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser
import java.io.ByteArrayOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLConnection
import java.nio.charset.Charset
import java.util.*

class EmoteHandler {
    companion object {
        private const val EMOTE_LIST_URL = "https://raw.githubusercontent.com/ABitOfEverything/ABitOfEverythingConfigs/master/emotes.json"
    }

    private val logger = LogManager.getLogger(javaClass)
    private val registeredEmotes: MutableMap<String, String>

    init {
        registeredEmotes = HashMap()

        refreshEmoteList()

        ClientCommandHandler.instance.registerCommand(object : ABOEClientCommand() {
            override fun getCommandName(): String = "refresh_emotes"
            override fun getCommandUsage(p_71518_1_: ICommandSender): String = "/$commandName"
            override fun getCommandAliases(): List<*> = arrayListOf<Any>()
            override fun addTabCompletionOptions(p_71516_1_: ICommandSender, p_71516_2_: Array<String>): List<*> = arrayListOf<Any>()

            override fun processCommand(p_71515_1_: ICommandSender, p_71515_2_: Array<String>) {
                p_71515_1_.addChatMessage(ChatComponentText("&aRefreshing emotes...".convertColours()))
                refreshEmoteList()
                p_71515_1_.addChatMessage(ChatComponentText("&aLoaded &2${registeredEmotes.size}&a emotes!".convertColours()))
            }
        })
    }

    private fun refreshEmoteList() {
        try {
            val listUrl: URL = URL(EMOTE_LIST_URL)
            val urlConnection: URLConnection = listUrl.openConnection() as HttpURLConnection

            val responseStream: ByteArrayOutputStream = ByteArrayOutputStream()
            IOUtils.copy(urlConnection.inputStream, responseStream)

            val response: String = String(responseStream.toByteArray(), Charset.forName("UTF-8"))

            val root: JSONObject = JSONParser().parse(response) as JSONObject
            val emotes: JSONArray = root["emotes"] as JSONArray

            for (o in emotes) {
                val emoteObject: JSONObject = o as JSONObject

                val name: String = emoteObject["name"].toString()
                val emote: String = emoteObject["emote"].toString()

                if (!registeredEmotes.containsKey(name)) {
                    registeredEmotes[name] = emote
                    ClientCommandHandler.instance.registerCommand(EmoteCommand(name, emote))

                    logger.info("Registered emote: {}", name)
                }
            }
        } catch (e: Exception) {
            logger.error("Failed to download emotes!", e)
        }
    }
}
