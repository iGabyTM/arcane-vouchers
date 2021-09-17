package me.gabytm.minecraft.arcanevouchers.functions

import java.util.logging.Level
import java.util.logging.Logger

fun Logger.error(message: String, exception: Throwable) {
    log(Level.SEVERE, message, exception)
}