package dev.kikugie.techutils.client.task

interface Task {
    val completed: Boolean
    fun init()
    fun tick()
    fun cancel()
}