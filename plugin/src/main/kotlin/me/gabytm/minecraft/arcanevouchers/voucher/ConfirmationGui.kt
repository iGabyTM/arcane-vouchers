package me.gabytm.minecraft.arcanevouchers.voucher

import dev.triumphteam.gui.guis.Gui
import dev.triumphteam.gui.guis.GuiItem
import me.gabytm.minecraft.arcanevouchers.ArcaneVouchers
import me.gabytm.minecraft.arcanevouchers.functions.mini
import me.gabytm.minecraft.arcanevouchers.functions.warning
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import kotlin.math.max

class ConfirmationGui(private val plugin: ArcaneVouchers) {

    private var size = 3
    private lateinit var title: Component

    private lateinit var confirmationButton: Button
    private lateinit var cancelButton: FunctionalButton
    private val otherButtons = mutableSetOf<FunctionalButton>()

    private fun ConfigurationSection.getSlots(path: String): List<Int> {
        val finalPath = "$path.slots"

        return if (isList(finalPath)) {
            getIntegerList(finalPath)
        } else {
            listOf(getInt(finalPath))
        }
    }

    fun load() {
        val config = plugin.config.getConfigurationSection("confirmationGui") ?: kotlin.run {
            warning("Could not find 'confirmationGui' section in config.yml")
            return
        }

        val itemCreator = plugin.itemCreator

        this.size = max(config.getInt("size", 3), 1)
        this.title = (config.getString("title", "") ?: "").mini()

        this.confirmationButton = Button(
            itemCreator.create(false, config.getConfigurationSection("items.buttons.confirmation"), Material.PAPER),
            config.getSlots("items.buttons.confirmation")
        )

        val cancelItem = itemCreator.create(false, config.getConfigurationSection("items.buttons.cancel"), Material.BARRIER)
        this.cancelButton = FunctionalButton(
            GuiItem(cancelItem) { it.whoClicked.closeInventory() },
            config.getSlots("items.buttons.cancel")
        )

        this.otherButtons.clear()

        val otherItemsSection = config.getConfigurationSection("items.other") ?: return

        for (it in otherItemsSection.getKeys(false)) {
            val item = itemCreator.create(false, otherItemsSection.getConfigurationSection(it), Material.STONE)
            this.otherButtons.add(FunctionalButton(GuiItem(item), otherItemsSection.getSlots(it)))
        }
    }

    fun open(player: Player, voucher: Voucher, voucherItem: ItemStack, args: MutableMap<String, String>, isBulk: Boolean) {
        val gui = Gui.gui()
            .title(this.title)
            .rows(this.size)
            .disableAllInteractions()
            .create()

        gui.setDefaultClickAction { it.isCancelled = true }
        this.otherButtons.forEach { gui.setItem(it.slots, it.item) }

        gui.setItem(this.cancelButton.slots, this.cancelButton.item)
        gui.setItem(this.confirmationButton.slots, GuiItem(this.confirmationButton.item) {
            it.whoClicked.closeInventory()
            voucher.redeem(player, voucherItem, args, plugin, isBulk)
        })
        gui.open(player)
    }

    private data class Button(val item: ItemStack, val slots: List<Int>)

    private data class FunctionalButton(val item: GuiItem, val slots: List<Int>)

}