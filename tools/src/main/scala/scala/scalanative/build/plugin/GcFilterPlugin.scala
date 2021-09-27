package scala.scalanative.build
package plugin

import java.nio.file.{Files, Path, Paths}

import scalanative.build.Config
import scalanative.build.IO.RichPath
import scalanative.build.LLVM.oExt
import scalanative.linker.Result

/** Includes the GC code and filters unused code. */
private[build] class GcFilterPlugin extends FilterPlugin {
  override def filterNativelib(
      config: Config,
      linkerResult: Result,
      nativeCodePath: Path,
      allPaths: Seq[Path]
  ): (Seq[Path], Config) = {
    // predicate to check if given file path shall be compiled
    // we only include sources of the current gc
    val (gcPath, gcIncludePaths, gcSelectedPaths) = {
      val gcPath = nativeCodePath.resolve("gc")
      val gcIncludePaths = config.gc.include.map(gcPath.resolve(_).abs)
      val selectedGC = gcPath.resolve(config.gc.name).abs
      val selectedGCPath = selectedGC +: gcIncludePaths
      (gcPath.abs, gcIncludePaths, selectedGCPath)
    }

    def include(path: String) = {
      if (path.contains(gcPath)) {
        gcSelectedPaths.exists(path.contains)
      } else {
        true
      }
    }

    val projectPaths = Filter.filterPathsDeleteExcluded(allPaths, include)

    val projectConfig = config.withCompilerConfig(
      _.withCompileOptions(
        config.compileOptions ++ gcIncludePaths.map("-I" + _)
      )
    )

    (projectPaths, projectConfig)
  }
}
