package scala.scalanative.build

/**
 * @param dir directory name of the gc
 * @param links dependencies of the gc
 */
sealed abstract class GC(val name: String, val links: Seq[String] = Nil)
object GC {
  object None  extends GC("none")
  object Boehm extends GC("boehm", Seq("gc"))
  object Immix extends GC("immix")

  def apply(gc: String) = gc match {
    case "none"  => GC.None
    case "boehm" => GC.Boehm
    case "immix" => GC.Immix
    case value =>
      throw new IllegalArgumentException(
        "nativeGC can be either \"none\", \"boehm\" or \"immix\", not: " + value)
  }

  /** The default garbage collector. */
  def default: GC = Boehm
}
