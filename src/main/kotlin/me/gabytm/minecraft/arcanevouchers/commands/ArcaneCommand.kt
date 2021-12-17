package me.gabytm.minecraft.arcanevouchers.commands

import me.gabytm.minecraft.arcanevouchers.ArcaneVouchers
import me.mattstudios.mf.base.CommandBase

open class ArcaneCommand(plugin: ArcaneVouchers) : CommandBase(
    plugin.config.getString("settings.command") ?: "arcanevouchers",
    plugin.config.getStringList("settings.alias")
)