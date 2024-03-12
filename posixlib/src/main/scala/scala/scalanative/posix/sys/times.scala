package scala.scalanative
package posix
package sys

import scalanative.unsafe._

import scalanative.meta.LinktimeInfo.{is32BitPlatform, isFreeBSD, isNetBSD}

/** POSIX sys/times.h for Scala
 *
 *  The Open Group Base Specifications
 *  [[https://pubs.opengroup.org/onlinepubs/9699919799 Issue 7, 2018]] edition.
 */

@extern
object times {

  /* The 'tms' structure below is defined in a way which allows fast
   * direct call-thru to the C Runtime Library, without any "glue" code.
   *
   * Scala Native uses a CLong clock_t. This will be 64 bits on 64 bit
   * architectures and 32 bits on 32 bit architectures.
   *
   * This works well with Linux & macOS. FreeBSD and NetBSD uses a fixed
   * 32 bit clock_t on both 64 and 32 bit architectures.
   *
   * Using the names in timesOps below is recommended on any
   * architecture. On FreeBSD and NetBSD 64 bit machines using timeOps
   * names rather than the _N idiom is required in order to extract
   * correct & proper 32 bit values.
   */

  type clock_t = types.clock_t

  type tms = CStruct4[
    clock_t, // tms_utime User CPU time
    clock_t, // tms_stime System CPU time
    clock_t, // tms_cutime User CPU time of terminated child processes.
    clock_t // tms_cstime System CPU time of terminated child processes.
  ]

  def times(buf: Ptr[tms]): clock_t = extern
}

/** Allow using C names to access tms structure fields.
 */
object timesOps {
  import times._

  private def use32BitGetLowBits(bits: clock_t): clock_t =
    (bits.toLong & 0x00000000ffffffffL).toSize

  private def use32BitGetHighBits(bits: clock_t): clock_t =
    (bits.toLong & 0xffffffff00000000L).toSize

  private def use32BitSetLowBits(ptr: Ptr[clock_t], value: clock_t): Unit =
    !ptr = ((!ptr & 0xffffffff00000000L) | value.toInt).toSize

  private def use32BitSetHighBits(ptr: Ptr[clock_t], value: clock_t): Unit =
    !ptr = ((value << 32) | (!ptr & 0x00000000ffffffffL)).toSize

  implicit class tmsOps(val ptr: Ptr[tms]) extends AnyVal {
    def tms_utime: clock_t = if (!isFreeBSD && !isNetBSD) ptr._1
    else if (is32BitPlatform) ptr._1
    else use32BitGetLowBits(ptr._1)

    def tms_stime: clock_t = if (!isFreeBSD && !isNetBSD) ptr._2
    else if (is32BitPlatform) ptr._2
    else use32BitGetHighBits(ptr._1)

    def tms_cutime: clock_t = if (!isFreeBSD && !isNetBSD) ptr._3
    else if (is32BitPlatform) ptr._3
    else use32BitGetLowBits(ptr._2)

    def tms_cstime: clock_t = if (!isFreeBSD && !isNetBSD) ptr._4
    else if (is32BitPlatform) ptr._4
    else use32BitGetHighBits(ptr._2)

    /* The fields are query-only in use.
     * Provide setters for completeness and testing.
     */
    def tms_utime_=(c: clock_t): Unit =
      if (!isFreeBSD && !isNetBSD) ptr._1 = c
      else if (is32BitPlatform) ptr._1 = c
      else use32BitSetLowBits(ptr.at1, c)

    def tms_stime_=(c: clock_t): Unit =
      if (!isFreeBSD && !isNetBSD) ptr._2 = c
      else if (is32BitPlatform) ptr._2 = c
      else use32BitSetHighBits(ptr.at1, c)

    def tms_cutime_=(c: clock_t): Unit =
      if (!isFreeBSD && !isNetBSD) ptr._3 = c
      else if (is32BitPlatform) ptr._3 = c
      else use32BitSetLowBits(ptr.at2, c)

    def tms_cstime_=(c: clock_t): Unit =
      if (!isFreeBSD && !isNetBSD) ptr._4 = c
      else if (is32BitPlatform) ptr._4 = c
      else use32BitSetHighBits(ptr.at2, c)
  }
}
