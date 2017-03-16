package scala.scalanative
package sbtplugin

import java.io.File

/**
 * @param dir directory name of the gc
 * @param links dependencies of the gc
 */
sealed abstract class GarbageCollector(val dir: String,
                                       val links: Seq[String] = Nil)

object NoGC extends GarbageCollector("nogc")
object BoehmGC extends GarbageCollector("boehm", Seq("gc"))
