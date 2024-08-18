package java.lang.management

import scala.scalanative.runtime.GC

trait MemoryMXBean {

  /** Returns the current runtime memory usage.
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

    /** The committed memory = used memory.
     *
     *  By @WojciechMazur: This one might be more difficult to track. On Windows
     *  we do manually commit memory provided by VirtualAlloc, but in the case
     *  of Unix we typically only reserve a maximal chunk of memory using mmap
     *  and committing is done automatically by the system on the first usage of
     *  every page. Access to this memory is only limited by Immix/Commix Heap
     *  blocks limit. I guess the best choice would be to fill it with usedHeap
     *  which is still correct regarding Javadocs: committed will always be
     *  greater than or equal to used.
     */
    def getHeapMemoryUsage(): MemoryUsage = {
      val init = GC.getInitHeapSize().toLong
      val used = GC.getUsedHeapSize().toLong
      val max = GC.getMaxHeapSize().toLong
      new MemoryUsage(init, used, used, max)
    }

    def gc(): Unit =
      System.gc()
  }

}
