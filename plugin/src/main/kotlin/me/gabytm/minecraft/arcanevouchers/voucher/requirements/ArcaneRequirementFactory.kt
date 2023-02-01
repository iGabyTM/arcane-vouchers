package me.gabytm.minecraft.arcanevouchers.voucher.requirements

import me.gabytm.minecraft.arcanevouchers.actions.ArcaneActionManager
import me.gabytm.minecraft.util.requirements.RequirementFactory
import org.bukkit.configuration.ConfigurationSection

abstract class ArcaneRequirementFactory<R : ArcaneRequirement> : RequirementFactory<ConfigurationSection, R> {

    override fun create(source: ConfigurationSection): R? = null

    abstract fun create(source: ConfigurationSection, actionManager: ArcaneActionManager): R?

}