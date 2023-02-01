package me.gabytm.minecraft.arcanevouchers.io.serializers.bukkit

import com.google.gson.*
import org.bukkit.Location
import java.lang.reflect.Type

class LocationSerializer : JsonSerializer<Location> {

    override fun serialize(src: Location?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
        if (src == null) {
            return JsonNull.INSTANCE
        }

        val element = JsonObject()
        element.addProperty("world", src.world?.name)
        element.addProperty("x", src.x)
        element.addProperty("y", src.y)
        element.addProperty("z", src.z)
        return element
    }

    companion object {

        val INSTANCE = LocationSerializer()

    }

}