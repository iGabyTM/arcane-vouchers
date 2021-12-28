package me.gabytm.minecraft.arcanevouchers.actions.implementations.message

import me.gabytm.minecraft.arcanevouchers.functions.audience
import net.kyori.adventure.audience.Audience
import org.bukkit.Bukkit
import org.bukkit.entity.Player

class Broadcast private constructor(private val conditions: List<Condition<*>>) {

    fun broadcast(player: Player, action: (Audience) -> Unit) {
        if (conditions.isEmpty()) {
            Bukkit.getOnlinePlayers().forEach { action(it.audience()) }
            return
        }

        Bukkit.getOnlinePlayers()
            .filter {
                if (it.uniqueId != player.uniqueId) true else conditions.all { condition ->
                    condition.check(
                        player,
                        it
                    )
                }
            }
            .forEach { action(it.audience()) }
    }

    companion object {

        private val EVERYONE: Broadcast = Broadcast(emptyList())

        fun parse(string: String?): Broadcast {
            if (string == null || string.trim().isEmpty()) {
                return EVERYONE
            }

            val conditions = mutableListOf<Condition<*>>()

            for (condition in string.split(",")) {
                when {
                    condition.equals("world", true) -> conditions.add(WorldCondition())
                    condition.startsWith("permission", true) -> {
                        conditions.add(PermissionCondition(condition.split(":")[1]))
                    }
                }
            }

            return Broadcast(conditions)
        }

    }

    private abstract class Condition<T>(protected val t: T) {

        abstract fun check(player: Player, other: Player): Boolean

    }

    private class WorldCondition : Condition<Byte>(0) {

        override fun check(player: Player, other: Player): Boolean = other.world.uid == player.world.uid

    }

    private class PermissionCondition(t: String) : Condition<String>(t) {

        override fun check(player: Player, other: Player): Boolean = other.hasPermission(t)

    }

}