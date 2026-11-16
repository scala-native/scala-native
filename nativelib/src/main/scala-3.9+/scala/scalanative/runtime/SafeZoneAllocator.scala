package scala.scalanative.runtime

import scala.scalanative.memory.SafeZone
import scala.scalanative.unsafe._

import language.experimental.captureChecking

/** We can move SafeZoneAllocator to package `memory` and make it
 *  `private[scalanative]` after dotty supports using `new {sz} T(...)` to
 *  create new instance allocated in sz. Currently, we need it not private to
 *  package scalanative for unit tests.
 */
object SafeZoneAllocator {
  /* The result captures both the zone and everything captured by `obj`, so
   * that references from zone allocated objects cannot be used to hide
   * captured capabilities. */
  def allocate[T](sz: SafeZone^, obj: T^): T^{sz, obj} = intrinsic

  @extern @define("__SCALANATIVE_MEMORY_SAFEZONE") object Impl {
    @name("scalanative_zone_open")
    def open(): RawPtr = extern

    @name("scalanative_zone_alloc")
    def alloc(rawzone: RawPtr, rawty: RawPtr, size: RawSize): RawPtr = extern

    @name("scalanative_zone_close")
    def close(rawzone: RawPtr): Unit = extern
  }
}
