package me.glorantq.aboe.chat.client.backport

import me.glorantq.aboe.chat.modID
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.ImageBufferDownload
import net.minecraft.client.renderer.ThreadDownloadImageData
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.ResourceLocation
import okhttp3.*
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.json.simple.JSONArray
import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser
import java.io.IOException
import java.util.*
import okhttp3.OkHttpClient
import java.util.logging.Level


class PlayerProfileManager {
    init {
        java.util.logging.Logger.getLogger(OkHttpClient::class.java.name).level = Level.FINE
    }

    private val logger: Logger = LogManager.getLogger("PlayerProfileManager")

    private val uuidEndpoint: String = "https://api.mojang.com/profiles/minecraft"
    private val profileEndpoint: String = "https://sessionserver.mojang.com/session/minecraft/profile/%s"

    private val httpClient: OkHttpClient = OkHttpClient.Builder().build()

    val missingSkinLocation: ResourceLocation = ResourceLocation(modID, "skins/default_skin.png")

    private val skinMappings: HashMap<String, ResourceLocation> = hashMapOf()
    private val uuidMappings: HashMap<String, String> = hashMapOf()

    fun hasSkinLoaded(name: String): Boolean = synchronized(skinMappings) { skinMappings.containsKey(name) }
    fun hasSkinLoaded(player: EntityPlayer): Boolean = hasSkinLoaded(player.gameProfile.name)
    fun getSkinLocation(name: String): ResourceLocation = synchronized(skinMappings) { skinMappings[name] ?: missingSkinLocation }
    fun getSkinLocation(player: EntityPlayer): ResourceLocation = getSkinLocation(player.gameProfile.name)

    private fun loadSkinFromURL(playerName: String, skinUrl: String): ResourceLocation {
        val texture: ThreadDownloadImageData = ThreadDownloadImageData(null, skinUrl, null, ImageBufferDownload())
        val resourceLocation: ResourceLocation = ResourceLocation("PlayerSkinStore/$playerName")

        Minecraft.getMinecraft().renderEngine.loadTexture(resourceLocation, texture)

        return resourceLocation
    }

    fun getIdForUser(playerName: String): String? {
        synchronized(uuidMappings) {
            if(uuidMappings.containsKey(playerName)) {
                return uuidMappings[playerName]
            }
        }

        val request: Request = Request.Builder()
                .url(uuidEndpoint)
                .post(RequestBody.create(MediaType.parse("application/json"), "[\"$playerName\"]"))
                .build()

        synchronized(uuidMappings) {
            uuidMappings[playerName] = "loading"
        }

        httpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                logger.error("Failed to get UUID for {}: {}", playerName, e.message)
                synchronized(uuidMappings) {
                    uuidMappings[playerName] = ""
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if(response.code() != 200) {
                    logger.error("Failed to get UUID for {}! ({})", playerName, response.code())
                    if(response.code() != 429) {
                        synchronized(uuidMappings) {
                            uuidMappings[playerName] = ""
                        }
                    }

                    response.body()!!.close()
                    return
                }

                val body: JSONArray = JSONParser().parse(response.body()!!.string()) as JSONArray
                response.body()!!.close()

                if(body.isEmpty()) {
                    logger.warn("Cannot get UUID for {}, offline mode?", playerName)
                    uuidMappings[playerName] = ""
                    return
                }

                val selectedProfile: JSONObject = body[0] as JSONObject

                val uuid: String = selectedProfile["id"].toString()

                synchronized(uuidMappings) {
                    uuidMappings[playerName] = uuid
                }
            }
        })

        return "loading"
    }

    fun loadSkinForUser(playerName: String): ResourceLocation? {
        synchronized(skinMappings) {
            if(hasSkinLoaded(playerName)) {
                return skinMappings[playerName]
            }
        }

        val uuid: String? = getIdForUser(playerName)

        if(uuid == null || uuid.isBlank()) {
            logger.error("Failed to load skin for {}! No UUID", playerName)
            skinMappings[playerName] = missingSkinLocation
            return missingSkinLocation
        }

        if(uuid == "loading") {
            return missingSkinLocation
        }

        val request: Request = Request.Builder()
                .url(profileEndpoint.format(uuid))
                .get()
                .build()

        httpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                logger.error("Failed to download skin for {}: {}", playerName, e.message)
                synchronized(skinMappings) {
                    skinMappings[playerName] = missingSkinLocation
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if(response.code() != 200) {
                    if(response.code() != 429) {
                        logger.error("Failed to get skin for {}! ({})", playerName, response.code())
                        synchronized(skinMappings) {
                            skinMappings[playerName] = missingSkinLocation
                        }
                    }

                    response.body()!!.close()
                    return
                }

                val profileRoot: JSONObject = JSONParser().parse(response.body()!!.string()) as JSONObject
                response.body()!!.close()
                val properties: JSONArray = profileRoot["properties"] as JSONArray

                val textures: JSONObject = JSONParser().parse(String(Base64.getDecoder().decode((properties[0] as JSONObject)["value"].toString()))) as JSONObject

                val skinUrl: String = ((textures["textures"] as JSONObject?)?.get("SKIN") as JSONObject?)?.get("url")?.toString() ?: ""

                if(skinUrl.isBlank()) {
                    logger.error("Failed to load skin for {}! No skin", playerName)
                    logger.error(textures.toJSONString())
                    skinMappings[playerName] = missingSkinLocation
                    return
                }

                synchronized(skinMappings) {
                    skinMappings[playerName] = loadSkinFromURL(playerName, skinUrl)
                }
            }
        })

        return missingSkinLocation
    }
}