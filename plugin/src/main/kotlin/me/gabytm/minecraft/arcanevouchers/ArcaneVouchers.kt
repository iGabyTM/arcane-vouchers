package me.gabytm.minecraft.arcanevouchers

import me.gabytm.minecraft.arcanevouchers.actions.ArcaneActionManager
import me.gabytm.minecraft.arcanevouchers.commands.CommandManager
import me.gabytm.minecraft.arcanevouchers.compatibility.CompatibilityHandler
import me.gabytm.minecraft.arcanevouchers.config.Config
import me.gabytm.minecraft.arcanevouchers.files.FileHandler
import me.gabytm.minecraft.arcanevouchers.functions.color
import me.gabytm.minecraft.arcanevouchers.items.ItemCreator
import me.gabytm.minecraft.arcanevouchers.listeners.DisableActionsListener
import me.gabytm.minecraft.arcanevouchers.listeners.VoucherUseListener
import me.gabytm.minecraft.arcanevouchers.message.Lang
import me.gabytm.minecraft.arcanevouchers.other.ResourcesHandler
import me.gabytm.minecraft.arcanevouchers.voucher.VoucherManager
import me.gabytm.minecraft.arcanevouchers.voucher.requirements.ArcaneRequirementProcessor
import net.kyori.adventure.platform.bukkit.BukkitAudiences
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin

class ArcaneVouchers : JavaPlugin() {

    lateinit var settings: Settings private set

    lateinit var audiences: BukkitAudiences private set
    lateinit var compatibilityHandler: CompatibilityHandler private set

    lateinit var vouchersConfig: Config private set
    private lateinit var langFile: Config

    lateinit var actionManager: ArcaneActionManager private set
    lateinit var itemCreator: ItemCreator private set
    lateinit var voucherManager: VoucherManager private set
    lateinit var requirementProcessor: ArcaneRequirementProcessor private set

    private fun sendLogo() {
        with (description) {
            sequenceOf(
                "&5 _____   _____   ",
                "&5|  _  | |  |  |  &fArcaneVouchers &dv$version &fby &d${authors.joinToString()} &7(${ServerVersion.CURRENT})",
                "&5|     | |  |  |  &7$description",
                "&5|__|__|  \\___/  "
            ).forEach { line -> Bukkit.getConsoleSender().sendMessage(line.color()) }
        }
    }

    override fun onEnable() {
        sendLogo()

        if (ServerVersion.IS_VERY_OLD) {
            logger.severe("1.7 is not supported! If you are on another version and see this, please report it @ https://github.com/iGabyTM/arcane-vouchers")
            isEnabled = false
            return
        }

        FileHandler(this)
        this.settings = Settings(this.config)

        this.audiences = BukkitAudiences.create(this)
        this.compatibilityHandler = CompatibilityHandler(this)

        this.vouchersConfig = Config(this, "vouchers.yml", false)
        this.langFile = Config(this, "lang.yml")

        this.actionManager = ArcaneActionManager(this)
        this.itemCreator = ItemCreator(this)
        this.voucherManager = VoucherManager(this)
        this.requirementProcessor = ArcaneRequirementProcessor(this.actionManager, this.compatibilityHandler)

        reload()
        CommandManager(this) // register the commands
        BStats(this)

        sequenceOf(
            DisableActionsListener(this),
            VoucherUseListener(this)
        ).forEach { server.pluginManager.registerEvents(it, this) }

        ResourcesHandler(this)
    }

    fun reload() {
        // Reload the configs
        reloadConfig()
        this.langFile.reload()
        this.vouchersConfig.reload()

        Lang.load(this.langFile.yaml)
        this.settings.load(this.config)
        this.itemCreator.loadNbt()
        this.voucherManager.load()
    }

}