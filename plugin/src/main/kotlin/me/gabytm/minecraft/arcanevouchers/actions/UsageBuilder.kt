package me.gabytm.minecraft.arcanevouchers.actions

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.*
import net.kyori.adventure.text.JoinConfiguration.newlines
import net.kyori.adventure.text.event.HoverEventSource
import net.kyori.adventure.text.format.NamedTextColor.*

internal class UsageBuilder(
    private val actionName: String
) {

    private var hover: HoverEventSource<*>? = null
    private val optionalProperties = mutableListOf<Component>()
    private val requiredProperties = mutableListOf<Component>()
    private val optionalArguments = mutableListOf<Component>()
    private val requiredArguments = mutableListOf<Component>()

    fun hover(hover: HoverEventSource<*>): UsageBuilder {
        this.hover = hover
        return this
    }

    fun property(property: UsageElement, condition: Boolean = true): UsageBuilder {
        if (!condition) {
            return this
        }

        if (property.required) {
            requiredProperties
        } else {
            optionalProperties
        }.add(property.create())

        return this
    }

    fun argument(argument: UsageElement, condition: Boolean = true): UsageBuilder {
        if (!condition) {
            return this
        }

        if (argument.required) {
            requiredArguments
        } else {
            optionalArguments
        }.add(argument.create())

        return this
    }

    fun build(): Component {
        val builder = text()
        val hasRequiredProperties = requiredProperties.isNotEmpty()
        val hasOptionalProperties = optionalProperties.isNotEmpty()

        if (hasRequiredProperties || hasOptionalProperties) {
            builder.append(text("- ")).append(text("{", GRAY))

            if (hasRequiredProperties) {
                builder.append(text("Req", RED).hoverEvent(join(newlines(), requiredProperties)))
            }

            // Add space between properties
            if (hasRequiredProperties && hasOptionalProperties) {
                builder.append(space())
            }

            if (hasOptionalProperties) {
                builder.append(text("Opt", GREEN).hoverEvent(join(newlines(), optionalProperties)))
            }

            builder.append(text("} ", GRAY)).append(text("[$actionName]").hoverEvent(hover))
        } else {
            builder.append(text("- ")).append(text("[$actionName]").hoverEvent(hover))
        }

        if (requiredArguments.isNotEmpty()) {
            builder.append(text(" Req", RED).hoverEvent(join(newlines(), requiredArguments)))
        }

        if (optionalArguments.isNotEmpty()) {
            builder.append(text(" Opt", GREEN).hoverEvent(join(newlines(), optionalArguments)))
        }

        return builder.build()
    }

    companion object {

        val BOOLEAN = text("Boolean", DARK_GREEN)
        val INTEGER = text("Integer", GOLD)
        val LIST = text("List", AQUA)
        val STRING = text("String", GREEN)
        val TICKS = text("Ticks", YELLOW)

        fun element(name: String): UsageElement = UsageElement(name)

    }

    class UsageElement internal constructor(private val name: String) {

        private lateinit var type: Component
        private lateinit var description: String
        private var default: Any? = null

        internal var required: Boolean = false; private set

        fun type(type: Component): UsageElement {
            this.type = type
            return this
        }

        fun description(description: String): UsageElement {
            this.description = description
            return this
        }

        fun default(default: Any?): UsageElement {
            this.default = default
            return this
        }

        fun required(): UsageElement {
            this.required = true
            return this
        }

        @Suppress("DuplicatedCode")
        fun create(): Component {
            // name: Type, description (default: value)
            // unbreakable: Boolean, whether the item is unbreakable (default: false)
            val builder = text()
                .append(text("$name: ", type.color()))
                .append(type)
                .append(text(", $description"))

            if (default != null) {
                builder.append(text(" (default: $default)", GRAY))
            }

            return builder.build()
        }

    }

}