package scala.scalanative
package build

import java.io.FileReader
import java.nio.file.{Files, Path, Paths}
import java.util.Properties

import scala.util.Failure
import scala.util.Success

import scalanative.build.IO.RichPath
import scalanative.build.NativeLib._
import scala.scalanative.linker.ReachabilityAnalysis

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
   *    The (paths, config) for this native library.
   */
  def filterNativelib(
      config: Config,
      analysis: ReachabilityAnalysis.Result,
      destPath: Path,
      allPaths: Seq[Path]
  ): (Seq[Path], Config) = {
    val nativeCodePath = destPath.resolve(nativeCodeDir)
    // check if filtering is needed, o.w. return all paths
    findFilterProperties(nativeCodePath).fold((allPaths, config)) { filepath =>

      val desc =
        Descriptor.load(filepath) match {
          case Success(v) => v
          case Failure(e) =>
            throw new BuildException(
              s"Problem reading $nativeProjectProps: ${e.getMessage}"
            )
        }

      config.logger.debug(desc.toString())

      val projectConfig = desc match {
        case Descriptor(Some(groupId), Some(artifactId), _)
            if (groupId == "org.scala-native" && artifactId == "nativelib") =>
          createGcConfig(nativeCodePath, config)
        case Descriptor(_, _, _) =>
          createLinkConfig(desc, analysis, config)
      }

      (allPaths, projectConfig)
    }
  }

  private def createLinkConfig(
      desc: Descriptor,
      analysis: ReachabilityAnalysis.Result,
      config: Config
  ): Config = {
    val linkDefines =
      desc.link
        .filter(name => analysis.links.exists(_.name == name))
        .map(name => s"-DSCALANATIVE_LINK_${name.toUpperCase}")

    config.withCompilerConfig(
      _.withCompileOptions(_ ++ linkDefines)
    )
  }

  private def createGcConfig(
      nativeCodePath: Path,
      config: Config
  ): Config = {
    /* A conditional compilation define is used to compile the
     * correct garbage collector code because code is shared.
     * This avoids handling all the paths needed and compiling
     * all the GC code for a given platform.
     *
     * Note: The zone directory is also part of the garbage collection
     * system and shares code from the gc directory.
     */
    val gcFlag = {
      val gc = config.compilerConfig.gc.toString
      s"-DSCALANATIVE_GC_${gc.toUpperCase}"
    }

    val gcPath = nativeCodePath.resolve("gc").abs

    config.withCompilerConfig(
      _.withCompileOptions(_ :+ ("-I" + gcPath) :+ gcFlag)
    )
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
