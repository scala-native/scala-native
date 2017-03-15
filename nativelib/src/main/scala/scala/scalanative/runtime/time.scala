package scala.scalanative
package runtime

import scala.scalanative.native.{CLongLong, extern}

@extern
object time {
  def scalanative_nano_time: CLongLong = extern
}
