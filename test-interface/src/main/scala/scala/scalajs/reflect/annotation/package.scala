package scala.scalajs.reflect

package object annotation {
  @deprecated(
    message =
      "Use scala.scalanative.reflect.annotation.EnableReflectiveInstantiation instead.",
    since = "0.4.0")
  type EnableReflectiveInstantiation =
    scala.scalanative.reflect.annotation.EnableReflectiveInstantiation
}
