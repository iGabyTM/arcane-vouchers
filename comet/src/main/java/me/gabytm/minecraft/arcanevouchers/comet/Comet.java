package me.gabytm.minecraft.arcanevouchers.comet;

import me.gabytm.minecraft.arcanevouchers.comet.modules.ModuleManager;
import org.apache.commons.cli.CommandLine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Comet {

    private static final Logger LOGGER = LoggerFactory.getLogger(Comet.class);

    public Comet(final CommandLine commandLine) {
        final ModuleManager moduleManager = new ModuleManager();

        final String action = commandLine.getOptionValue('a');

        switch (action.toLowerCase()) {
            case "convert": {
                moduleManager.convert(commandLine);
                return;
            }

            case "update": {
                moduleManager.update(commandLine);
                return;
            }

            default: {
                LOGGER.error("Unknown action {}", action);
                System.exit(0);
            }
        }

    }

}
