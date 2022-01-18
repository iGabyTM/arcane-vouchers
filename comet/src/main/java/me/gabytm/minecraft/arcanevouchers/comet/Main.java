package me.gabytm.minecraft.arcanevouchers.comet;

import me.gabytm.minecraft.arcanevouchers.comet.modules.implementations.converter.ConverterModule;
import org.apache.commons.cli.*;
import org.slf4j.LoggerFactory;

public class Main {

    private static Options createOptions() {
        final Options options = new Options();

        options.addOption(
                Option.builder("a")
                        .longOpt("action")
                        .required()
                        .hasArg()
                        .type(String.class)
                        .argName("action")
                        .desc("Available actions: convert, updater")
                        .build()
        );

        options.addOption(
                Option.builder("p")
                        .longOpt("plugin")
                        .hasArg()
                        .type(String.class)
                        .argName("plugin")
                        .desc("Available plugins for conversion: " + String.join(", ", ConverterModule.converters()))
                        .build()
        );
        return options;
    }

    public static void main(String[] args) {
        final Options options = createOptions();

        try {
            final CommandLineParser parser = new DefaultParser();
            final CommandLine commandLine = parser.parse(options, args);

            new Comet(commandLine);
        } catch (ParseException e) {
            if (e instanceof MissingOptionException) {
                new HelpFormatter().printHelp("see below", options);
                return;
            }

            LoggerFactory.getLogger("main").error(e.getMessage(), e);
        }
    }

}
