package me.gabytm.minecraft.arcanevouchers.message

import me.gabytm.minecraft.arcanevouchers.functions.audience
import me.gabytm.minecraft.arcanevouchers.functions.mini
import net.kyori.adventure.text.Component
import org.bukkit.command.CommandSender
import org.bukkit.configuration.file.YamlConfiguration
import java.util.*
import java.util.regex.Pattern

enum class Lang(private val path: String, vararg stringPlaceholders: String) {

    GENERAL__NO_PERMISSION("general.noPermission"),

    GIVE__SENDER("give.sender", "{amount}", "{receiver}", "{voucher}");

    private val placeholders: MutableList<Pattern> = mutableListOf()
    private lateinit var component: Component

    init {
        for (placeholder in stringPlaceholders) {
            placeholders.add(Pattern.compile(Pattern.quote(placeholder), Pattern.CASE_INSENSITIVE))
        }
    }

    fun send(receiver: CommandSender, values: List<Any> = emptyList()) {
        if (values.size != placeholders.size) {
            throw IllegalArgumentException("")
        }

        var message = component

        for ((value, placeholder) in values.zip(placeholders)) {
            message = message.replaceText { it.match(placeholder).replacement(value.toString()) }
        }

        receiver.audience().sendMessage(message)
    }

    companion object {

        val VALUES: EnumSet<Lang> = EnumSet.allOf(Lang::class.java)

        fun load(lang: YamlConfiguration) {
            for (it in VALUES) {
                val message = lang.getString(it.path) ?: continue
                it.component = message.mini()
            }
        }

    }

}