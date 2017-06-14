package com.fingolfintek.util

import java.io.Closeable

class AutoCloseableContext : Closeable {
  val closeables = arrayListOf<Closeable>()

  fun <T : Closeable> T.autoClose(): T {
    closeables.add(this)
    return this
  }

  fun <T> T.autoClose(closer: T.() -> Any): T {
    closeables.add(Closeable { this.closer() })
    return this
  }

  override fun close() {
    closeables.reversed().forEach { it.close() }
  }
}

inline fun <R> using(body: AutoCloseableContext.() -> R): R {
  val context = AutoCloseableContext()
  return context.use {
    it.body()
  }
}
