package scala.scalanative

import java.nio.file.{Files, Path}
import java.util.Arrays

import tools.{Config, IO, LinkerReporter, LinkerResult, Logger, OptimizerDriver}

package object build {

  /**
   * Unpack the `nativelib` to `workdir/lib`.
   *
   * If the same archive has already been unpacked to this location, this
   * call has no effects.
   *
   * @param nativelib The JAR to unpack.
   * @param workdir   The working directory. The nativelib will be unpacked
   *                  to `workdir/lib`.
   * @return The location where the nativelib has been unpacked, `workdir/lib`.
   */
  def unpackNativeLibrary(nativeLib: Path, workdir: Path): Path = {
    val lib         = workdir.resolve("lib")
    val jarhash     = IO.sha1(nativeLib)
    val jarhashPath = lib.resolve("jarhash")
    def unpacked =
      Files.exists(lib) &&
        Files.exists(jarhashPath) &&
        Arrays.equals(jarhash, Files.readAllBytes(jarhashPath))

    if (!unpacked) {
      IO.deleteRecursive(lib)
      IO.unzip(nativeLib, lib)
      Files.write(jarhashPath, jarhash)
    }

    lib
  }

  /** Links the NIR files on classpath, reports linking errors. */
  def linkNIR(driver: OptimizerDriver,
              config: Config,
              reporter: LinkerReporter,
              logger: Logger): LinkerResult = {
    val result = logger.time("Linking") {
      tools.link(config, driver, reporter)
    }
    if (result.unresolved.nonEmpty) {
      result.unresolved.map(_.show).sorted.foreach { signature =>
        logger.error(s"cannot link: $signature")
      }
      throw new Exception("unable to link")
    }
    val classCount = result.defns.count {
      case _: nir.Defn.Class | _: nir.Defn.Module | _: nir.Defn.Trait => true
      case _                                                          => false
    }
    val methodCount = result.defns.count(_.isInstanceOf[nir.Defn.Define])
    logger.info(s"Discovered ${classCount} classes and ${methodCount} methods")
    result
  }

}
