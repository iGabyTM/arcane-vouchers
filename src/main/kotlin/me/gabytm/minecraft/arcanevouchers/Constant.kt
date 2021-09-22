package me.gabytm.minecraft.arcanevouchers

import com.google.gson.GsonBuilder
import net.kyori.adventure.text.minimessage.MiniMessage

object Constant {

    val GSON = GsonBuilder().create()
    val MINI = MiniMessage.markdown()
    val SEPARATOR = Regex(";")

    object NBT {

        // The main compound
        const val VOUCHER_COMPOUND = "ArcaneVouchers"

        // Arguments compound
        const val ARGUMENTS_COMPOUND = "Arguments"

        // Receiver's name
        const val RECEIVER_NAME = "Receiver"

        // Voucher's name
        const val VOUCHER_NAME = "Name"

    }

    object Permission {

        private const val BASE = "arcanevouchers"

        const val LIMIT_BYPASS = "$BASE.limitbypass"
        const val LIMIT_BYPASS_ALL_GLOBAL = "$LIMIT_BYPASS.*.global"
        const val LIMIT_BYPASS_ALL_PERSONAL = "$LIMIT_BYPASS.*.personal"
        const val LIMIT_BYPASS_GLOBAL = "$LIMIT_BYPASS.%s.global"
        const val LIMIT_BYPASS_PERSONAL = "$LIMIT_BYPASS.%s.personal"

    }

}