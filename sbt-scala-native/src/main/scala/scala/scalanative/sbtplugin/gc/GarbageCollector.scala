package scala.scalanative
package sbtplugin
package gc

import java.io.File

/**
 * @param dir directory name of the gc
 * @param links dependencies of the gc
 */
abstract class GarbageCollector(dir: String, val links: Seq[String] = Nil) {
  // Directory in nativelib containing the garbage collectors
  private final val garbageCollectorsDir = "gc"
  private val specificDir                = s"$garbageCollectorsDir/$dir"

  /**
   *
   * Used to find files that should not be compiled
   *
   * @param path the path to the file
   * @param nativelib nativelib directory
   * @return true if path is in the nativelib gc directory but not the current GC, false otherwise
   */
  private def isOtherGC(path: String, nativelib: File): Boolean = {
    val nativeGCPath = nativelib.toPath.resolve(garbageCollectorsDir)
    path.contains(nativeGCPath.toString) && !path.contains(specificDir)
  }

  /**
   * Removes all files specific to other gcs.
   */
  def filterFiles(files: Seq[File], nativelib: File): Seq[File] =
    files.filterNot(f => isOtherGC(f.getPath().toString, nativelib))

}

object NoGC extends GarbageCollector("nogc")
object BoehmGC extends GarbageCollector("boehm", Seq("gc"))
