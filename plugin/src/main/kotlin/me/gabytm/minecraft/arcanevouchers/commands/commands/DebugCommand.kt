package me.gabytm.minecraft.arcanevouchers.commands.commands

import com.google.common.io.CharStreams
import com.google.gson.*
import me.gabytm.minecraft.arcanevouchers.ArcaneVouchers
import me.gabytm.minecraft.arcanevouchers.Constant
import me.gabytm.minecraft.arcanevouchers.commands.ArcaneCommand
import me.gabytm.minecraft.arcanevouchers.functions.exception
import me.gabytm.minecraft.arcanevouchers.io.serializers.java.PatternSerializer
import me.gabytm.minecraft.arcanevouchers.io.serializers.adventure.TextComponentSerializer
import me.gabytm.minecraft.arcanevouchers.io.serializers.bukkit.LocationSerializer
import me.gabytm.minecraft.arcanevouchers.message.implementations.ActionBarMessage
import me.gabytm.minecraft.arcanevouchers.message.implementations.ChatMessage
import me.gabytm.minecraft.arcanevouchers.message.implementations.TitleMessage
import me.gabytm.minecraft.arcanevouchers.voucher.Voucher
import me.gabytm.minecraft.arcanevouchers.voucher.settings.VoucherSettings
import me.mattstudios.mf.annotations.Permission
import me.mattstudios.mf.annotations.SubCommand
import net.kyori.adventure.text.TextComponent
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.command.CommandSender
import java.io.IOException
import java.io.InputStreamReader
import java.lang.reflect.Type
import java.net.HttpURLConnection
import java.net.URL
import java.nio.file.Files
import java.nio.file.StandardOpenOption
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.regex.Pattern

// Inspired from https://github.com/PlaceholderAPI/PlaceholderAPI/blob/52119682f35af2e49d499f2a83b06c00dab2f913/src/main/java/me/clip/placeholderapi/commands/impl/local/CommandDump.java
class DebugCommand(plugin: ArcaneVouchers) : ArcaneCommand(plugin) {

    companion object {
        val GSON: Gson = GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .registerTypeAdapter(TextComponent::class.java, TextComponentSerializer())
            .registerTypeAdapter(Voucher::class.java, VoucherSerializer.INSTANCE)
            .registerTypeAdapter(Pattern::class.java, PatternSerializer())
            .registerTypeAdapter(Location::class.java, LocationSerializer.INSTANCE)
            // Messages
            .registerTypeAdapter(ActionBarMessage::class.java, ActionBarMessage.Serializer.INSTANCE)
            .registerTypeAdapter(ChatMessage::class.java, ChatMessage.Serializer.INSTANCE)
            .registerTypeAdapter(TitleMessage::class.java, TitleMessage.Serializer.INSTANCE)
            .create()
    }

    private val dateFormat = DateTimeFormatter
        .ofLocalizedDate(FormatStyle.FULL)
        .withLocale(Locale.US)
        .withZone(ZoneId.of("UTC"))

    private fun postDump(content: String): CompletableFuture<String?> {
        return CompletableFuture.supplyAsync {
            return@supplyAsync try {
                val connection = URL("https://paste.helpch.at/documents").openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "text/plain; charset=utf-8")
                connection.setRequestProperty("User-Agent", "ArcaneVouchers v${plugin.description.version}")
                connection.doOutput = true

                connection.connect()

                connection.outputStream.use { it.write(content.toByteArray(Charsets.UTF_8)) }
                connection.inputStream.use {
                    val json = CharStreams.toString(InputStreamReader(it, Charsets.UTF_8))
                    GSON.fromJson(json, JsonObject::class.java).get("key").asString
                }
            } catch (e: IOException) {
                exception("", e)
                null
            }
        }
    }

    @Permission(Constant.Permission.ADMIN)
    @SubCommand("debug")
    fun onCommand(sender: CommandSender) {
        val date = dateFormat.format(Instant.now())

        val content = buildString {
            val plugins = Bukkit.getPluginManager().plugins
            val pluginNamesLength = plugins.maxOf { it.name.length }

            append("----- Created @ $date -----\n\n")

            append("▶ Versions:\n")
            append("Plugin: ${plugin.description.version}\n")
            append("Server: ${Bukkit.getBukkitVersion()}/${Bukkit.getVersion()}\n")
            append("Java: ${System.getProperty("java.version")}\n\n")

            append("▶ Plugins (${plugins.size}):\n")
            for (plugin in plugins.sortedBy { it.name }) {
                val status = if (plugin.isEnabled) "+" else "-"
                val version = plugin.description.version
                val authors = plugin.description.authors.joinToString(", ")

                append("$status %-${pluginNamesLength}s [$version by $authors]\n".format(plugin.name))
            }

            append("\n")
            val vouchers = plugin.voucherManager.getVouchers().sortedBy { it.id }

            append("▶ Vouchers (${vouchers.size}):\n")
            vouchers.forEach {
                append("${it.id}: ${GSON.toJson(it)}\n")
                plugin.itemCreator.nbtHandler.getNbt(it.id)?.let { nbt -> append("NBT: $nbt\n") }
                append("\n")
            }
        }

        sender.sendMessage("Uploading, please wait...")
        postDump(content).whenComplete { key, _ ->
            run {
                if (key == null) {
                    val debugFilePath = plugin.dataFolder.toPath().resolve("debug-$date.log")

                    Files.write(debugFilePath, content.toByteArray(), StandardOpenOption.CREATE, StandardOpenOption.WRITE)
                    sender.sendMessage("Could not upload the debug to paste.helpch.at, but it was saved to $debugFilePath")
                    return@run
                }

                sender.sendMessage("Your debug can be found @ https://paste.helpch.at/$key.log")
            }
        }
    }

    internal class VoucherSerializer : JsonSerializer<Voucher> {

        override fun serialize(src: Voucher?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
            if (src == null) {
                return JsonNull.INSTANCE
            }

            val actions = JsonArray()
            src.actions.map { action ->
                val properties = action.meta.properties.map { (key, value) -> "$key=$value" }.joinToString()
                val name = action.getName()
                val args = action.meta.rawData
                "{$properties} [$name] $args"
            }.forEach { actions.add(it) }

            val obj = JsonObject()
            obj.add("settings", GSON.toJsonTree(src.settings, VoucherSettings::class.java))
            obj.add("actions", actions)
            return obj
        }

        companion object {
            val INSTANCE = VoucherSerializer()
        }

    }

}