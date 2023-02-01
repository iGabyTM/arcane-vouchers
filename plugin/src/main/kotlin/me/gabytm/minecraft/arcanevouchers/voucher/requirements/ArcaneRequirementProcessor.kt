package me.gabytm.minecraft.arcanevouchers.voucher.requirements

import me.gabytm.minecraft.arcanevouchers.actions.ArcaneActionManager
import me.gabytm.minecraft.arcanevouchers.voucher.requirements.implementations.location.DistanceRequirementFactory
import me.gabytm.minecraft.arcanevouchers.voucher.requirements.implementations.number.NumberRequirementFactory
import me.gabytm.minecraft.arcanevouchers.voucher.requirements.implementations.string.StringRequirementFactory
import me.gabytm.minecraft.util.requirements.RequirementsList
import me.gabytm.minecraft.util.requirements.bukkit.BukkitRequirementProcessor
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.Player

class ArcaneRequirementProcessor(private val actionManager: ArcaneActionManager) : BukkitRequirementProcessor<Player>() {

    private val requirementFactories = mutableSetOf<ArcaneRequirementFactory<out ArcaneRequirement>>()

    init {
        // Location
        registerFactory(DistanceRequirementFactory())

        registerFactory(NumberRequirementFactory())
        registerFactory(StringRequirementFactory())
    }

    private fun createRequirement(source: ConfigurationSection): ArcaneRequirement? {
        return requirementFactories.firstOrNull { it.matches(source) }?.create(source, actionManager)
    }

    fun registerFactory(factory: ArcaneRequirementFactory<out ArcaneRequirement>) {
        requirementFactories.add(factory)
    }

    fun processRequirements(root: ConfigurationSection?): RequirementsList<ArcaneRequirement, Player> {
        if (root == null) {
            return RequirementsList(emptyList())
        }

        val minimumRequirements = root.getInt("minimumRequirements", RequirementsList.ALL_REQUIREMENTS)
        val requirementsSection = root.getConfigurationSection("list") ?: return RequirementsList(emptyList())
        val requirements = mutableListOf<ArcaneRequirement>()

        for (name in requirementsSection.getKeys(false)) {
            val section = requirementsSection.getConfigurationSection(name) ?: continue
            val requirement = createRequirement(section) ?: continue
            requirements.add(requirement)
        }

        return RequirementsList(requirements, minimumRequirements)
    }

}