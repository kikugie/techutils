package dev.kikugie.techutils.client.task

object TaskManager {
    private val initQueue = ArrayList<Task>()
    private val tasks = ArrayList<Task>()
    fun tick() {
        tasks.addAll(initQueue)
        initQueue.forEach { it.init() }
        initQueue.clear()

        val itr = tasks.iterator()
        while (itr.hasNext()) {
            val task = itr.next()
            task.tick()
            if (task.completed) itr.remove()
        }
    }

    fun onUnload() {
        tasks.forEach { it.cancel() }
        tasks.clear()
        initQueue.clear()
    }

    fun add(task: Task): Task {
        initQueue.add(task)
        return task
    }
}