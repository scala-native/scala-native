package scala.scalanative
package runtime

import scala.scalanative.native.{CLong, extern}

@extern
object time {
  def scalanative_nano_time: CLong = extern
}
