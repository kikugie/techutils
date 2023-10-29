package dev.kikugie.techutils.client.feature.containerscan

import dev.kikugie.techutils.client.config.option.Options.create

object ContainerScanConfig {
    val scanContainers = create("scanContainers", false, "U, S")
    val fillContainers = create("fillContainers", false, "U, N")
}