package me.kikugie.techutils.utils

import java.util.*

object ResponseMuffler {
    private val muteQueue = ArrayDeque<String>()
    fun scheduleMute(matcher: String) {
        muteQueue.add(matcher)
    }

    fun pop() {
        muteQueue.remove()
    }

    fun clear() {
        muteQueue.clear()
    }

    fun matches(message: String): Boolean {
        return if (muteQueue.isEmpty()) false else message.matches(muteQueue.peek().toRegex())
    }
}