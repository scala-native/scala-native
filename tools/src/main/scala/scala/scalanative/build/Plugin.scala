package scala.scalanative
package build

import java.nio.file.Path

trait Plugin {
  def filterNativelib(config: Config,
                      linkerResult: linker.Result,
                      nativeCodePath: Path,
                      allPaths: Seq[Path]): (Seq[Path], Config)
}
