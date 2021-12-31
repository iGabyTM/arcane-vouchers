package me.gabytm.minecraft.arcanevouchers.message

import me.gabytm.minecraft.arcanevouchers.functions.audience
import me.gabytm.minecraft.arcanevouchers.functions.mini
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.ComponentLike
import org.bukkit.command.CommandSender
import org.bukkit.configuration.file.YamlConfiguration
import java.util.*
import java.util.regex.Pattern

enum class Lang(private val path: String, vararg stringPlaceholders: String) {

    PREFIX("prefix"),

    // Messages used pretty much for all commands and not only
    GENERAL__INVALID__NUMBER__INTEGER("general.invalid.number.integer", "{input}"),
    GENERAL__INVALID__NUMBER__LONG("general.invalid.number.long", "{input}"),

    GENERAL__NO_PERMISSION("general.noPermission"),

    GENERAL__UNKNOWN__PLAYER("general.unknown.player", "{input}"),
    GENERAL__UNKNOWN__VOUCHER("general.unknown.voucher", "{input}"),
    //-----

    // Messages for the give command
    GIVE__SENDER("give.sender", "{amount}", "{receiver}", "{voucher}"),
    GIVE__USAGE("give.usage"),
    //-----

    // Messages for the limit command
    LIMIT__TYPE_NONE("limit.typeNone", "{voucher}"),
    LIMIT__USAGE("limit.usage"),

    LIMIT__CHECK__GLOBAL("limit.check.global.message", "{limit}", "{usages}","{voucher}"),
    LIMIT__CHECK__GLOBAL__REQUIRE_PLAYER("limit.check.global.requirePlayer"),
    LIMIT__CHECK__PERSONAL("limit.check.personal.message", "{limit}", "{player}", "{usages}", "{voucher}"),

    LIMIT__MODIFY__VALUE("limit.modify.value"),
    LIMIT__MODIFY__GLOBAL__CONFIRMATION(
        "limit.modify.global.confirmation",
        "{new_limit}", "{value}", "{voucher}"
    ),
    LIMIT__MODIFY__PERSONAL__CONFIRMATION(
        "limit.modify.player.confirmation",
        "{new_limit}", "{player}", "{value}", "{voucher}"
    ),
    LIMIT__MODIFY__PERSONAL__REQUIRE_PLAYER("limit.modify.personal.requirePlayer"),

    LIMIT__SET__NEW_VALUE("limit.set.newValue"),
    LIMIT__SET__GLOBAL__CONFIRMATION("limit.set.global.confirmation", "{new_value}", "{voucher}"),
    LIMIT__SET__PERSONAL__CONFIRMATION("limit.set.personal.confirmation", "{new_value}", "{player}", "{voucher}"),
    LIMIT__SET__PERSONAL__REQUIRE_PLAYER("limit.set.personal.requirePlayer"),
    //-----

    // Messages for the list command
    LIST__NO_VOUCHERS("list.noVouchers"),
    LIST__PREFIX("list.prefix", "{amount}"),
    LIST__SEPARATOR("list.separator"),
    LIST__SUFFIX("list.suffix"),
    LIST__VOUCHER("list.voucher", "{voucher}"),
    //-----

    RELOAD("reload")
    ;

    private val placeholders: MutableList<Pattern> = mutableListOf()
    private var component: Component? = null

    init {
        for (placeholder in stringPlaceholders) {
            placeholders.add(Pattern.compile(Pattern.quote(placeholder), Pattern.CASE_INSENSITIVE))
        }
    }

    fun isEmpty(): Boolean = this.component == null || this.component == Component.empty()

    fun format(values: List<Any> = emptyList()): Component? {
        if (component == null) {
            return null
        }

        if (values.size != placeholders.size) {
            throw IllegalArgumentException("Expected ${placeholders.size} values for message $name but got only ${values.size}")
        }

        var message = component ?: return null

        for ((value, placeholder) in values.zip(placeholders)) {
            message = message.replaceText {
                it.match(placeholder)

                if (value is ComponentLike) {
                    it.replacement(value)
                } else {
                    it.replacement(value.toString())
                }
            }
        }

        return message.replaceText { it.match(PREFIX_PATTERN).replacement(PREFIX.component ?: Component.empty()) }
    }

    fun format(first: Any): Component? = format(listOf(first))

    fun format(first: Any, second: Any): Component? = format(listOf(first, second))

    fun send(receiver: CommandSender, values: List<Any> = emptyList()) {
        val message = format(values) ?: return
        receiver.audience().sendMessage(message)
    }

    fun send(receiver: CommandSender, first: Any) {
        send(receiver, listOf(first))
    }

    fun send(receiver: CommandSender, first: Any, second: Any) {
        send(receiver, listOf(first, second))
    }

    fun send(receiver: CommandSender, first: Any, second: Any, third: Any) {
        send(receiver, listOf(first, second, third))
    }

    fun send(receiver: CommandSender, first: Any, second: Any, third: Any, forth: Any) {
        send(receiver, listOf(first, second, third, forth))
    }

    companion object {

        private val PREFIX_PATTERN: Pattern = Pattern.compile("\\{prefix}", Pattern.CASE_INSENSITIVE)
        val VALUES: EnumSet<Lang> = EnumSet.allOf(Lang::class.java)

        fun load(lang: YamlConfiguration) {
            for (it in VALUES) {
                it.component = lang.getString(it.path)?.mini()
            }
        }

    }

}