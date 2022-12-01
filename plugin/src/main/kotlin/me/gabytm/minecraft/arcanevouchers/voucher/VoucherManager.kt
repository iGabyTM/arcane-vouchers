package me.gabytm.minecraft.arcanevouchers.voucher

import de.tr7zw.nbtapi.NBTItem
import dev.triumphteam.gui.builder.item.ItemBuilder
import me.gabytm.minecraft.arcanevouchers.ArcaneVouchers
import me.gabytm.minecraft.arcanevouchers.Constant.NBT
import me.gabytm.minecraft.arcanevouchers.cooldown.CooldownManager
import me.gabytm.minecraft.arcanevouchers.functions.*
import me.gabytm.minecraft.arcanevouchers.limit.LimitManager
import me.gabytm.minecraft.arcanevouchers.message.Lang
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.*

class VoucherManager(private val plugin: ArcaneVouchers) {

    private val loadedVouchers = mutableMapOf<String, Voucher>()

    private val confirmationGui = ConfirmationGui(plugin)
    val cooldownManager = CooldownManager(plugin)
    val limitManager = LimitManager(plugin)

    fun load() {
        this.confirmationGui.load()
        loadedVouchers.clear()

        val vouchersSection = plugin.vouchersConfig.getSection("vouchers") ?: kotlin.run {
            warning("Could not find the 'vouchers' section")
            return
        }

        for (it in vouchersSection.getValues(false).keys) {
            if (it.contains(' ')) {
                warning("Invalid voucher name '$it', remove any spaces and invalid characters")
                continue
            }

            val section = vouchersSection.getConfigurationSection(it) ?: continue
            this.loadedVouchers[it] = Voucher.from(section, plugin.actionManager, plugin.itemCreator, plugin.requirementProcessor)
        }

        info("Loaded ${this.loadedVouchers.size} voucher(s): ${this.loadedVouchers.keys.joinToString(", ")}")
    }

    fun getVoucherIds(): Set<String> = this.loadedVouchers.keys.toSet()

    fun getVouchers(): Set<Voucher> = Collections.unmodifiableSet(this.loadedVouchers.values.toSet())

    fun getVoucher(id: String): Voucher? = this.loadedVouchers[id]

    fun giveVoucher(player: Player, id: String, amount: Int, args: Array<String>) {
        val voucher = this.getVoucher(id) ?: throw IllegalArgumentException("Unknown voucher $id")
        this.giveVoucher(player, voucher, amount, args)
    }

    fun createVoucherItem(player: Player, voucher: Voucher, amount: Int, args: Array<String>): ItemStack {
        val argsMap = args.toArgsMap()
        argsMap[Lang.Placeholder.RECEIVER] = player.name

        val nbt = NBTItem(voucher.item.clone())

        // Set the arguments and player's name inside the item
        val compound = nbt.getCompound(NBT.VOUCHER_COMPOUND)
        val argumentsCompound = compound.getCompound(NBT.ARGUMENTS_COMPOUND)

        argsMap.entries.forEach { (key, value) -> argumentsCompound.setString(key, value) }
        compound.setReceiverUUID(player.uniqueId)
        // -----

        if (argsMap.isEmpty()) {
            val item = nbt.item
            item.amount = amount
            return item;
        }

        val item = ItemBuilder.from(nbt.item).amount(amount)

        // Replace the arguments on item name and lore
        val placeholders = argsMap.keys.toTypedArray()
        val values = argsMap.values.toTypedArray()

        if (voucher.itemName.isNotBlank()) {
            item.name(voucher.itemName.replace(placeholders, values).mini(true))
        }

        if (voucher.itemLore.isNotEmpty()) {
            item.lore(voucher.itemLore.map { it.replace(placeholders, values).mini(true) })
        }

        return item.build()
    }

    fun giveVoucher(player: Player, voucher: Voucher, amount: Int, args: Array<String>) {
        val argsMap = args.toArgsMap()
        val item = createVoucherItem(player, voucher, amount, args)

        // Add the voucher to player's inventory and drop the leftovers on the ground
        player.giveItems(item)

        // Send the message to the player and also add the {amount} placeholder
        val audience = player.audience()
        voucher.settings.messages.receiveMessage.send(audience, argsMap.add(Lang.Placeholder.AMOUNT, amount.toString()))
        voucher.settings.sounds.receiveSound.play(audience)
    }

    fun openConfirmation(player: Player, voucher: Voucher, voucherItem: ItemStack, args: MutableMap<String, String>, isBulk: Boolean) {
        this.confirmationGui.open(player, voucher, voucherItem, args, isBulk)
    }

}