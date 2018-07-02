package me.glorantq.aboe.chat.server.permissions

import net.minecraft.entity.player.EntityPlayer

interface IPermissionProvider {
    fun hasPermission(player: EntityPlayer, permission: String): Boolean

    fun getGroup(player: EntityPlayer): String

    fun getPrefix(player: EntityPlayer): String

    fun getSuffix(player: EntityPlayer): String

    fun registerPermission(permission: String, permissionLevel: PermissionLevel)

    fun getDisplayName(player: EntityPlayer): String

    enum class PermissionLevel {
        TRUE,

        @Deprecated("")
        OP_1,

        @Deprecated("")
        OP_2,

        @Deprecated("")
        OP_3,

        OP,

        FALSE
    }
}
