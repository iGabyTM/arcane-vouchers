package me.gabytm.minecraft.arcanevouchers.commands.commands

import dev.triumphteam.gui.builder.gui.PaginatedBuilder
import dev.triumphteam.gui.builder.item.ItemBuilder
import dev.triumphteam.gui.components.util.GuiFiller
import dev.triumphteam.gui.guis.PaginatedGui
import me.gabytm.minecraft.arcanevouchers.ArcaneVouchers
import me.gabytm.minecraft.arcanevouchers.Constant
import me.gabytm.minecraft.arcanevouchers.ServerVersion
import me.gabytm.minecraft.arcanevouchers.commands.ArcaneCommand
import me.gabytm.minecraft.arcanevouchers.functions.audience
import me.gabytm.minecraft.arcanevouchers.functions.processArguments
import me.gabytm.minecraft.arcanevouchers.functions.removeItalic
import me.gabytm.minecraft.arcanevouchers.message.Lang
import me.gabytm.minecraft.arcanevouchers.voucher.VoucherManager
import me.mattstudios.mf.annotations.Permission
import me.mattstudios.mf.annotations.SubCommand
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.*
import net.kyori.adventure.text.JoinConfiguration
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Material
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class ListCommand(plugin: ArcaneVouchers) : ArcaneCommand(plugin) {

    private val listGui = ListGui(plugin.voucherManager)

    @Permission(Constant.Permission.ADMIN)
    @SubCommand("list")
    fun onCommand(sender: CommandSender, args: Array<String>) {
        if (plugin.settings.useGuiForList && sender is Player) {
            listGui.open(sender, args.copyOfRange(1, args.size).joinToString().processArguments())
            return
        }

        val vouchers = plugin.voucherManager.getVouchers()

        if (vouchers.isEmpty()) {
            Lang.LIST__NO_VOUCHERS.send(sender)
            return
        }

        val joinConfiguration = JoinConfiguration.builder()
            .prefix(Lang.LIST__PREFIX.format(vouchers.size))
            .separator(Lang.LIST__SEPARATOR.format() ?: space())
            .suffix(Lang.LIST__SUFFIX.format())
            .build()

        val components = mutableListOf<Component>()

        for (voucher in vouchers) {
            components.add(Lang.LIST__VOUCHER.format(voucher.id) ?: text(voucher.id))
        }

        sender.audience().sendMessage(join(joinConfiguration, components))
    }

    private class ListGui(private val voucherManager: VoucherManager) {

        private val pageSize = 36

        @Suppress("DEPRECATION")
        private val fillerItem = if (ServerVersion.IS_LEGACY) {
            ItemBuilder.from(ItemStack(Material.valueOf("STAINED_GLASS_PANE"), 1, 7))
        } else {
            ItemBuilder.from(Material.GRAY_STAINED_GLASS_PANE)
        }.name(empty()).asGuiItem()

        private val previousPageButton = ItemBuilder.skull()
            .texture("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZjg0ZjU5NzEzMWJiZTI1ZGMwNThhZjg4OGNiMjk4MzFmNzk1OTliYzY3Yzk1YzgwMjkyNWNlNGFmYmEzMzJmYyJ9fX0=")
            .name(text("Previous Page", NamedTextColor.RED).removeItalic())
            .asGuiItem { (it.inventory.holder as PaginatedGui).previous() }

        private val nextPageButton = ItemBuilder.skull()
            .texture("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTYzMzlmZjJlNTM0MmJhMThiZGM0OGE5OWNjYTY1ZDEyM2NlNzgxZDg3ODI3MmY5ZDk2NGVhZDNiOGFkMzcwIn19fQ==")
            .name(text("Next Page", NamedTextColor.GREEN).removeItalic())
            .asGuiItem { (it.inventory.holder as PaginatedGui).next() }

        fun open(player: Player, args: Array<String>) {
            val gui = PaginatedBuilder()
                .title(text("Vouchers!", NamedTextColor.LIGHT_PURPLE))
                .rows(6)
                .pageSize(pageSize)
                .disableAllInteractions()
                .create()

            val filler = GuiFiller(gui)
            filler.fillTop(fillerItem)
            filler.fillBottom(fillerItem)

            val vouchers = voucherManager.getVouchers()

            for (voucher in vouchers) {
                val item = ItemBuilder.from(voucherManager.createVoucherItem(player, voucher, 1, args))
                    .lore {
                        it.add(empty())
                        it.add(text("Left Click to get 1x ${voucher.id}").removeItalic())
                        it.add(text("Left Click to get 64x ${voucher.id}").removeItalic())
                        it.add(empty())
                    }
                    .asGuiItem {
                        val amount = if (it.isLeftClick) 1 else 64
                        voucherManager.giveVoucher(player, voucher, amount, args)
                    }

                gui.addItem(item)
            }

            if (vouchers.size > pageSize) {
                gui.setItem(6, 4, previousPageButton)
                gui.setItem(6, 6, nextPageButton)
            }

            gui.open(player)
        }

    }

}