package me.gabytm.minecraft.arcanevouchers.actions.tags

import me.gabytm.util.actions.actions.Context
import me.gabytm.util.actions.components.Component
import me.gabytm.util.actions.placeholders.PlaceholderManager
import org.bukkit.entity.Player
import java.util.*

class ChanceTag(stringValue: String, placeholderManager: PlaceholderManager) : Component<Player, String>(
    stringValue,
    placeholderManager
) {

    private val elements: Set<Pair<String, String>>
    private val default: String

    init {
        val test = stringValue
            .split(",")
            .map { it.split("=") }
            .map { it[0] to it[1] }
            .toMutableSet()

        val defaultValue = test.firstOrNull { it.first == "default" }

        if (defaultValue != null) {
            test.remove(defaultValue)
            default = defaultValue.second
            elements = test.toSet()
        } else {
            elements = test.toSet()
            default = ""
        }
    }

    override fun parse(player: Player, context: Context<Player>): String {
        val set2 = elements.map {
            val chance = placeholderManager.replace(player, it.first, context).toDoubleOrNull() ?: throw IllegalArgumentException("(chance tag) '${it.first}' is not a number")
            chance to placeholderManager.replace(player, it.second, context)
        }
        val chance = RANDOM.nextDouble(101.0)
        val valid = set2.filter { chance <= it.first }

        return if (valid.isEmpty()) {
            default
        } else if (valid.size == 1) {
            valid.first().second
        } else {
            valid.elementAt(RANDOM.nextInt(valid.size)).second
        }
    }

    @Suppress("unused")
    companion object {

        private const val ID: String = "chance"
        private val RANDOM = SplittableRandom()

    }

}