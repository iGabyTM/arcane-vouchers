package me.gabytm.minecraft.arcanevouchers.actions

import me.gabytm.minecraft.arcanevouchers.ArcaneVouchers
import me.gabytm.minecraft.arcanevouchers.actions.implementations.command.ConsoleCommandAction
import me.gabytm.minecraft.arcanevouchers.actions.implementations.command.PlayerCommandAction
import me.gabytm.minecraft.arcanevouchers.actions.implementations.message.MessageAction
import me.gabytm.minecraft.arcanevouchers.actions.implementations.other.AddMoneyAction
import me.gabytm.minecraft.arcanevouchers.permission.PermissionHandler
import me.gabytm.util.actions.actions.Action
import me.gabytm.util.actions.actions.implementations.DataAction
import me.gabytm.util.actions.spigot.actions.SpigotActionManager
import net.milkbowl.vault.economy.Economy
import org.bukkit.Bukkit
import org.bukkit.entity.Player

/*
class ArcaneActionManager(
    private val plugin: ArcaneVouchers
) : ActionManager(SpigotTaskProcessor(plugin), 100.0) {

    private val handler = PermissionHandler()

    init {
        register(Player::class.java, "data") { DataAction(it) }
        register(Player::class.java, "console") { ConsoleCommandAction(it, handler) }
        register(Player::class.java, "player") { PlayerCommandAction(it, handler) }
    }

    fun run(
        player: Player,
        actions: List<ArcaneAction>,
        async: Boolean,
        data: MutableMap<String, Any> = mutableMapOf()
    ) {
        val context = Context(actions, data)

        for (action in context) {
            val meta = action.meta

            if (meta.hasChance() && random.nextDouble(0.0, maxChance) > meta.chance) {
                continue
            }

            //action as ArcaneAction
            val task = { action.run(player, context) }

            if (meta.hasDelay()) {
                if (async) {
                    taskProcessor.runAsync(task, meta.delay)
                } else {
                    taskProcessor.runSync(task, meta.delay)
                }

                continue
            }

            if (async) {
                taskProcessor.runAsync(task)
            } else {
                taskProcessor.runSync(task)
            }
        }
    }

}*/
class ArcaneActionManager(plugin: ArcaneVouchers) : SpigotActionManager(plugin) {

    private val handler = PermissionHandler()

    private lateinit var economy: Economy

    init {
        registerDefaults(Player::class.java)

        // Commands
        register("console") { ConsoleCommandAction(it, handler) }
        register("player") { PlayerCommandAction(it, handler) }

        // Message
        register("message") { MessageAction(it, handler) }

        // Other
        if (setupEconomy()) {
            register("addmoney") { AddMoneyAction(it, handler, economy) }
        }
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

}