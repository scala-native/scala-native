package scala.scalanative
package runtime

import scala.scalanative.native.{CLongLong, extern}

@extern
object time {
<<<<<<< 4459f51eb5b8a0039e00609e5446fea26a349159
  def scalanative_nano_time: CLongLong           = extern

  def scalanative_current_time_millis: CLongLong = extern
}
=======
  def scalanative_nano_time: CLongLong = extern
}
>>>>>>> second field of timespec is now CLong
