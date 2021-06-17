package scala.scalanative
package build

import java.nio.file.{Files, Path, Paths}

import java.util.Properties
import java.io.FileInputStream

import scalanative.build.IO.RichPath
import scalanative.build.NativeLib._
import scalanative.build.LLVM._

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
      // this should use portable-scala reflect
      // currently devoid of error handling
      val props = new Properties()
      props.load(new FileInputStream(file.toFile()))
      val plugin = props.getProperty("plugin")
      println(s"Found plugin: $plugin")
      val cls  = Class.forName(plugin)
      val ctor = cls.getConstructor()
      val obj  = ctor.newInstance().asInstanceOf[Plugin]
      obj.filterNativelib(config, linkerResult, nativeCodePath, allPaths)
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
