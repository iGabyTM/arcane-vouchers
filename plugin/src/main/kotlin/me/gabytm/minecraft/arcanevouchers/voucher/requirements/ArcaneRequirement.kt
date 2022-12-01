package me.gabytm.minecraft.arcanevouchers.voucher.requirements

import me.clip.placeholderapi.PlaceholderAPI
import me.gabytm.minecraft.arcanevouchers.actions.ArcaneAction
import me.gabytm.minecraft.arcanevouchers.actions.ArcaneActionManager
import me.gabytm.minecraft.util.requirements.Requirement
import org.bukkit.Bukkit
import org.bukkit.entity.Player

abstract class ArcaneRequirement(
    name: String,
    optional: Boolean,
    negated: Boolean,
    @Transient private val failActions: List<ArcaneAction>,
    @Transient private val actionManager: ArcaneActionManager
) : Requirement<Player>(name, optional, negated) {

    protected fun papi(player: Player?, string: String): String {
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            return PlaceholderAPI.setPlaceholders(player, string)
        }

        return string
    }

    override fun onFail(t: Player?) {
        if (t != null && failActions.isNotEmpty()) {
            actionManager.executeActions(t, failActions)
        }
    }

}