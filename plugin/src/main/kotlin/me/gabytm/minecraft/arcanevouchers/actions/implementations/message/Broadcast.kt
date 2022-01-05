package me.gabytm.minecraft.arcanevouchers.actions.implementations.message

import me.gabytm.minecraft.arcanevouchers.Constant
import me.gabytm.minecraft.arcanevouchers.functions.audience
import net.kyori.adventure.audience.Audience
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Player
import kotlin.math.pow
import kotlin.math.sqrt

class Broadcast private constructor(private val conditions: List<Condition<*>>, private val noBroadcast: Boolean = false) {

    fun broadcast(player: Player, action: (Audience) -> Unit) {
        if (noBroadcast) {
            action(player.audience())
        } else {
            getPlayers(player).forEach { action(it.audience()) }
        }
    }

    fun getPlayers(player: Player): Collection<Player> {
        if (noBroadcast) {
            return setOf(player)
        }

        if (conditions.isEmpty()) {
            return Bukkit.getOnlinePlayers()
        }

        return Bukkit.getOnlinePlayers()
            .filter {
                if (it.uniqueId == player.uniqueId) {
                    true
                } else {
                    conditions.all { condition -> condition.check(player, it) }
                }
            }
    }

    companion object {

        private val EVERYONE: Broadcast = Broadcast(emptyList())
        private val NO_BROADCAST: Broadcast = Broadcast(emptyList(), true)

        fun parse(string: String?): Broadcast {
            if (string == null) {
                return NO_BROADCAST
            }

            if (string == "*" || string.trim().isEmpty()) {
                return EVERYONE
            }

            val conditions = mutableListOf<Condition<*>>()

            for (condition in string.split(Constant.Separator.COMMA)) {
                when {
                    condition.startsWith("permission", true) -> {
                        PermissionCondition(condition.split(Constant.Separator.COLON)[1])
                    }

                    condition.startsWith("radius", true) -> {
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

        /**
         * `sqrt((Ax - Bx)^2 + (Az - Bz)^2)`
         */
        private fun distance(a: Location, b: Location): Double {
            return sqrt((a.x - b.x).pow(2.0) + (a.z - b.z).pow(2.0))
        }

        override fun check(player: Player, other: Player): Boolean {
            return other.world.uid == player.world.uid && distance(other.location, player.location) <= t
        }

    }

    private class WorldCondition : Condition<Unit>(Unit) {

        override fun check(player: Player, other: Player): Boolean = other.world.uid == player.world.uid

    }

}