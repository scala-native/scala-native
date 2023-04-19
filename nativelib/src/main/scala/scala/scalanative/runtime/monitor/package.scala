package scala.scalanative.runtime

import scala.scalanative.runtime.Intrinsics._
import scala.scalanative.annotation.alwaysinline
import scala.scalanative.meta.LinktimeInfo.{is32BitPlatform => is32bit}

package object monitor {

  /*
   * Lock word can contain one of two memory layouts: Thin or Inflated Monitor
   *
   * Thin monitor is lightweight structure used in single-threaded workflows.
   * It does not support wait/notify routines. In case of detected contention (
   * when two threads are trying to lock the same object) ThinMonitor is being
   * inflated.
   *
   * 64bit platforms
   * 64bit lock word as ThinMonitor =
   * |------56bit-------|---7bit----|--1bit--|
   * | threadID (owner) | recursion |   0    |
   *
   * InflatedMonitor contains reference to heavy-weight ObjectMonitor
   * 64bit lock word as InflatedMonitor
   * |------63bit-------------------|--1bit--|
   * |     ObjectMonitor ref        |   1    |
   *
   * 32bit platforms
   * Thin monitor
   * |------24bit-------|---7bit----|--1bit--|
   * | threadID (owner) | recursion |   0    |
   *
   * Fat monitor
   * |------31bit-------------------|--1bit--|
   * |     ObjectMonitor ref        |   1    |
   *
   */
  private[runtime] object LockWord {
    // For information why we use `def` instead of `val` see comments in the runtime/MemoryLayout
    @alwaysinline def RecursionOffset = 1
    @alwaysinline def RecursionBits = 7
    @alwaysinline def ThinMonitorMaxRecursion = (1 << RecursionBits) - 1
    @alwaysinline def RecursionMask = ThinMonitorMaxRecursion << RecursionOffset

    @alwaysinline def ThreadIdOffset = 8
    @alwaysinline def ThreadIdBits = 56
    @alwaysinline def ThreadIdMax = (1L << ThreadIdBits) - 1
    @alwaysinline def ThreadIdMask = ThreadIdMax << ThreadIdOffset

    @alwaysinline def LockTypeOffset = 0
    @alwaysinline def LockTypeBits = 1
    @alwaysinline def LockTypeMask = 1L
    // ((1L << LockTypeBits) - 1) << LockTypeOffset

    object LockType {
      @alwaysinline def Deflated = 0
      @alwaysinline def Inflated = 1
    }

    // Potentially can decreased 60bits if we would need to encode additioanl flags
    @alwaysinline def ObjectMonitorOffset = 1
    @alwaysinline def ObjectMonitorBits = 63
    @alwaysinline def ObjectMonitorMask = -2L
    // ((1L << ObjectMonitorBits) - 1) << ObjectMonitorOffset
  }

  private[runtime] object LockWord32 {
    import LockWord._
    @alwaysinline def ThreadIdBits = 24
    @alwaysinline def ThreadIdMax = (1 << ThreadIdBits) - 1
    @alwaysinline def ThreadIdMask = ThreadIdMax << ThreadIdOffset

    @alwaysinline def LockTypeMask = 1
    // ((1 << LockTypeBits) - 1) << LockTypeOffset

    @alwaysinline def ObjectMonitorBits = 31
    @alwaysinline def ObjectMonitorMask = -2
    // ((1 << ObjectMonitorBits) - 1) << ObjectMonitorOffset
  }

  @inline private[runtime] implicit class LockWord(val value: RawPtr)
      extends AnyVal {
    @alwaysinline def longValue = castRawPtrToLong(value)
    @alwaysinline def intValue = castRawPtrToInt(value)
    @alwaysinline def ==(other: RawPtr) =
      if (is32bit) intValue == other.intValue
      else longValue == other.longValue

    import LockWord._

    @alwaysinline def isDefalted =
      if (is32bit) (intValue & LockTypeMask) == LockType.Deflated
      else (longValue & LockTypeMask) == LockType.Deflated
    @alwaysinline def isInflated =
      if (is32bit) (intValue & LockTypeMask) == LockType.Inflated
      else (longValue & LockTypeMask) == LockType.Inflated
    @alwaysinline def isUnlocked =
      if (is32bit) intValue == 0
      else longValue == 0L

    // Thin monitor ops
    // ThreadId uses the most significent bits, so no mask is required.
    @alwaysinline def threadId: RawPtr =
      if (is32bit) castIntToRawPtr(intValue >>> ThreadIdOffset)
      else castLongToRawPtr(longValue >>> ThreadIdOffset)

    @alwaysinline def recursionCount =
      if (is32bit) ((intValue & RecursionMask) >>> RecursionOffset).toInt
      else ((longValue & RecursionMask) >>> RecursionOffset).toInt

    @alwaysinline def withIncreasedRecursion: RawPtr = {
      if (is32bit)
        castIntToRawPtr(
          ((intValue >>> RecursionOffset) + 1) << RecursionOffset
        )
      else
        castLongToRawPtr(
          ((longValue >>> RecursionOffset) + 1) << RecursionOffset
        )
    }

    @alwaysinline def withDecresedRecursion: RawPtr = {
      if (is32bit)
        castIntToRawPtr(
          ((intValue >>> RecursionOffset) - 1) << RecursionOffset
        )
      else
        castLongToRawPtr(
          ((longValue >>> RecursionOffset) - 1) << RecursionOffset
        )
    }

    // Inflated monitor ops
    @alwaysinline def getObjectMonitor: ObjectMonitor = {
      // assert(isInflated, "LockWord was not inflated")
      val addr =
        if (is32bit) castIntToRawPtr((intValue & LockWord32.ObjectMonitorMask))
        else castLongToRawPtr(longValue & LockWord.ObjectMonitorMask)

      castRawPtrToObject(addr).asInstanceOf[ObjectMonitor]
    }
  }
}
