package scala.scalanative
package util

import scala.collection.mutable
import scala.annotation.nowarn

object Stats {
  private val times = mutable.Map.empty[String, Long]
  private val counts = mutable.Map.empty[String, Long]
  private val dists = mutable.Map.empty[String, mutable.UnrolledBuffer[Long]]
  private def printTotal(): Unit = {
    val totalTimes = mutable.Map.empty[String, Long]
    val totalCounts = mutable.Map.empty[String, Long]
    val totalThreads = mutable.Map.empty[String, Long]
    times.foreach {
      case (k, v) =>
        val key = k.split(":")(1)
        totalTimes(key) = totalTimes.getOrElse(key, 0L) + v
        totalThreads(key) = totalThreads.getOrElse(key, 0L) + 1
    }
    counts.foreach {
      case (k, v) =>
        val key = k.split(":")(1)
        totalCounts(key) = totalCounts.getOrElse(key, 0L) + v
    }
    println("--- Total")
    totalTimes.toSeq.sortBy(_._1).foreach {
      case (key, time) =>
        val ms = (time / 1000000d).toString
        val count = totalCounts(key)
        val threads = totalThreads(key)
        println(s"$key: $ms ms, $count times, $threads threads")
    }
    if (dists.nonEmpty) {
      println("--- Total (Dist)")
      printDist()
    }
  }
  private def printDist(): Unit = {
    val elems = dists.toSeq.sortBy(_._1)
    elems.foreach {
      case (key, measurements) =>
        println(key + ":")
        println("  min: " + measurements.min)
        println("  max: " + measurements.max)
        println(
          "  avg: " + measurements.map(_.toDouble).sum / measurements.size
        )
    }
  }
  private def printThread(id: String): Unit = {
    println(s"--- Thread $id")
    times.toSeq.sortBy(_._1).foreach {
      case (key, time) if key.startsWith(id) =>
        val ms = (time / 1000000d).toString
        val count = counts(key)
        val k = key.split(":")(1)
        println(s"$k: $ms ms, $count times")
      case _ =>
        ()
    }
  }
  private def printThreads(): Unit = {
    val threads = mutable.Set.empty[String]
    times.keys.foreach { k => threads += k.split(":")(0) }
    threads.toSeq.sorted.foreach(printThread)
  }
  private def print(): Unit = synchronized {
    printTotal()
    printThreads()
  }
  private def clear(): Unit = synchronized {
    times.clear()
    counts.clear()
  }
  @nowarn // "Thread.getId() is deprecated")
  private def threadKey(key: String): String =
    "" + java.lang.Thread.currentThread.getId + ":" + key
  def in[T](f: => T): T = {
    clear()
    val res = f
    print()
    res
  }
  def time[T](key: String)(f: => T): T = {
    import System.nanoTime
    val start = nanoTime()
    val res = f
    val end = nanoTime()
    val t = end - start
    val k = threadKey(key)
    times.synchronized {
      times(k) = times.getOrElse(k, 0L) + t
    }
    counts.synchronized {
      counts(k) = counts.getOrElse(k, 0L) + 1
    }
    res
  }
  def dist(key: String)(value: Long): Unit = {
    dists.synchronized {
      val buf = dists.getOrElseUpdate(key, mutable.UnrolledBuffer.empty[Long])
      buf += value
    }
  }
}
