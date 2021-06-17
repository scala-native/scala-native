package scala.scalanative
package build.nativelib

import scala.scalanative.build.Plugin
import java.nio.file.{Files, Path, Paths}
import scala.scalanative.build.Config
import scala.scalanative.build.IO.RichPath
import scala.scalanative.build.LLVM.oExt
import scala.scalanative.linker.Result

class FilterPlugin extends Plugin {
  override def filterNativelib(config: Config,
                               linkerResult: Result,
                               nativeCodePath: Path,
                               allPaths: Seq[Path]): (Seq[Path], Config) = {
    println("Filtering native lib...")
    // predicate to check if given file path shall be compiled
    // we only include sources of the current gc and exclude
    // all optional (javalib zlib) dependencies if they are not necessary
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
