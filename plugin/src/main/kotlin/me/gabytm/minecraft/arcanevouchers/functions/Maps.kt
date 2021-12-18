package me.gabytm.minecraft.arcanevouchers.functions

operator fun Map<String, String>.component1(): Array<String> {
    return this.keys.toTypedArray()
}

operator fun Map<String, String>.component2(): Array<String> {
    return this.values.toTypedArray()
}

fun <K, V> MutableMap<K, V>.add(k: K, v: V): MutableMap<K, V> {
    this[k] = v
    return this
}