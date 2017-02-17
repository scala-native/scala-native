package scala.scalanative
package sbtplugin
package gc

import java.io.File
import GarbageCollector._

object GarbageCollector {
  val dir = "gc"
}

case class GarbageCollector(name: String, links: Seq[String]) {
  private val specificDir = s"$dir/$name"

  def isOtherGC(path: String): Boolean =
    path.contains(dir) && !path.contains(specificDir)

  def filterFiles(files: Seq[File]): Seq[File] =
    files.filterNot(f => isOtherGC(f.getPath().toString))

}

final object NoGC extends GarbageCollector("nogc", Seq())
final object BoehmGC extends GarbageCollector("boehm", Seq("gc"))
