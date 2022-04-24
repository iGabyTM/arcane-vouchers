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
import net.kyori.adventure.text.format.TextDecoration
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
        componentParser.registerDefaults(Player::class.java)

        // Default actions

        // Commands
        usages.add(Component.text("+ Commands:", NamedTextColor.LIGHT_PURPLE))
        register<ConsoleCommandAction> { ConsoleCommandAction(it, handler) }
        register<PlayerCommandAction> { PlayerCommandAction(it, handler) }
        //-----

        // Economy
        usages.add(Component.text("+ Economy:", NamedTextColor.LIGHT_PURPLE))
        register<AddExpAction> { AddExpAction(it, handler) }
        register<ItemAction> { ItemAction(it, handler, plugin.itemCreator) }
        register<VoucherAction> { VoucherAction(it, handler, plugin.voucherManager) }
        //-----

        // Message
        usages.add(Component.text("+ Message:", NamedTextColor.LIGHT_PURPLE))
        register<BossBarAction> { BossBarAction(it, handler) }
        register<ChatAction> { ChatAction(it, handler) }
        register<MessageAction> { MessageAction(it, handler) }
        //-----

        // Other
        usages.add(Component.text("+ Other:", NamedTextColor.LIGHT_PURPLE))
        register<DataAction> { DataAction(it, handler) }
        register<EffectAction> { EffectAction(it, handler) }
        register<SoundAction> { SoundAction(it, handler) }
        //-----

        // Actions with external dependencies

        // Crates
        usages.add(Component.text("+ Crates:", NamedTextColor.DARK_PURPLE))
        registerIfEnabled<GiveCrateReloadedKeyAction>("CrateReloaded") { GiveCrateReloadedKeyAction(it, handler) }
        //-----

        // Vault
        isEnabled("Vault") {
            usages.add(Component.text("+ Vault:", NamedTextColor.DARK_PURPLE))

            if (setupEconomy()) {
                register<AddMoneyAction> { AddMoneyAction(it, handler, economy) }
            }

            if (setupPermission()) {
                register<PermissionAction> { PermissionAction(it, handler, permission) }
            }
        }
        //-----

        info("Loaded ${actions.size()} actions: ${actions.rowKeySet().sorted().joinToString(", ")}")

        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            placeholderManager.register(PlaceholderAPIProvider())
        } else {
            // '%player_name' is the only placeholder replaced in case PlaceholderAPI is not installed
            placeholderManager.register(PlayerNamePlaceholderProvider())
        }

        // Join all usages in one component
        actionsMessage = Component.join(JoinConfiguration.newlines(), usages)
            .append(Component.newline())
            .append(Component.text("     ${actions.size()} actions available", NamedTextColor.GRAY, TextDecoration.ITALIC))
        usages.clear()
    }

    private inline fun <reified A : ArcaneAction> register(supplier: Action.Supplier<Player>) {
        val clazz = A::class.java

        try {
            val identifier = clazz.getDeclaredField("ID").apply { isAccessible = true }.get(null)
            register(Player::class.java, identifier as String, supplier)

            val usage = clazz.getDeclaredField("USAGE").apply { isAccessible = true }.get(null)
            usages.add(usage as Component)
        } catch (e: ReflectiveOperationException) {
            exception("An exception occurred while registering ${clazz.simpleName}", e)
        }
    }

    @Suppress("SameParameterValue")
    private inline fun <reified A : ArcaneAction> registerIfEnabled(plugin: String, supplier: Action.Supplier<Player>) {
        isEnabled(plugin) { register<A>(supplier) }
    }

    private fun isEnabled(plugin: String, action: () -> Unit) {
        if (Bukkit.getPluginManager().isPluginEnabled(plugin)) {
            action()
        }
    }

    private fun setupEconomy(): Boolean {
        val rsp = Bukkit.getServicesManager().getRegistration(Economy::class.java) ?: return false
        this.economy = rsp.provider
        return true
    }

    private fun setupPermission(): Boolean {
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