package scala.scalanative.build
package plugin

import java.nio.file.{Files, Path, Paths}

import scalanative.build.Config
import scalanative.build.IO.RichPath
import scalanative.build.LLVM.oExt
import scalanative.linker.Result

/** If used, includes the C code used for `java.util.zip` */
private[build] class JavalibBuildPlugin extends BuildPlugin {
  override def filterNativelib(config: Config,
                               linkerResult: Result,
                               nativeCodePath: Path,
                               allPaths: Seq[Path]): (Seq[Path], Config) = {
    // predicate to check if given file path shall be compiled and exclude
    // all optional (javalib zlib) dependencies if they are not necessary
    val optPath = nativeCodePath.resolve("optional").abs

    def include(path: String) = {
      if (path.contains(optPath)) {
        val name = Paths.get(path).toFile.getName.split("\\.").head
        linkerResult.links.map(_.name).contains(name)
      } else {
        true
      }
    }

    val projectPaths = Filter.filterPathsDeleteExcluded(allPaths, include)

    (projectPaths, config)
  }
}
