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
    GENERAL__INVALID__NUMBER__INTEGER("general.invalid.number.integer", Placeholder.INPUT),
    GENERAL__INVALID__NUMBER__LONG("general.invalid.number.long", Placeholder.INPUT),

    GENERAL__NO_PERMISSION("general.noPermission"),

    GENERAL__UNKNOWN__PLAYER("general.unknown.player", Placeholder.INPUT),
    GENERAL__UNKNOWN__VOUCHER("general.unknown.voucher", Placeholder.INPUT),
    //-----

    // Messages for the give command
    GIVE__ALL("give.all", Placeholder.AMOUNT, Placeholder.VOUCHER),
    GIVE__SENDER("give.sender", Placeholder.AMOUNT, Placeholder.RECEIVER, Placeholder.VOUCHER),
    GIVE__USAGE("give.usage"),
    //-----

    HELP("help", Placeholder.VERSION),

    // Messages for the list command
    LIST__NO_VOUCHERS("list.noVouchers"),
    LIST__PREFIX("list.prefix", Placeholder.AMOUNT),
    LIST__SEPARATOR("list.separator"),
    LIST__SUFFIX("list.suffix"),
    LIST__VOUCHER("list.voucher", Placeholder.VOUCHER),
    //-----

    RELOAD("reload"),

    // Messages for the usages command
    USAGES__TYPE_NONE("usages.typeNone", Placeholder.VOUCHER),
    USAGES__USAGE("usages.usage"),

    USAGES__CHECK__GLOBAL("usages.check.global.message", Placeholder.LIMIT, Placeholder.USAGES, Placeholder.VOUCHER),
    USAGES__CHECK__PERSONAL(
        "usages.check.personal.message",
        Placeholder.LIMIT, Placeholder.PLAYER, Placeholder.USAGES, Placeholder.VOUCHER
    ),
    USAGES__CHECK__PERSONAL__REQUIRE_PLAYER("usages.check.personal.requirePlayer"),

    USAGES__MODIFY__USAGE("usages.modify.usage"),
    USAGES__MODIFY__GLOBAL__CONFIRMATION(
        "usages.modify.global.confirmation",
        Placeholder.NEW_LIMIT, Placeholder.VALUE, Placeholder.VOUCHER
    ),
    USAGES__MODIFY__PERSONAL__CONFIRMATION(
        "usages.modify.player.confirmation",
        Placeholder.NEW_LIMIT, Placeholder.PLAYER, Placeholder.VALUE, Placeholder.VOUCHER
    ),
    USAGES__MODIFY__PERSONAL__REQUIRE_PLAYER("usages.modify.personal.requirePlayer"),

    USAGES__SET__USAGE("usages.set.usage"),
    USAGES__SET__GLOBAL__CONFIRMATION(
        "usages.set.global.confirmation",
        Placeholder.NEW_VALUE, Placeholder.VOUCHER
    ),
    USAGES__SET__PERSONAL__CONFIRMATION(
        "usages.set.personal.confirmation",
        Placeholder.NEW_VALUE, Placeholder.PLAYER, Placeholder.VOUCHER
    ),
    USAGES__SET__PERSONAL__REQUIRE_PLAYER("usages.set.personal.requirePlayer")
    //-----
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

    private object Placeholder {

        const val AMOUNT: String = "{amount}"

        const val INPUT: String = "{input}"

        const val LIMIT: String = "{limit}"

        const val NEW_LIMIT: String = "{new_limit}"
        const val NEW_VALUE: String = "{new_value}"

        const val PLAYER: String = "{player}"

        const val RECEIVER: String = "{receiver}"

        const val USAGES: String = "{usages}"

        const val VALUE: String = "{value}"
        const val VERSION: String = "{version}"
        const val VOUCHER: String = "{voucher}"

    }

}