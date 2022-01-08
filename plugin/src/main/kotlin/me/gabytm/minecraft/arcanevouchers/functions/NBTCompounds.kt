package me.gabytm.minecraft.arcanevouchers.functions

import de.tr7zw.nbtapi.NBTCompound
import me.gabytm.minecraft.arcanevouchers.Constant
import me.gabytm.minecraft.arcanevouchers.ServerVersion
import java.util.*

fun NBTCompound.setReceiverUUID(uuid: UUID) {
    if (ServerVersion.HAS_UUID_NBT_COMPOUND) {
        setUUID(Constant.NBT.RECEIVER_UUID, uuid)
    } else {
        setString(Constant.NBT.RECEIVER_UUID, uuid.toString())
    }
}

fun NBTCompound.getReceiverUUID(): UUID? {
    return if (ServerVersion.HAS_UUID_NBT_COMPOUND) {
        getUUID(Constant.NBT.RECEIVER_UUID)
    } else {
        val uuidString = getString(Constant.NBT.RECEIVER_UUID) ?: return null
        UUID.fromString(uuidString)
    }
}