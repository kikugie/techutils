package dev.kikugie.techutils.client.task

class AwaitTask<T>(
    val listener: (T) -> Unit
) : Task {
    override val completed: Boolean
        get() = _completed

    override fun init() {
    }

    private var _completed = false

    fun complete(result: T) {
        listener.run { result }
        _completed = true
    }

    override fun tick() {

    }

    override fun cancel() {
    }
}