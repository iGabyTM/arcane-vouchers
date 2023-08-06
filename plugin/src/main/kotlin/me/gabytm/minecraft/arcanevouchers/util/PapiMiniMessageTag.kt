package me.gabytm.minecraft.arcanevouchers.util

import me.gabytm.minecraft.arcanevouchers.functions.papi
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.Context
import net.kyori.adventure.text.minimessage.tag.Tag
import net.kyori.adventure.text.minimessage.tag.resolver.ArgumentQueue
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.entity.Player

class PapiMiniMessageTag(private val player: Player) : TagResolver {

    override fun resolve(name: String, arguments: ArgumentQueue, ctx: Context): Tag? {
        val placeholder = arguments.popOr("Placeholder argument not found").value()
        val parsed = "%$placeholder%".papi(player)

        // If the placeholder returns itself it means it is invalid
        if (parsed == "%$placeholder%") {
            return null
        }

        if (parsed.isEmpty()) {
            return Tag.inserting(Component.empty())
        }

        if (LEGACY_REGEX.containsMatchIn(parsed)) {
            return Tag.inserting(LegacyComponentSerializer.legacyAmpersand().deserialize(parsed.replace('§', '&')))
        }

        return Tag.inserting(ctx.deserialize(parsed));
    }

    override fun has(name: String): Boolean = name == NAME

    companion object {

        private const val NAME: String = "papi"
        private val LEGACY_REGEX: Regex = Regex("[&|§][0-9A-FK-OR]", RegexOption.IGNORE_CASE)

    }

}