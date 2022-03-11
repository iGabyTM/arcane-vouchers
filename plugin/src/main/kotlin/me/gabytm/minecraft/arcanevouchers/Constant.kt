package me.gabytm.minecraft.arcanevouchers

import me.gabytm.minecraft.arcanevouchers.functions.exception
import me.gabytm.minecraft.arcanevouchers.functions.info
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Color
import java.lang.reflect.Modifier

object Constant {

    val MINI = MiniMessage.miniMessage()

    lateinit var NAMED_COLORS: Map<String, Color> private set

    init {
        try {
            NAMED_COLORS = Color::class.java.declaredFields
                .filter { Modifier.isStatic(it.modifiers) && it.type == Color::class.java }
                .associate { it.name to (it.get(null) as Color) }
        } catch (e: SecurityException) {
            exception("Could not get colors", e)
        }
    }

    object NBT {

        // The main compound
        const val VOUCHER_COMPOUND = "ArcaneVouchers"

        // Arguments compound
        const val ARGUMENTS_COMPOUND = "Arguments"

        // Receiver's name
        const val RECEIVER_UUID = "Receiver"

        // Voucher's name
        const val VOUCHER_NAME = "Name"

    }

    object Permission {

        private const val BASE = "arcanevouchers"

        const val ADMIN = "$BASE.admin"

        const val COOLDOWN_BYPASS_ALL = "$BASE.cooldownbypass"
        const val COOLDOWN_BYPASS = "$COOLDOWN_BYPASS_ALL.%s"

        const val LIMIT_BYPASS = "$BASE.limitbypass"
        const val LIMIT_BYPASS_ALL_GLOBAL = "$LIMIT_BYPASS.*.global"
        const val LIMIT_BYPASS_ALL_PERSONAL = "$LIMIT_BYPASS.*.personal"
        const val LIMIT_BYPASS_GLOBAL = "$LIMIT_BYPASS.%s.global"
        const val LIMIT_BYPASS_PERSONAL = "$LIMIT_BYPASS.%s.personal"

    }

    object Separator {

        val COLON = Regex(":")
        val COMMA = Regex(",")
        val NEW_LINE = Regex("\\[[nN]]")
        val SEMICOLON = Regex(";")
        val SPACE = Regex(" ")

    }

}