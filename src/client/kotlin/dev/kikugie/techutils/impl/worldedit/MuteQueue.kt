package dev.kikugie.techutils.impl.worldedit

class MuteQueue {
    val delegate = ArrayList<Regex>()

    operator fun plusAssign(value: Regex) {
        delegate.add(value)
    }

    fun test(value: String): Boolean {
        val match = delegate.indexOfFirst { it.matches(value) }
        if (match == -1) return false
        delegate.removeAt(match)
        return true
    }

    fun clear() {
        delegate.clear()
    }
}