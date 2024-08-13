package java.lang.management

import scala.scalanative.runtime.GC

trait MemoryMXBean {

  /** Returns the current runtime memory usage.
   *
   *  @note
   *    the committed memory is always -1
   *
   *  @note
   *    the memory usage values may differ between invocations
   */
  def getHeapMemoryUsage(): MemoryUsage

  /** Runs the garbage collector.
   *
   *  An alias for `System.gc()`.
   */
  def gc(): Unit
}

object MemoryMXBean {

  private[management] def apply(): MemoryMXBean =
    new Impl

  private class Impl extends MemoryMXBean {
    def getHeapMemoryUsage(): MemoryUsage =
      new MemoryUsage(
        GC.getInitHeapSize().toLong,
        GC.getUsedHeapSize().toLong,
        -1L,
        GC.getMaxHeapSize().toLong
      )

    def gc(): Unit =
      System.gc()
  }

}
