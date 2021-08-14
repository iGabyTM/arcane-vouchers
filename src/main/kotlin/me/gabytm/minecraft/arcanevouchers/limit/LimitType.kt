package me.gabytm.minecraft.arcanevouchers.limit

enum class LimitType {

    GLOBAL,
    PERSONAL;

    companion object {

        fun getLimit(string: String): LimitType {
            return when (string.uppercase()) {
                "GLOBAL" -> GLOBAL
                else -> PERSONAL
            }
        }

    }

}