package me.gabytm.minecraft.arcanevouchers.listeners

import de.tr7zw.nbtapi.NBTItem
import me.gabytm.minecraft.arcanevouchers.ArcaneVouchers
import me.gabytm.minecraft.arcanevouchers.Constant
import me.gabytm.minecraft.arcanevouchers.functions.item
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.inventory.PrepareItemCraftEvent

class DisableActionsListener(private val plugin: ArcaneVouchers) : Listener {

    @EventHandler
    fun PrepareItemCraftEvent.onEvent() {
        if (!plugin.settings.disableCrafting) {
            return
        }

        for (item in inventory.matrix) {
            if (item == null || item.type == Material.AIR) {
                continue
            }

            if (NBTItem(item).hasKey(Constant.NBT.VOUCHER_COMPOUND)) {
                inventory.result = null
                return
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun BlockPlaceEvent.onEvent() {
        isCancelled = NBTItem(player.item()).hasKey(Constant.NBT.VOUCHER_COMPOUND)
    }

}