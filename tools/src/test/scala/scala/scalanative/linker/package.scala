package scala.scalanative

import scala.scalanative.nir._

package object linker {
  val linktimeInfoProperties = "scala.scalanative.meta.linktimeinfo"
  // Defaults values for linktime resolved properties
  val linktimeInfoDefaults: Map[String, Boolean] = {
    Map(
      s"$linktimeInfoProperties.isWindows" -> false,
      s"$linktimeInfoProperties.isWeakReferenceSupported" -> false
    )
  }
}
