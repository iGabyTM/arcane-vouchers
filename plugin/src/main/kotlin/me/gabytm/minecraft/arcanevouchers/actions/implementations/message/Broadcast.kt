package me.gabytm.minecraft.arcanevouchers.actions.implementations.message

import me.gabytm.minecraft.arcanevouchers.Constant
import me.gabytm.minecraft.arcanevouchers.functions.audience
import net.kyori.adventure.audience.Audience
import org.bukkit.Bukkit
import org.bukkit.entity.Player

class Broadcast private constructor(private val conditions: List<Condition<*>>) {

    fun broadcast(player: Player, action: (Audience) -> Unit) {
        getPlayers(player).forEach { action(it.audience()) }
    }

    fun getPlayers(player: Player): Collection<Player> {
        if (conditions.isEmpty()) {
            return Bukkit.getOnlinePlayers()
        }

        return Bukkit.getOnlinePlayers()
            .filter {
                if (it.uniqueId != player.uniqueId) {
                    true
                } else {
                    conditions.all { condition -> condition.check(player, it) }
                }
            }
    }

    companion object {

        private val EVERYONE: Broadcast = Broadcast(emptyList())

        fun parse(string: String?): Broadcast {
            if (string == null || string.equals("*") || string.trim().isEmpty()) {
                return EVERYONE
            }

            val conditions = mutableListOf<Condition<*>>()

            for (condition in string.split(",")) {
                when {
                    condition.startsWith("permission", true) -> {
                        PermissionCondition(condition.split(Constant.Separator.COLON)[1])
                    }

                    condition.equals("radius", true) -> {
                        condition.split(Constant.Separator.COLON)[1].toDoubleOrNull()?.let { RadiusCondition(it) }
                    }

                    condition.equals("world", true) -> WorldCondition()

                    else -> null
                }?.let { conditions.add(it) }
            }

            return Broadcast(conditions)
        }

    }

    private abstract class Condition<T>(protected val t: T) {

        abstract fun check(player: Player, other: Player): Boolean

    }

    private class PermissionCondition(t: String) : Condition<String>(t) {

        override fun check(player: Player, other: Player): Boolean = other.hasPermission(t)

    }

    private class RadiusCondition(t: Double) : Condition<Double>(t) {

        override fun check(player: Player, other: Player): Boolean =
            other.location.distanceSquared(player.location) <= t

    }

    private class WorldCondition : Condition<Unit>(Unit) {

        override fun check(player: Player, other: Player): Boolean = other.world.uid == player.world.uid

    }

}