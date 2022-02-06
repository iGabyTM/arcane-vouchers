package me.gabytm.minecraft.arcanevouchers.voucher.settings

import me.gabytm.minecraft.arcanevouchers.functions.replace
import me.gabytm.minecraft.arcanevouchers.functions.warning
import java.util.regex.Pattern
import java.util.regex.PatternSyntaxException

/**
 * Class that holds options for things like [me.gabytm.minecraft.arcanevouchers.voucher.VoucherSettings.Worlds.whitelistedWorlds]
 */
class OptionHolder(
    private val stringOptions: Set<String> = emptySet(),
    private val regexOptions: Set<Pattern> = emptySet()
) {

    val isEmpty: Boolean = this.stringOptions.isEmpty() && this.regexOptions.isEmpty()

    /**
     * Check if any value of a [Set] matches any of the options
     */
    fun any(set: Set<String>, placeholders: Array<String>, values: Array<String>): Boolean {
        // Replace the placeholders from each option only once
        val stringOptions = this.stringOptions.map { it.replace(placeholders, values) }

        for (stringOption in stringOptions) {
            // Return true of the set contains one of the string options
            if (set.contains(stringOption)) {
                return true
            }
        }

        for (regexOption in regexOptions) {
            val matcher = regexOption.matcher("")

            for (it in set) {
                // Return true if one of the regex options matches an element of the set
                if (matcher.reset(it).find()) {
                    return true
                }
            }
        }

        return false
    }

    /**
     * Check if a string matches any of the options
     */
    fun any(string: String, placeholders: Array<String>, values: Array<String>): Boolean {
        for (stringOption in stringOptions) {
            // Return true of the string equals one of the string options
            if (string == stringOption.replace(placeholders, values)) {
                return true
            }
        }

        for (regexOption in regexOptions) {
            // Return true if one of the regex options matches the string
            if (regexOption.matcher(string).find()) {
                return true
            }
        }

        return false
    }

    companion object {

        private const val REGEX_PREFIX = "regex:"
        private val REGEX_PREFIX_PATTERN = Regex(REGEX_PREFIX, RegexOption.IGNORE_CASE)

        /* no-op */
        val NO_OP = OptionHolder()

        fun from(list: List<String>): OptionHolder {
            val stringOptions = mutableSetOf<String>()
            val regexOptions = mutableSetOf<Pattern>()

            for (it in list) {
                if (it.startsWith(REGEX_PREFIX, true)) {
                    val regex = it.split(REGEX_PREFIX_PATTERN)[0]

                    try {
                        regexOptions.add(Pattern.quote(regex).toPattern())
                    } catch (e: PatternSyntaxException) {
                        warning("Could not parse regex '$regex': ${e.message}")
                    }
                } else {
                    stringOptions.add(it)
                }
            }

            return if (stringOptions.isEmpty() && regexOptions.isEmpty()) NO_OP else OptionHolder(stringOptions, regexOptions)
        }

    }

}