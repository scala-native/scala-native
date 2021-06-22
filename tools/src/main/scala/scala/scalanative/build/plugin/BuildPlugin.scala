package scala.scalanative.build
package plugin

import java.nio.file.Path

import scalanative.linker

trait BuildPlugin {
  def filterNativelib(config: Config,
                      linkerResult: linker.Result,
                      nativeCodePath: Path,
                      allPaths: Seq[Path]): (Seq[Path], Config)
}
