package scala.scalanative
package runtime

import scala.scalanative.unsafe.{CLongLong, extern}

@extern
object time {
  def scalanative_nano_time: CLongLong = extern
  def scalanative_current_time_millis: CLongLong = extern

  /** Time zone offset in seconds
   *
   *  @return
   *    offset in seconds from UTC
   */
  def scalanative_time_zone_offset(): CLongLong = extern
}
