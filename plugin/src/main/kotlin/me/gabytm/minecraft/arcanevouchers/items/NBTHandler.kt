package me.gabytm.minecraft.arcanevouchers.items

import com.google.gson.JsonObject
import me.gabytm.minecraft.arcanevouchers.ArcaneVouchers
import me.gabytm.minecraft.arcanevouchers.Constant
import me.gabytm.minecraft.arcanevouchers.functions.exception
import java.io.IOException
import java.nio.file.Files

/**
 * Better NBT support for vouchers by storing it on a .json file
 */
class NBTHandler(plugin: ArcaneVouchers) {

    private val jsonFilePath = plugin.dataFolder.toPath().resolve("vouchers-nbt.json")
    private lateinit var json: JsonObject

    init {
        if (!Files.exists(jsonFilePath)) {
            try {
                Files.createFile(this.jsonFilePath)
            } catch (e: IOException) {
                exception("Could not create $jsonFilePath", e)
            }
        }

        this.load()
    }

    /**
     * Load the NBT string from file
     */
    fun load() {
        Files.newBufferedReader(this.jsonFilePath).use {
            this.json = Constant.GSON.fromJson(it, JsonObject::class.java)
        }
    }

    /**
     * Get the NBT string of a voucher by its id
     * @param id voucher id
     * @return NBT string if found otherwise `null`
     */
    fun getNbt(id: String): String? {
        return this.json.get(id)?.toString()
    }

}