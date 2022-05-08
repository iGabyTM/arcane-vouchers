package me.gabytm.minecraft.arcanevouchers.listeners

import de.tr7zw.nbtapi.NBTItem
import me.gabytm.minecraft.arcanevouchers.ArcaneVouchers
import me.gabytm.minecraft.arcanevouchers.Constant
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.inventory.PrepareItemCraftEvent
import org.bukkit.inventory.ItemStack

class DisableActionsListener(private val plugin: ArcaneVouchers) : Listener {

    private fun ItemStack?.isVoucher(): Boolean {
        return (this != null && type != Material.AIR) && NBTItem(this).hasKey(Constant.NBT.VOUCHER_COMPOUND)
    }

    @EventHandler
    fun PrepareItemCraftEvent.onEvent() {
        if (plugin.settings.disableCrafting && inventory.matrix.any { it.isVoucher() }) {
            inventory.result = null
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun BlockPlaceEvent.onEvent() {
        isCancelled = itemInHand.isVoucher()
    }

}