package scala.scalanative
package build

import java.nio.file.{Files, Path, Paths}
import scalanative.build.IO.RichPath
import scalanative.build.NativeLib._

object Filter {

  /** To find filter file */
  private val filterProperties = s"${codeDir}-filter.properties"

  /**
   * Filter the `nativelib` source files with special logic
   * to select GC and optional components.
   *
   * @param config The configuration of the toolchain.
   * @param linkerResult The results from the linker.
   * @param destPath The unpacked location of the Scala Native nativelib.
   * @param allPaths The native paths found for this library
   * @return The paths filtered to be included in the compile.
   */
  def filterNativelib(config: Config,
                      linkerResult: linker.Result,
                      destPath: Path,
                      allPaths: Seq[Path]): Seq[Path] = {
    val codePath = destPath.resolve(codeDir)
    // check if filtering is needed, o.w. return all paths
    findFilterProperties(codePath) match {
      case None => allPaths
      case Some(file) =>
        val paths = allPaths.map(_.abs)

        // predicate to check if given file path shall be compiled
        // we only include sources of the current gc and exclude
        // all optional dependencies if they are not necessary
        val optPath = codePath.resolve("optional").abs
        val (gcPath, gcSelPath) = {
          val gcPath    = codePath.resolve("gc")
          val gcSelPath = gcPath.resolve(config.gc.name)
          (gcPath.abs, gcSelPath.abs)
        }

        def include(path: String) = {
          if (path.contains(optPath)) {
            val name = Paths.get(path).toFile.getName.split("\\.").head
            linkerResult.links.map(_.name).contains(name)
          } else if (path.contains(gcPath)) {
            path.contains(gcSelPath)
          } else {
            true
          }
        }

        val (includePaths, excludePaths) = paths.partition(include(_))

        // delete .o files for all excluded source files
        excludePaths.foreach { path =>
          val opath = Paths.get(path + oExt) // path.resolve
          if (Files.exists(opath))
            Files.delete(opath)
        }

        includePaths.map(Paths.get(_))
    }
  }

  /**
   * Check for a filtering properties file in destination
   * native code directory.
   *
   * @param codePath The native code directory
   * @return The optional path to the file or none
   */
  private def findFilterProperties(codePath: Path): Option[Path] = {
    val file = codePath.resolve(filterProperties)
    if (Files.exists(file)) Some(file)
    else None
  }
}
