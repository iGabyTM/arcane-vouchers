package me.gabytm.minecraft.arcanevouchers.comet.modules.implementations.updater;

import me.gabytm.minecraft.arcanevouchers.comet.modules.CometModule;
import me.gabytm.minecraft.arcanevouchers.comet.modules.ModuleInfo;
import me.gabytm.minecraft.arcanevouchers.comet.modules.implementations.updater.updaters.ConfigUpdater;
import me.gabytm.minecraft.arcanevouchers.comet.modules.implementations.updater.updaters.UsagesUpdater;
import me.gabytm.minecraft.arcanevouchers.comet.modules.implementations.updater.updaters.VouchersUpdater;
import org.apache.commons.cli.CommandLine;

@ModuleInfo(id = "updater")
public class UpdaterModule extends CometModule {

    @Override
    public void run(final CommandLine commandLine) {
        new ConfigUpdater();
        new UsagesUpdater();
        new VouchersUpdater();
    }

}
