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
    // A requirement can not be 'optional' and 'required' at the same time
    // So we consider a requirement 'required' if it is not 'optional'
) : Requirement<Player>(name, !optional, optional, negated) {

    protected fun papi(player: Player?, string: String): String {
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            return PlaceholderAPI.setPlaceholders(player, string)
        }

        return string
    }

    override fun onFail(player: Player?) {
        if (player != null && failActions.isNotEmpty()) {
            actionManager.executeActions(player, failActions)
        }
    }

}