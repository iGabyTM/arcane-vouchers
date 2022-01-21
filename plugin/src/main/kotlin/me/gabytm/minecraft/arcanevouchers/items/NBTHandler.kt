package me.gabytm.minecraft.arcanevouchers.items

import me.gabytm.minecraft.arcanevouchers.ArcaneVouchers
import me.gabytm.minecraft.arcanevouchers.Constant
import java.nio.file.Files

/**
 * Better NBT support for vouchers by storing it on a .json file
 */
class NBTHandler(plugin: ArcaneVouchers) {

    private val nbtFilePath = plugin.dataFolder.toPath().resolve("vouchers-nbt.txt")
    private val nbt = mutableMapOf<String, String>()

    init {
        if (!Files.exists(this.nbtFilePath)) {
            plugin.saveResource("vouchers-nbt.txt", false)
        }

        this.load()
    }

    /**
     * Load the NBT string from file
     */
    fun load() {
        this.nbt.clear()

        Files.newBufferedReader(this.nbtFilePath).use { reader ->
            reader.lines()
                .map { it.split(Constant.Separator.SPACE, 2) }
                .filter { it.size == 2 }
                .forEach { this.nbt[it[0]] = it[1] }
        }
    }

    /**
     * Get the NBT string of a voucher by its id
     * @param id voucher id
     * @return NBT string if found otherwise `null`
     */
    fun getNbt(id: String): String? {
        return this.nbt[id]
    }

}