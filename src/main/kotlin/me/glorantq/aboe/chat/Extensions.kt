package me.glorantq.aboe.chat

import cpw.mods.fml.common.network.simpleimpl.IMessage
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper
import cpw.mods.fml.relauncher.Side
import me.glorantq.aboe.chat.client.channels.ClientChatChannel
import me.glorantq.aboe.chat.server.channels.ChatChannel
import net.minecraftforge.common.config.ConfigCategory
import net.minecraftforge.common.config.Configuration
import net.minecraftforge.common.config.Property
import kotlin.reflect.KClass

@Suppress("UNCHECKED_CAST", "IMPLICIT_CAST_TO_ANY")
fun <T> Configuration.getKeyOrSetDefault(category: String, key: String, defaultValue: T, propertyType: Property.Type): T {
    load()
    val chatCategory: ConfigCategory = getCategory(category)

    if (!chatCategory.containsKey(key)) {
        chatCategory[key] = Property(key, defaultValue.toString(), propertyType)
        save()

        return defaultValue
    }

    val property = chatCategory.get(key)
    return when (propertyType) {
        Property.Type.DOUBLE -> property.double
        Property.Type.STRING -> property.string
        Property.Type.BOOLEAN -> property.boolean
        Property.Type.INTEGER -> property.int
        else -> defaultValue
    } as T
}

fun String.convertColours() = replace("&", "\u00A7")

fun KClass<*>.register(network: SimpleNetworkWrapper, handler: KClass<*>, id: Int, side: Side) =
        network.registerMessage((handler as KClass<IMessageHandler<IMessage, IMessage>>).java, (this as KClass<IMessage>).java, id, side)

typealias ClientChannelList = MutableList<ClientChatChannel>
typealias ServerChannelMap = MutableMap<String, ChatChannel>