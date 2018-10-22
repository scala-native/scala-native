package scala.scalanative
package linker

import scala.collection.mutable
import nir.{Global, Dep, Attr, Defn, Type}
import nir.serialization.deserializeBinary
import java.nio.file.{FileSystems, Path}
import scalanative.io.VirtualDirectory
import scalanative.util.Scope

sealed trait ClassPath {

  /** Check if given global is present in this classpath. */
  private[scalanative] def contains(name: Global): Boolean

  /** Load given global and info about its dependencies. */
  private[scalanative] def load(name: Global): Option[Seq[Defn]]
}

object ClassPath {

  /** Create classpath based on the directory. */
  def apply(directory: Path, config: build.Config): ClassPath =
    new Impl(VirtualDirectory.local(directory), config)

  /** Create classpath based on the virtual directory. */
  private[scalanative] def apply(directory: VirtualDirectory,
                                 config: build.Config): ClassPath =
    new Impl(directory, config)

  private final class Impl(directory: VirtualDirectory, config: build.Config)
      extends ClassPath {
    private val files =
      directory.files
        .filter(_.toString.endsWith(".nir"))
        .map { file =>
          val name = Global.stripImplClassTrailingDollar(
            Global.Top(io.packageNameFromPath(file)))

          name -> file
        }
        .toMap

    private val cache =
      mutable.Map.empty[Global, Option[Seq[Defn]]]

    def contains(name: Global) =
      files.contains(name.top)

    def load(name: Global): Option[Seq[Defn]] =
      cache.getOrElseUpdate(name, {
        files.get(name.top).map { file =>
          deserializeBinary(
            directory.read(file)
          )
        }
      })
  }
}
