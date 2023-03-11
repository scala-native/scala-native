package scala.scalanative
package build

import java.nio.file.{Files, Path, Paths}

import scalanative.build.IO.RichPath
import scalanative.build.NativeLib._
import scalanative.util.Scope
import scalanative.io.VirtualDirectory
import scalanative.linker.ClassPath
import scalanative.linker.CompilationRequests

private[scalanative] object Filter {

  /** Filter the `nativelib` source files with special logic to select GC and
   *  optional components.
   *
   *  @param config
   *    The configuration of the toolchain.
   *  @param linkerResult
   *    The results from the linker.
   *  @param unpackedPath
   *    The unpacked location of the Scala Native nativelib.
   *  @return
   *    The paths filtered to be included in the compile.
   */
  def filterNativelib(
      config: Config,
      linkerResult: linker.Result,
      nativeLib: NativeLib
  ): Seq[nir.Attr.Compile] = {
    val toCompile = linkerResult.compilations
    if (toCompile.isEmpty) Nil
    else
      Scope { implicit scope: Scope =>
        val cp = ClassPath(VirtualDirectory.real(nativeLib.src))
        toCompile
          .collect {
            case CompilationRequests(ctx, name) if cp.contains(name) => ctx
          }
          .groupBy(_.relPath)
          .toSeq
          .map {
            case (path, seq) =>
              nir.Attr.Compile(
                path,
                seq.foldLeft(Seq.empty[String])(_ ++ _.options).distinct
              )
          }
      }

    // val projectConfig = config.withCompilerConfig(
    //   _.withCompileOptions(
    //     config.compileOptions ++ gcIncludePaths.map("-I" + _)
    //   )
    // )
    // val projectPaths = includePaths.map(Paths.get(_))
    // Result(projectPaths, projectConfig)
  }
}
