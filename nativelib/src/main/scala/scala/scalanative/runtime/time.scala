package scala.scalanative
package runtime

import scala.scalanative.unsafe.{CLongLong, extern, compile}

@extern
object time {

  /** Monotonically increasing time for use in timers and for
   *  [[java.lang.System$.nanoTime()* System.nanoTime()]] in the Java library.
   *  @return
   *    increasing time hopefully at better than millisecond resolution
   */
  @compile("time_nano.c")
  def scalanative_nano_time(): CLongLong = extern

  /** Milliseconds from the UNIX epoch to implement
   *  [[java.lang.System$.currentTimeMillis()* System.currentTimeMillis()]] in
   *  the Java library.
   *  @return
   *    time in millis (UTC)
   */
  @compile("time_millis.c")
  def scalanative_current_time_millis(): CLongLong = extern

  /** Time zone offset in seconds from UTC. Negative to the west and positive to
   *  the East. Designed to be used by [[java.util.Date]] by adding to UTC.
   *
   *  @return
   *    offset in seconds from UTC
   */
  @compile("time_zone_offset.c")
  def scalanative_time_zone_offset(): CLongLong = extern
}
