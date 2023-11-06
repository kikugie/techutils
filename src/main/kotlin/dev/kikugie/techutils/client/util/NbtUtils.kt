package dev.kikugie.techutils.client.util

import net.minecraft.nbt.NbtCompound

fun NbtCompound.getString(key: String, default: String): String = this.getString(key).ifBlank { default }
