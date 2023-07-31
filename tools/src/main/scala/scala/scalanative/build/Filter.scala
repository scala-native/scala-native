package scala.scalanative
package build

import java.nio.file.{Files, Path, Paths}

import scalanative.build.IO.RichPath
import scalanative.build.NativeLib._

private[scalanative] object Filter {

  /** To find filter file */
  private val nativeProjectProps = s"${nativeCodeDir}.properties"

  /** Filter the `nativelib` source files with special logic to select GC and
   *  optional components.
   *
   *  @param config
   *    The configuration of the toolchain.
   *  @param linkerResult
   *    The results from the linker.
   *  @param destPath
   *    The unpacked location of the Scala Native nativelib.
   *  @param allPaths
   *    The native paths found for this library
   *  @return
   *    The paths filtered to be included in the compile.
   */
  def filterNativelib(
      config: Config,
      linkerResult: linker.Result,
      destPath: Path,
      allPaths: Seq[Path]
  ): (Seq[Path], Config) = {
    val nativeCodePath = destPath.resolve(nativeCodeDir)
    // check if filtering is needed, o.w. return all paths
    findFilterProperties(nativeCodePath).fold((allPaths, config)) { file =>
      // predicate to check if given file path shall be compiled
      // we only include sources to the base of the gc code and exclude
      // all optional dependencies if they are not necessary
      val optPath = nativeCodePath.resolve("optional").abs
      val gcPath = nativeCodePath.resolve("gc").abs

      def include(path: String) = {
        if (path.contains(optPath)) {
          val name = Paths.get(path).toFile.getName.split("\\.").head
          linkerResult.links.exists(_.name == name)
        } else {
          true
        }
      }

      // All the .o files are kept but we pass on the
      // included files to the link phase
      val includePaths = allPaths.map(_.abs).filter(include)

      /* A conditional compilation define is used to compile the
       * correct garbage collector code as code is shared.
       * Note: The zone directory is also part of the garbage collection
       * system and shares code from the gc directory.
       */
      val gcFlag = {
        val gc = config.compilerConfig.gc.toString
        s"-DSCALANATIVE_GC_${gc.toUpperCase}"
      }

      val projectConfig = config.withCompilerConfig(
        _.withCompileOptions(
          config.compileOptions :+ ("-I" + gcPath) :+ gcFlag
        )
      )
      val projectPaths = includePaths.map(Paths.get(_))
      (projectPaths, projectConfig)
    }
  }

  /** Check for a filtering properties file in destination native code
   *  directory.
   *
   *  @param nativeCodePath
   *    The native code directory
   *  @return
   *    The optional path to the file or none
   */
  private def findFilterProperties(nativeCodePath: Path): Option[Path] = {
    val file = nativeCodePath.resolve(nativeProjectProps)
    if (Files.exists(file)) Some(file)
    else None
  }
}
