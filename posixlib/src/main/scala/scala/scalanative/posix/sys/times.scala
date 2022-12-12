package scala.scalanative
package posix
package sys

import scalanative.unsafe._

/** POSIX sys/times.h for Scala
 *
 *  The Open Group Base Specifications
 *  [[https://pubs.opengroup.org/onlinepubs/9699919799 Issue 7, 2018]] edition.
 */

@extern
object times {
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

  implicit class tmsOps(val ptr: Ptr[tms]) extends AnyVal {
    def tms_utime(): clock_t = ptr._1
    def tms_stime(): clock_t = ptr._2
    def tms_cutime(): clock_t = ptr._3
    def tms_cstime(): clock_t = ptr._4

    // The fields are query-only in use. Provide setters for completeness.
    def tms_utime_=(c: clock_t): Unit = ptr._1 = c
    def tms_stime_=(c: clock_t): Unit = ptr._2 = c
    def tms_cutime_=(c: clock_t): Unit = ptr._3 = c
    def tms_cstime_=(c: clock_t): Unit = ptr._4 = c
  }
}
