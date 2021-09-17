package me.gabytm.minecraft.arcanevouchers

import com.google.gson.GsonBuilder
import net.kyori.adventure.text.minimessage.MiniMessage

object Constant {

    val GSON = GsonBuilder().create()
    val MINI = MiniMessage.markdown()
    val SEPARATOR = Regex(";")

    object Nbt {

        // The main compound
        const val VOUCHER_COMPOUND = "ArcaneVouchers"
        // Arguments compound
        const val ARGUMENTS_COMPOUND = "Arguments"
        // Receiver's name
        const val RECEIVER_NAME = "Receiver"
        // Voucher's name
        const val VOUCHER_NAME = "Name"

    }

}