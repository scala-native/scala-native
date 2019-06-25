package scala.scalanative.build

/** Garbage Collector. Application is going
 *  to be automatically linked with corresponding
 *  libraries that implement given collector. One of the:
 *
 *  * None GC. Never frees allocated memory.
 *
 *  * Boehm GC. Conservative mark-and-sweep garbage collector.
 *
 *  * Immix GC. Mostly-precise mark-region garbage collector.
 *
 *  Additional GCs might be added to the list in the future.
 *
 *  @param dir name of the gc
 *  @param links linking dependencies of the gc
 */
sealed abstract class GC private (val name: String, val links: Seq[String]) {
  override def toString: String = name
}
object GC {
  private[scalanative] final case object None  extends GC("none", Seq())
  private[scalanative] final case object Boehm extends GC("boehm", Seq("gc"))
  private[scalanative] final case object Immix extends GC("immix", Seq())

  /** Non-freeing garbage collector.*/
  def none: GC = None

  /** Conservative garbage collector based on libgc. */
  def boehm: GC = Boehm

  /** Mostly-precise mark-region garbage collector. */
  def immix: GC = Immix

  /** The default garbage collector. */
  def default: GC = Immix

  /** Get a garbage collector with given name. */
  def apply(gc: String) = gc match {
    case "none" =>
      GC.None
    case "boehm" =>
      GC.Boehm
    case "immix" =>
      GC.Immix
    case value =>
      throw new IllegalArgumentException(
        "nativeGC can be either \"none\", \"boehm\" or \"immix\", not: " + value)
  }
}
