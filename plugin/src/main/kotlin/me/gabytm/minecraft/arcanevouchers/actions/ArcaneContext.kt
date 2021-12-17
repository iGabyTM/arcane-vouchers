package me.gabytm.minecraft.arcanevouchers.actions

import me.gabytm.util.actions.actions.Context
import org.bukkit.entity.Player

class ArcaneContext(val actions: List<ArcaneAction>, data: MutableMap<String, Any>) : Context<Player>(mutableListOf(), data)