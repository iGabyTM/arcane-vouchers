package me.gabytm.minecraft.arcanevouchers.actions

import me.gabytm.minecraft.arcanevouchers.ArcaneVouchers
import me.gabytm.minecraft.arcanevouchers.actions.implementations.command.ConsoleCommandAction
import me.gabytm.minecraft.arcanevouchers.actions.implementations.command.PlayerCommandAction
import me.gabytm.minecraft.arcanevouchers.actions.implementations.crates.GiveCrateReloadedKeyAction
import me.gabytm.minecraft.arcanevouchers.actions.implementations.economy.AddExpAction
import me.gabytm.minecraft.arcanevouchers.actions.implementations.economy.ItemAction
import me.gabytm.minecraft.arcanevouchers.actions.implementations.message.BossBarAction
import me.gabytm.minecraft.arcanevouchers.actions.implementations.message.ChatAction
import me.gabytm.minecraft.arcanevouchers.actions.implementations.message.MessageAction
import me.gabytm.minecraft.arcanevouchers.actions.implementations.other.EffectAction
import me.gabytm.minecraft.arcanevouchers.actions.implementations.other.SoundAction
import me.gabytm.minecraft.arcanevouchers.actions.implementations.vault.AddMoneyAction
import me.gabytm.minecraft.arcanevouchers.actions.implementations.vault.PermissionAction
import me.gabytm.minecraft.arcanevouchers.actions.permission.PermissionHandler
import me.gabytm.minecraft.arcanevouchers.actions.placeholders.PlayerNamePlaceholderProvider
import me.gabytm.minecraft.arcanevouchers.functions.info
import me.gabytm.util.actions.actions.Action
import me.gabytm.util.actions.spigot.actions.SpigotActionManager
import me.gabytm.util.actions.spigot.placeholders.PlaceholderAPIProvider
import net.milkbowl.vault.economy.Economy
import net.milkbowl.vault.permission.Permission
import org.bukkit.Bukkit
import org.bukkit.entity.Player

class ArcaneActionManager(plugin: ArcaneVouchers) : SpigotActionManager(plugin) {

    private val handler = PermissionHandler()

    private lateinit var economy: Economy
    private lateinit var permission: Permission

    init {
        registerDefaults(Player::class.java)
        componentParser.registerDefaults(Player::class.java)

        // Commands
        register("console") { ConsoleCommandAction(it, handler) }
        register("player") { PlayerCommandAction(it, handler) }
        //-----

        // Crates
        register("CrateReloaded", GiveCrateReloadedKeyAction.ID) { GiveCrateReloadedKeyAction(it, handler) }
        //-----

        // Economy
        register("addexp") { AddExpAction(it, handler) }
        register("item") { ItemAction(it, handler) }
        //-----

        // Message
        register("bossbar") { BossBarAction(it, handler) }
        register("chat") { ChatAction(it, handler) }
        register("message") { MessageAction(it, handler) }
        //-----

        // Other
        register("effect") { EffectAction(it, handler) }
        register("sound") { SoundAction(it, handler) }
        //-----

        // Vault
        if (setupEconomy()) {
            register("addmoney") { AddMoneyAction(it, handler, economy) }
        }

        if (setupPermission()) {
            register("permission") { PermissionAction(it, handler, permission) }
        }
        //-----

        info("Loaded actions: ${actions.rowKeySet().sorted().joinToString(", ")}")

        // '%player_name' is the only placeholder replaced in case PlaceholderAPI is not installed
        placeholderManager.register(PlayerNamePlaceholderProvider())

        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            placeholderManager.register(PlaceholderAPIProvider())
        }
    }

    private fun register(id: String, supplier: Action.Supplier<Player>) {
        register(Player::class.java, id, supplier)
    }

    private fun register(plugin: String, id: String, supplier: Action.Supplier<Player>) {
        if (Bukkit.getPluginManager().isPluginEnabled(plugin)) {
            info("Registering [$id] because $plugin is on the server")
            register(id, supplier)
        }
    }

    private fun setupEconomy(): Boolean {
        if (!Bukkit.getPluginManager().isPluginEnabled("Vault")) {
            return false
        }

        val rsp = Bukkit.getServicesManager().getRegistration(Economy::class.java) ?: return false
        this.economy = rsp.provider
        return true
    }

    private fun setupPermission(): Boolean {
        if (!Bukkit.getPluginManager().isPluginEnabled("Vault")) {
            return false
        }

        val rsp = Bukkit.getServicesManager().getRegistration(Permission::class.java) ?: return false
        this.permission = rsp.provider
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