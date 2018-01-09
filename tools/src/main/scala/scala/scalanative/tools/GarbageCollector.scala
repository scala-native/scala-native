package scala.scalanative.tools

/**
 * @param dir directory name of the gc
 * @param links dependencies of the gc
 */
sealed abstract class GarbageCollector(val name: String,
                                       val links: Seq[String] = Nil)
object GarbageCollector {
  object None  extends GarbageCollector("none")
  object Boehm extends GarbageCollector("boehm", Seq("gc"))
  object Immix extends GarbageCollector("immix")

  def apply(gc: String) = gc match {
    case "none"  => GarbageCollector.None
    case "boehm" => GarbageCollector.Boehm
    case "immix" => GarbageCollector.Immix
    case value =>
      throw new Exception(
        "nativeGC can be either \"none\", \"boehm\" or \"immix\", not: " + value)
  }
}
