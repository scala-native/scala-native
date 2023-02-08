package scala.scalanative.runtime

import scalanative.unsafe._

@extern
object CZone {
  @name("scalanative_zone_open")
  def open(): RawPtr = extern

  @name("scalanative_zone_alloc")
  def alloc(rawzone: RawPtr, rawty: RawPtr, size: CSize): RawPtr = extern

  @name("scalanative_zone_alloc")
  def alloc(rawzone: RawPtr, cls: Class[_], size: CSize): RawPtr = extern

  @name("scalanative_zone_close")
  def close(rawzone: RawPtr): Unit = extern
}
