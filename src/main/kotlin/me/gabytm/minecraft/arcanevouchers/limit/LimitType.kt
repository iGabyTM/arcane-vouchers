package me.gabytm.minecraft.arcanevouchers.limit

enum class LimitType {

    GLOBAL,
    PERSONAL,
    NONE;

    companion object {

        fun getLimit(string: String): LimitType {
            return when (string.uppercase()) {
                "GLOBAL" -> GLOBAL
                "PERSONAL" -> PERSONAL
                else -> NONE
            }
        }

    }

}