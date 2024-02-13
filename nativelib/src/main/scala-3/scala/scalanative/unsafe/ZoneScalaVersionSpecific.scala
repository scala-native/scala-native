package scala.scalanative.unsafe

import scala.annotation.targetName

trait ZoneCompanionScalaVersionSpecific { self: Zone.type =>

  /** Run given function with a fresh zone and destroy it afterwards. */
  inline def apply[T](inline f: Zone ?=> T): T = {
    val zone = open()
    try f(using zone)
    finally zone.close()
  }
}
