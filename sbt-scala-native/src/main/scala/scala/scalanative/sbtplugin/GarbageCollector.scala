package scala.scalanative
package sbtplugin

import java.io.File

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
}
