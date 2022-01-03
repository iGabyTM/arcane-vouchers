package me.gabytm.minecraft.arcanevouchers.updater;

import me.gabytm.minecraft.arcanevouchers.updater.updaters.ConfigUpdater;
import me.gabytm.minecraft.arcanevouchers.updater.updaters.UsagesUpdater;
import me.gabytm.minecraft.arcanevouchers.updater.updaters.VouchersUpdater;

public class Updater {

    public Updater() {
        new ConfigUpdater();
        new UsagesUpdater();
        new VouchersUpdater();
    }

}
