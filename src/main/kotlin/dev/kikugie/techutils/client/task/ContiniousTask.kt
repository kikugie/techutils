package dev.kikugie.techutils.client.task

abstract class ContiniousTask(
) : Task {
    override val completed = false

    override fun init() {

    }
}