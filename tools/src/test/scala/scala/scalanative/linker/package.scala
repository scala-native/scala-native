package scala.scalanative

import scala.scalanative.nir._

package object linker {
  // Defaults values for linktime resolved properties
  val linktimeInfoDefaults = {
    val info = "scala.scalanative.meta.LinktimeInfo"
    Map(
      s"$info.isWindows" -> false
    )
  }

  def propName(name: String) = Sig.Generated(name + "_property")

  val linktimeInfoModule = Global.Top("scala.scalanative.meta.LinktimeInfo$")
  val isWindowsProp = linktimeInfoModule.member(propName("isWindows"))
}
