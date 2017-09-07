package scala.scalanative
package util

import scala.collection.mutable

object Stats {
  private val times    = mutable.Map.empty[String, Long]
  private val counters = mutable.Map.empty[String, Long]
  def time[T](key: String)(f: => T): T = {
    import System.nanoTime
    val start = nanoTime()
    val res   = f
    val end   = nanoTime()
    synchronized {
      times(key) = times.getOrElse(key, 0L) + (end - start)
    }
    res
  }
  def count(key: String): Unit = synchronized {
    counters(key) = counters.getOrElse(key, 0L) + 1L
  }
  def print(): Unit = synchronized {
    println("--- Stats")
    times.toSeq.sortBy(_._1).foreach {
      case (key, time) =>
        println(key + ": " + (time / 1000000D).toString + " ms")
    }
    counters.toSeq.sortBy(_._1).foreach {
      case (key, n) =>
        println(key + ": " + n + " times")
    }
  }
  def clear(): Unit = synchronized {
    times.clear()
    counters.clear()
  }
}
