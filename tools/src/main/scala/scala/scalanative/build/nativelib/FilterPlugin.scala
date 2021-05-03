package scala.scalanative
package build.nativelib

import scala.scalanative.build.Plugin
import java.nio.file.Path
import scala.scalanative.build.Config
import scala.scalanative.linker.Result

class FilterPlugin extends Plugin {

  override def filterNativelib(config: Config,
                               linkerResult: Result,
                               destPath: Path,
                               allPaths: Seq[Path]): (Seq[Path], Config) = {
    println("*** filtering native lib test ***")
    (allPaths, config)
  }

}
