package me.gabytm.minecraft.arcanevouchers.functions

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextDecoration

/**
 * Remove the [italic][TextDecoration.ITALIC] of a component, used for [item][org.bukkit.inventory.ItemStack] name and lore
 * @return a [Component] without [italic][TextDecoration.ITALIC]
 */
fun Component.removeItalic(): Component = decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)