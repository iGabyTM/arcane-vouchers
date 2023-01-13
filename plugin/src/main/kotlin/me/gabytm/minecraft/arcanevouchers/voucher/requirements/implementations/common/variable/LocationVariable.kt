package me.gabytm.minecraft.arcanevouchers.voucher.requirements.implementations.common.variable

import me.gabytm.minecraft.arcanevouchers.Constant
import me.gabytm.minecraft.arcanevouchers.functions.papi
import me.gabytm.minecraft.arcanevouchers.functions.warning
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Player

data class LocationVariable(
    val string: String
) : Variable<Location>(
    transformer@{ player ->
        // world;x;y;z
        val parts = string.papi(player).split(Constant.Separator.SEMICOLON)

        if (parts.size != 4) {
            return@transformer null
        }

        val world = Bukkit.getWorld(parts[0]) ?: return@transformer null
        val x = parts[1].toDoubleOrNull() ?: return@transformer null
        val y = parts[2].toDoubleOrNull() ?: return@transformer null
        val z = parts[3].toDoubleOrNull() ?: return@transformer null
        return@transformer Location(world, x, y, z)
    }
) {

    override fun warn(player: Player?, requirement: String) {
        warning("[$requirement] '$string' (${string.papi(player)}) is not a valid location (expected format: world;x;y;z)")
    }

}
