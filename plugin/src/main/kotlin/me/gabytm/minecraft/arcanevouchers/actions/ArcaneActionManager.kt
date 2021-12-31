package me.gabytm.minecraft.arcanevouchers.actions

import me.gabytm.minecraft.arcanevouchers.ArcaneVouchers
import me.gabytm.minecraft.arcanevouchers.actions.implementations.command.ConsoleCommandAction
import me.gabytm.minecraft.arcanevouchers.actions.implementations.command.PlayerCommandAction
import me.gabytm.minecraft.arcanevouchers.actions.implementations.message.BossBarAction
import me.gabytm.minecraft.arcanevouchers.actions.implementations.message.MessageAction
import me.gabytm.minecraft.arcanevouchers.actions.implementations.other.AddMoneyAction
import me.gabytm.minecraft.arcanevouchers.actions.implementations.other.SoundAction
import me.gabytm.minecraft.arcanevouchers.actions.permission.PermissionHandler
import me.gabytm.minecraft.arcanevouchers.actions.placeholders.PlayerNamePlaceholderProvider
import me.gabytm.util.actions.actions.Action
import me.gabytm.util.actions.spigot.actions.SpigotActionManager
import net.milkbowl.vault.economy.Economy
import org.bukkit.Bukkit
import org.bukkit.entity.Player

class ArcaneActionManager(plugin: ArcaneVouchers) : SpigotActionManager(plugin) {

    private val handler = PermissionHandler()

    private lateinit var economy: Economy

    init {
        registerDefaults(Player::class.java)
        componentParser.registerDefaults(Player::class.java)

        // Commands
        register("console") { ConsoleCommandAction(it, handler) }
        register("player") { PlayerCommandAction(it, handler) }

        // Message
        register("bossbar") { BossBarAction(it, handler) }
        register("message") { MessageAction(it, handler) }

        // Other
        if (setupEconomy()) {
            register("addmoney") { AddMoneyAction(it, handler, economy) }
        }
        register("sound") { SoundAction(it, handler) }

        // '%player_name' is the only placeholder replaced in case PlaceholderAPI is not installed
        placeholderManager.register(PlayerNamePlaceholderProvider())
    }

    private fun register(id: String, supplier: Action.Supplier<Player>) {
        register(Player::class.java, id, supplier)
    }

    private fun setupEconomy(): Boolean {
        if (!Bukkit.getPluginManager().isPluginEnabled("Vault")) {
            return false
        }

        val rsp = Bukkit.getServicesManager().getRegistration(Economy::class.java) ?: return false
        this.economy = rsp.provider
        return true
    }

    fun parseActions(actions: Collection<String>): List<ArcaneAction> {
        return parse(Player::class.java, actions).map { it as ArcaneAction }
    }

    fun executeActions(player: Player, actions: List<ArcaneAction>, args: Map<String, String> = mutableMapOf()) {
        // TODO: 12/6/2021 find a way to run actions async
        //  run sync by default and then async those that can be run async?
        run(player, actions, false, args)
    }

}