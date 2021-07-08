package scala.scalanative
package build

import java.nio.file.{Files, Path, Paths}

import java.util.Properties
import java.io.FileInputStream

import scalanative.build.IO.RichPath
import scalanative.build.NativeLib._
import scalanative.build.LLVM._
import scalanative.build.plugin._

private[scalanative] object Filter {

  /** To find filter file */
  private[build] val nativeProjectProps = s"${nativeCodeDir}.properties"

  /** Build plugin factory key */
  private[build] val buildPluginKey = "buildplugin"

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
    findFilterProperties(nativeCodePath).fold((allPaths, config)) { path =>
      val file = path.toFile()
      val props =
        try {
          val props = new Properties()
          props.load(new FileInputStream(file))
          props
        } catch {
          case t: Throwable =>
            sys.error(s"Unable to read properties: ${file}")
        }
      val pluginName = props.getProperty(buildPluginKey)
      val plugin = BuildPluginFactory.create(pluginName)
      plugin.filterNativelib(config, linkerResult, nativeCodePath, allPaths)
    }
  }

  /** Partitions the native source file paths using the include function and
   *  deletes any of the excluded source with `.o` appended. This way if the
   *  options changed from the prior compile, the `.o` files generated from the
   *  source files that are not needed will be removed.
   *
   *  @param allPaths
   *    All the identified source paths
   *  @param include
   *    A function that returns `true` for files to be included
   *  @return
   *    The included source paths
   */
  def filterPathsDeleteExcluded(
      allPaths: Seq[Path],
      include: String => Boolean
  ): Seq[Path] = {
    val (includePaths, excludePaths) = allPaths.map(_.abs).partition(include)

    // delete .o files for all excluded source files
    // avoids deleting .o files except when changing
    // options that change the excluded list
    excludePaths.foreach { path =>
      val opath = Paths.get(path + oExt)
      Files.deleteIfExists(opath)
    }

    val projectPaths = includePaths.map(Paths.get(_))
    projectPaths
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
