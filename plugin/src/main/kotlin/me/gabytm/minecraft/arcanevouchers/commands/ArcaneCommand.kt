package me.gabytm.minecraft.arcanevouchers.commands

import me.gabytm.minecraft.arcanevouchers.ArcaneVouchers
import me.mattstudios.mf.base.CommandBase
import org.bukkit.Bukkit
import org.bukkit.util.StringUtil

abstract class ArcaneCommand(protected val plugin: ArcaneVouchers) : CommandBase(
    plugin.config.getString("settings.command") ?: "arcanevouchers",
    plugin.config.getStringList("settings.alias")
) {

    fun createPlayersCompletion(arg: String, includeAllOnlinePlayers: Boolean = false, includeAllPlayers: Boolean = false): MutableList<String> {
        val collection = if (includeAllOnlinePlayers) mutableListOf(ALL_ONLINE_PLAYERS) else mutableListOf()

        if (includeAllPlayers) {
            collection.add(ALL_PLAYERS)
        }

        return StringUtil.copyPartialMatches(arg, Bukkit.getOnlinePlayers().map { it.name }, collection)
    }

    fun createVoucherCompletion(arg: String): MutableList<String> {
        return StringUtil.copyPartialMatches(arg, plugin.voucherManager.getVoucherIds(), mutableListOf())
    }

    companion object {

        const val ALL_ONLINE_PLAYERS = "*"
        const val ALL_PLAYERS = "**"

    }

}