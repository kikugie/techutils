package dev.kikugie.techutils.feature.containerscan.scanners;

public interface Scanner {
    void tick();

    void start();

    void stop();

    void update();
}
