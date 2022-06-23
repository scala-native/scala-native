package scala.scalanative.compat

import scala.annotation.StaticAnnotation

object annotation {
  // Stub for nowarn annotations to allow compilation with legacy versions of Scala
  class nowarn(value: String = "") extends StaticAnnotation
}
