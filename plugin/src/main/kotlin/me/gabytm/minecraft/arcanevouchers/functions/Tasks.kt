package me.gabytm.minecraft.arcanevouchers.functions

import me.gabytm.minecraft.arcanevouchers.ArcaneVouchers
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin

private val plugin = JavaPlugin.getPlugin(ArcaneVouchers::class.java)

fun async(task: Runnable) = Bukkit.getScheduler().runTaskAsynchronously(plugin, task)

fun sync(task: Runnable) {
    if (Bukkit.isPrimaryThread()) {
        task.run()
    } else {
        Bukkit.getScheduler().runTask(plugin, task)
    }
}