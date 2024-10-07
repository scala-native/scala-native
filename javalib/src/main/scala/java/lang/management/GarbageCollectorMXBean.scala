package java.lang.management

import scala.scalanative.meta.LinktimeInfo
import scala.scalanative.runtime.GC

trait GarbageCollectorMXBean extends MemoryManagerMXBean {

  /** Returns the total number of garbage collections that have occurred.
   *
   *  Returns `-1` when the collection count is undefined for this collector.
   */
  def getCollectionCount(): Long

  /** Returns the approximate accumulated elapsed time in milliseconds.
   *
   *  Returns `-1` if the collection elapsed time is undefined for this
   *  collector.
   */
  def getCollectionTime(): Long
}

object GarbageCollectorMXBean {
  private val NanosPerMilli = 1000000

  private[management] def apply(): GarbageCollectorMXBean =
    new Impl

  private class Impl extends GarbageCollectorMXBean {
    def getName(): String = LinktimeInfo.garbageCollector
    def isValid(): Boolean = true
    def getMemoryPoolNames(): Array[String] = Array.empty
    def getCollectionCount(): Long = GC.getStatsCollectionTotal().toLong
    def getCollectionTime(): Long =
      GC.getStatsCollectionDurationTotal().toLong / NanosPerMilli
  }

}
