package scala.scalanative
package build

import java.nio.file.{Files, Path, Paths}

import scalanative.build.IO.RichPath
import scalanative.build.NativeLib._
import scalanative.build.LLVM._

private[scalanative] object Filter {

  /** To find filter file */
  private val nativeProjectProps = s"${nativeCodeDir}.properties"

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
                      allPaths: Seq[Path]): (Seq[Path], Config) = {
    val nativeCodePath = destPath.resolve(nativeCodeDir)
    // check if filtering is needed, o.w. return all paths
    findFilterProperties(nativeCodePath).fold((allPaths, config)) { file =>
      // predicate to check if given file path shall be compiled
      // we only include sources of the current gc and exclude
      // all optional dependencies if they are not necessary
      val optPath = nativeCodePath.resolve("optional").abs
      val (gcPath, gcIncludePaths, gcSelectedPaths) = {
        val gcPath         = nativeCodePath.resolve("gc")
        val gcIncludePaths = config.gc.include.map(gcPath.resolve(_).abs)
        val selectedGC     = gcPath.resolve(config.gc.name).abs
        val selectedGCPath = selectedGC +: gcIncludePaths
        (gcPath.abs, gcIncludePaths, selectedGCPath)
      }

      def include(path: String) = {
        if (path.contains(optPath)) {
          val name = Paths.get(path).toFile.getName.split("\\.").head
          linkerResult.links.map(_.name).contains(name)
        } else if (path.contains(gcPath)) {
          gcSelectedPaths.exists(path.contains)
        } else {
          true
        }
      }

      val (includePaths, excludePaths) = allPaths.map(_.abs).partition(include)

      // delete .o files for all excluded source files
      // avoids deleting .o files except when changing
      // optional or garbage collectors
      excludePaths.foreach { path =>
        val opath = Paths.get(path + oExt)
        Files.deleteIfExists(opath)
      }
      val projectConfig = config.withCompilerConfig(
        _.withCompileOptions(
          config.compileOptions ++ gcIncludePaths.map("-I" + _)))
      val projectPaths = includePaths.map(Paths.get(_))
      (projectPaths, projectConfig)
    }
  }

  /**
   * Check for a filtering properties file in destination
   * native code directory.
   *
   * @param nativeCodePath The native code directory
   * @return The optional path to the file or none
   */
  private def findFilterProperties(nativeCodePath: Path): Option[Path] = {
    val file = nativeCodePath.resolve(nativeProjectProps)
    if (Files.exists(file)) Some(file)
    else None
  }
}
