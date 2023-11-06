package dev.kikugie.techutils.client.util.data

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
import java.util.function.Function

@Suppress("UNCHECKED_CAST")
class Pos2ObjectMap<T>(
    private val defaultSupplier: (Long) -> T
) : MutableMap<Long, T> {
    private val delegate: Long2ObjectOpenHashMap<T> = Long2ObjectOpenHashMap()
    override val entries: MutableSet<MutableMap.MutableEntry<Long, T>>
        get() = delegate.long2ObjectEntrySet() as MutableSet<MutableMap.MutableEntry<Long, T>>
    override val keys: MutableSet<Long>
        get() = delegate.keys
    override val size: Int
        get() = delegate.size
    override val values: MutableCollection<T>
        get() = delegate.values

    override fun clear() {
        delegate.clear()
    }

    override fun isEmpty(): Boolean {
        return delegate.isEmpty()
    }

    override fun remove(key: Long): T? {
        return delegate.remove(key)
    }

    override fun putAll(from: Map<out Long, T>) {
        delegate.putAll(from)
    }

    override fun put(key: Long, value: T): T? {
        return delegate.put(key, value)
    }

    override fun get(key: Long): T {
        return delegate.getOrDefault(key, defaultSupplier(key))
    }

    fun compute(key: Long): T {
        return delegate.computeIfAbsent(key, defaultSupplier as Function<in Long, out T>)
    }

    override fun containsValue(value: T): Boolean {
        return delegate.containsValue(value)
    }

    override fun containsKey(key: Long): Boolean {
        return delegate.containsKey(key)
    }
}