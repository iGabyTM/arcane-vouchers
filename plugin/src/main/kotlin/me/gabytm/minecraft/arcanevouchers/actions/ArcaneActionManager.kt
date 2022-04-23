package me.gabytm.minecraft.arcanevouchers.actions

import me.gabytm.minecraft.arcanevouchers.ArcaneVouchers
import me.gabytm.minecraft.arcanevouchers.actions.implementations.command.ConsoleCommandAction
import me.gabytm.minecraft.arcanevouchers.actions.implementations.command.PlayerCommandAction
import me.gabytm.minecraft.arcanevouchers.actions.implementations.crates.GiveCrateReloadedKeyAction
import me.gabytm.minecraft.arcanevouchers.actions.implementations.economy.AddExpAction
import me.gabytm.minecraft.arcanevouchers.actions.implementations.economy.ItemAction
import me.gabytm.minecraft.arcanevouchers.actions.implementations.economy.VoucherAction
import me.gabytm.minecraft.arcanevouchers.actions.implementations.message.BossBarAction
import me.gabytm.minecraft.arcanevouchers.actions.implementations.message.ChatAction
import me.gabytm.minecraft.arcanevouchers.actions.implementations.message.MessageAction
import me.gabytm.minecraft.arcanevouchers.actions.implementations.other.DataAction
import me.gabytm.minecraft.arcanevouchers.actions.implementations.other.EffectAction
import me.gabytm.minecraft.arcanevouchers.actions.implementations.other.SoundAction
import me.gabytm.minecraft.arcanevouchers.actions.implementations.vault.AddMoneyAction
import me.gabytm.minecraft.arcanevouchers.actions.implementations.vault.PermissionAction
import me.gabytm.minecraft.arcanevouchers.actions.permission.PermissionHandler
import me.gabytm.minecraft.arcanevouchers.actions.placeholders.PlayerNamePlaceholderProvider
import me.gabytm.minecraft.arcanevouchers.functions.exception
import me.gabytm.minecraft.arcanevouchers.functions.info
import me.gabytm.util.actions.actions.Action
import me.gabytm.util.actions.spigot.actions.SpigotActionManager
import me.gabytm.util.actions.spigot.placeholders.PlaceholderAPIProvider
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.JoinConfiguration
import net.kyori.adventure.text.format.NamedTextColor
import net.milkbowl.vault.economy.Economy
import net.milkbowl.vault.permission.Permission
import org.bukkit.Bukkit
import org.bukkit.entity.Player

class ArcaneActionManager(plugin: ArcaneVouchers) : SpigotActionManager(plugin) {

    private val handler = PermissionHandler()

    private lateinit var economy: Economy
    private lateinit var permission: Permission

    private val usages = mutableListOf<Component>()
    val actionsMessage: Component

    init {
        //registerDefaults(Player::class.java)
        componentParser.registerDefaults(Player::class.java)

        // Commands
        usages.add(Component.text("+ Commands:", NamedTextColor.LIGHT_PURPLE))
        register("console", ConsoleCommandAction::class.java) { ConsoleCommandAction(it, handler) }
        register("player", PlayerCommandAction::class.java) { PlayerCommandAction(it, handler) }
        //-----

        // Crates
        usages.add(Component.text("+ Crates:", NamedTextColor.LIGHT_PURPLE))
        register("CrateReloaded", GiveCrateReloadedKeyAction.ID, GiveCrateReloadedKeyAction::class.java) { GiveCrateReloadedKeyAction(it, handler) }
        //-----

        // Economy
        usages.add(Component.text("+ Economy:", NamedTextColor.LIGHT_PURPLE))
        register("addexp", AddExpAction::class.java) { AddExpAction(it, handler) }
        register("item", ItemAction::class.java) { ItemAction(it, handler, plugin.itemCreator) }
        register("voucher", VoucherAction::class.java) { VoucherAction(it, handler, plugin.voucherManager) }
        //-----

        // Message
        usages.add(Component.text("+ Message:", NamedTextColor.LIGHT_PURPLE))
        register("bossbar", BossBarAction::class.java) { BossBarAction(it, handler) }
        register("chat", ChatAction::class.java) { ChatAction(it, handler) }
        register("message", MessageAction::class.java) { MessageAction(it, handler) }
        //-----

        // Other
        usages.add(Component.text("+ Other:", NamedTextColor.LIGHT_PURPLE))
        register("data", DataAction::class.java) { DataAction(it, handler) }
        register("effect", EffectAction::class.java) { EffectAction(it, handler) }
        register("sound", SoundAction::class.java) { SoundAction(it, handler) }
        //-----

        // Vault
        if (Bukkit.getPluginManager().isPluginEnabled("Vault")) {
            usages.add(Component.text("+ Vault:", NamedTextColor.DARK_PURPLE))

            if (setupEconomy()) {
                register("addmoney", AddMoneyAction::class.java) { AddMoneyAction(it, handler, economy) }
            }

            if (setupPermission()) {
                register("permission", PermissionAction::class.java) { PermissionAction(it, handler, permission) }
            }
        }
        //-----

        info("Loaded actions: ${actions.rowKeySet().sorted().joinToString(", ")}")

        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            placeholderManager.register(PlaceholderAPIProvider())
        } else {
            // '%player_name' is the only placeholder replaced in case PlaceholderAPI is not installed
            placeholderManager.register(PlayerNamePlaceholderProvider())
        }

        // Join all usages in one component
        actionsMessage = Component.join(JoinConfiguration.newlines(), usages)
        usages.clear()
    }

    private fun register(id: String, clazz: Class<*>, supplier: Action.Supplier<Player>) {
        try {
            val idField = clazz.getDeclaredField("ID")
            idField.isAccessible = true

            register(Player::class.java, idField.get(null) as String, supplier)

            val method = clazz.getDeclaredMethod("usage")
            method.isAccessible = true

            usages.add(method.invoke(null) as Component)
        } catch (e: ReflectiveOperationException) {
            if (e !is NoSuchMethodException) {
                exception("Could not register action ${clazz.simpleName}", e)
            }
        }
    }

    @Suppress("SameParameterValue")
    private fun register(plugin: String, id: String, clazz: Class<*>, supplier: Action.Supplier<Player>) {
        if (Bukkit.getPluginManager().isPluginEnabled(plugin)) {
            info("Registering [$id] because $plugin is on the server")
            register(id, clazz, supplier)
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