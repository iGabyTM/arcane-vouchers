package me.gabytm.minecraft.arcanevouchers.io.serializers.java

import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import java.lang.reflect.Type
import java.util.regex.Pattern

class PatternSerializer : JsonSerializer<Pattern> {

    override fun serialize(src: Pattern?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
        return JsonPrimitive(src?.pattern())
    }

}