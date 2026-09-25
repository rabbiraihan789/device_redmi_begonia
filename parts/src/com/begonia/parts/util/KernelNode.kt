package com.begonia.parts.util

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException

object KernelNode {

    fun exists(path: String): Boolean = File(path).exists()

    fun read(path: String): String? {
        return try {
            FileInputStream(path).use { stream ->
                val buffer = ByteArray(256)
                val count = stream.read(buffer)
                if (count <= 0) null else String(buffer, 0, count).trim()
            }
        } catch (e: IOException) {
            null
        }
    }

    fun readLong(path: String): Long = read(path)?.toLongOrNull() ?: -1L

    fun write(path: String, value: String): Boolean {
        return try {
            FileOutputStream(path).use { stream ->
                stream.write(value.toByteArray())
            }
            true
        } catch (e: IOException) {
            false
        }
    }

    fun write(path: String, value: Long): Boolean = write(path, value.toString())
}