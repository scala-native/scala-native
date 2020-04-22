package scala.scalanative
package linker

import scala.collection.mutable
import nir.{Global, Dep, Attr, Defn}
import nir.serialization.deserializeBinary
import java.nio.file.{FileSystems, Path}
import scalanative.io.VirtualDirectory
import scalanative.util.Scope

sealed trait ClassPath {

  /** Check if given global is present in this classpath. */
  private[scalanative] def contains(name: Global): Boolean

  /** Load given global and info about its dependencies. */
  private[scalanative] def load(name: Global): Option[Seq[Defn]]

  private[scalanative] def classesWithEntryPoints(): Iterable[Global.Top]
}

object ClassPath {

  /** Create classpath based on the directory. */
  def apply(directory: Path): ClassPath =
    new Impl(VirtualDirectory.local(directory))

  /** Create classpath based on the virtual directory. */
  private[scalanative] def apply(directory: VirtualDirectory): ClassPath =
    new Impl(directory)

  private final class Impl(directory: VirtualDirectory) extends ClassPath {
    private val files =
      directory.files
        .filter(_.toString.endsWith(".nir"))
        .map { file =>
          val name = Global.Top(io.packageNameFromPath(file))

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
          deserializeBinary(directory.read(file))
        }
      })

    def classesWithEntryPoints(): Iterable[Global.Top] = {
      files.filter {
        case (top, file) =>
          val buffer = directory.read(file, len = 1)
          buffer.get != 0
      }.keySet
    }
  }
}
