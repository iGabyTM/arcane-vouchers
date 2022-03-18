package me.gabytm.minecraft.arcanevouchers.actions

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.join
import net.kyori.adventure.text.Component.text
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

    private fun createDescription(name: String, type: Component, description: String, default: Any? = null): Component {
        val builder = text()
            .append(text("$name: ", type.color()))
            .append(type)
            .append(text(", $description"))

        if (default != null) {
            builder.append(text(" (default: $default)", GRAY))
        }

        return builder.build()
    }

    fun hover(hover: HoverEventSource<*>): UsageBuilder {
        this.hover = hover
        return this
    }

    fun property(property: UsageElement, condition: Boolean = true): UsageBuilder {
        if (!condition) {
            return this
        }

        val component = property.create()

        if (property.required) {
            requiredProperties.add(component)
        } else {
            optionalProperties.add(component)
        }

        return this
    }

    fun argument(argument: UsageElement, condition: Boolean = true): UsageBuilder {
        if (!condition) {
            return this
        }

        val component = argument.create()

        if (argument.required) {
            requiredArguments.add(component)
        } else {
            optionalArguments.add(component)
        }

        return this
    }

    fun optional(
        isArgument: Boolean,
        name: String,
        type: Component,
        description: String,
        default: Any? = null,
        condition: Boolean = true
    ): UsageBuilder {
        if (!condition) {
            return this
        }

        if (isArgument) {
            optionalArguments.add(createDescription(name, type, description, default))
        } else {
            optionalProperties.add(createDescription(name, type, description, default))
        }

        return this
    }

    fun required(
        isArgument: Boolean,
        name: String,
        type: Component,
        description: String,
        default: Any? = null,
        condition: Boolean = true
    ): UsageBuilder {
        if (!condition) {
            return this
        }

        if (isArgument) {
            optionalArguments.add(createDescription(name, type, description, default))
        } else {
            optionalProperties.add(createDescription(name, type, description, default))
        }

        return this
    }

    fun build(): Component {
        val builder = text()

        if (requiredProperties.isNotEmpty() || optionalProperties.isNotEmpty()) {
            builder.append(text("- ")).append(text("{", GRAY))

            if (requiredProperties.isNotEmpty()) {
                builder.append(text("<required", RED).hoverEvent(join(newlines(), requiredProperties)))
            }

            if (optionalProperties.isNotEmpty()) {
                builder.append(text("(optional)", GREEN).hoverEvent(join(newlines(), optionalProperties)))
            }

            builder.append(text("} ", GRAY)).append(text("[$actionName]").hoverEvent(hover))
        } else {
            builder.append(text("- ")).append(text("[$actionName]").hoverEvent(hover))
        }

        if (requiredArguments.isNotEmpty()) {
            builder.append(text(" <required>", RED).hoverEvent(join(newlines(), requiredArguments)))
        }

        if (optionalArguments.isNotEmpty()) {
            builder.append(text(" (optional)", GREEN).hoverEvent(join(newlines(), optionalArguments)))
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