package me.gabytm.minecraft.arcanevouchers.commands.commands

import me.gabytm.minecraft.arcanevouchers.ArcaneVouchers
import me.gabytm.minecraft.arcanevouchers.Constant
import me.gabytm.minecraft.arcanevouchers.commands.ArcaneCommand
import me.gabytm.minecraft.arcanevouchers.functions.audience
import me.gabytm.minecraft.arcanevouchers.message.Lang
import me.mattstudios.mf.annotations.Permission
import me.mattstudios.mf.annotations.SubCommand
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.space
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.JoinConfiguration
import org.bukkit.command.CommandSender

class ListCommand(plugin: ArcaneVouchers) : ArcaneCommand(plugin) {

    @Permission(Constant.Permission.ADMIN)
    @SubCommand("list")
    fun onCommand(sender: CommandSender) {
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

        sender.audience().sendMessage(Component.join(joinConfiguration, components))
    }

}