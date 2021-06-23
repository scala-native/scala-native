package scala.scalanative.build
package plugin

import java.nio.file.{Files, Path, Paths}

import scalanative.build.Config
import scalanative.build.IO.RichPath
import scalanative.build.LLVM.oExt
import scalanative.linker.Result

/** If used, includes the C code used for `java.util.zip` */
class JavalibBuildPlugin extends BuildPlugin {
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

    val (includePaths, excludePaths) = allPaths.map(_.abs).partition(include)

    // delete .o files for all excluded source files
    // avoids deleting .o files except when changing
    // optional zip code
    excludePaths.foreach { path =>
      val opath = Paths.get(path + oExt)
      Files.deleteIfExists(opath)
    }

    val projectPaths = includePaths.map(Paths.get(_))
    (projectPaths, config)
  }
}
