package me.gabytm.minecraft.arcanevouchers

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import net.kyori.adventure.text.minimessage.MiniMessage

object Constant {

    val GSON: Gson = GsonBuilder().create()
    val MINI = MiniMessage.get()

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