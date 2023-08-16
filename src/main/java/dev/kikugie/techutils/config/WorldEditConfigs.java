package dev.kikugie.techutils.config;

import com.google.common.collect.ImmutableList;
import fi.dy.masa.malilib.config.options.ConfigBoolean;
import fi.dy.masa.malilib.config.options.ConfigBooleanHotkeyed;
import fi.dy.masa.malilib.config.options.ConfigInteger;

public class WorldEditConfigs extends Configs.BaseConfigs {
    public static final ConfigBooleanHotkeyed WE_SYNC = new ConfigBooleanHotkeyed("autoWeSync", true, "",
            "Synchronise WorldEdit region to active Litematica selection");
    public static final ConfigInteger WE_SYNC_TICKS = new ConfigInteger("autoWeSyncTicks", 10, 1, 1000, false, """
            Ticks to wait before synchronising WorldEdit selection.
            (Increase in case of poor connection or if you get kicked because of spam)""");
    public static final ConfigBoolean WE_SYNC_FEEDBACK = new ConfigBoolean("autoWeSyncFeedback", true,
            "Shows an actionbar message when WorldEdit region is syncronised");
    public static final ConfigBoolean DISABLE_UPDATES = new ConfigBoolean("autoDisableUpdates", true, """
            Automatically disable WorldEdit neighbour updates on server join.
            (Has the same effect as running //perf neighbors off)""");

    public WorldEditConfigs() {
        super(ImmutableList.of(
                WE_SYNC,
                WE_SYNC_TICKS,
                WE_SYNC_FEEDBACK,
                DISABLE_UPDATES
        ));
    }
}
