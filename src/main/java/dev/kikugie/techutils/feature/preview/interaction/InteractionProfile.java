package dev.kikugie.techutils.feature.preview.interaction;

import dev.kikugie.techutils.config.LitematicConfigs;

public interface InteractionProfile {
    void set(int x, int y, int viewportSize);

    void scrolled(double x, double y, double amount);

    void dragged(double x, double y, double dx, double dy, int button);

    void released(double x, double y);

    void clicked(double x, double y, int button);

    boolean inViewport(double x, double y);

    int x();

    int y();

    int viewport();

    double angle();

    default double slant() {
        return Math.toRadians(LitematicConfigs.RENDER_SLANT.getIntegerValue());
    }

    float dx();

    float dy();

    float scale();
}
