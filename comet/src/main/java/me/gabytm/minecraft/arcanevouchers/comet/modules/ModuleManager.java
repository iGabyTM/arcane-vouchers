package me.gabytm.minecraft.arcanevouchers.comet.modules;

import me.gabytm.minecraft.arcanevouchers.comet.modules.implementations.converter.ConverterModule;
import me.gabytm.minecraft.arcanevouchers.comet.modules.implementations.updater.UpdaterModule;
import org.apache.commons.cli.CommandLine;

public class ModuleManager {

    private final CometModule converterModule = new ConverterModule();
    private final CometModule updaterModule = new UpdaterModule();

    public void convert(final CommandLine commandLine) {
        this.converterModule.run(commandLine);
    }

    public void update(final CommandLine commandLine) {
        this.updaterModule.run(commandLine);
    }

}
