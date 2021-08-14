package me.gabytm.minecraft.arcanevouchers

import me.gabytm.minecraft.arcanevouchers.actions.ArcaneActionManager
import me.gabytm.minecraft.arcanevouchers.config.Config
import me.gabytm.minecraft.arcanevouchers.functions.color
import me.gabytm.minecraft.arcanevouchers.voucher.VoucherManager
import net.kyori.adventure.platform.bukkit.BukkitAudiences
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import java.util.regex.Pattern

class ArcaneVouchers : JavaPlugin() {

    lateinit var audiences: BukkitAudiences private set

    lateinit var vouchersConfig: Config private set

    lateinit var actionManager: ArcaneActionManager private set
    lateinit var voucherManager: VoucherManager private set

    private fun sendLogo() {
        val matcher = Pattern.compile("\\d+\\.\\d+(?:\\.\\d+)?").matcher(Bukkit.getBukkitVersion())
        val serverVersion = if (matcher.find()) matcher.group() else "unknown"

        with (description) {
            sequenceOf(
                "&5 _____   _____   ",
                "&5|  _  | |  |  |  &fArcaneVouchers &dv$version &fby &d${authors.joinToString()} &7($serverVersion)",
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

        saveDefaultConfig()

        this.audiences = BukkitAudiences.create(this)
        this.vouchersConfig = Config(this, "vouchers.yml")

        this.actionManager = ArcaneActionManager(this)
        this.voucherManager = VoucherManager(this)
    }

}