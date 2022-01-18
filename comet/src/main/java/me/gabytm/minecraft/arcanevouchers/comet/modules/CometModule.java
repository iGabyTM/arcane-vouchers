package me.gabytm.minecraft.arcanevouchers.comet.modules;

import org.apache.commons.cli.CommandLine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class CometModule {

    protected final String id;
    protected final Logger logger;

    public CometModule() {
        final ModuleInfo info = getClass().getAnnotation(ModuleInfo.class);

        this.id = info.id();
        this.logger = LoggerFactory.getLogger(this.id);
    }

    public abstract void run(final CommandLine commandLine);

}
