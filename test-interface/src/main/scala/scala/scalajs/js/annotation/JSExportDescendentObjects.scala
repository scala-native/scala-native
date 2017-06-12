package scala.scalajs.js.annotation

@deprecated(
  "0.3.0",
  "Internal implementation detail from Scala.js, will be removed in the future.")
class JSExportDescendentObjects(ignoreInvalidDescendants: Boolean)
    extends scala.annotation.StaticAnnotation {
  def this() = this(false)
}
