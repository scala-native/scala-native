package scala.scalanative
package linker

import java.nio.file.Path

import scala.collection.mutable
import scala.scalanative.io.VirtualDirectory
import scala.scalanative.nir.serialization.deserializeBinary
import scala.scalanative.nir.serialization.{Prelude => NirPrelude}

sealed trait ClassPath {

  /** Check if given global is present in this classpath. */
  private[scalanative] def contains(name: nir.Global): Boolean

  /** Load given global and info about its dependencies. */
  private[scalanative] def load(name: nir.Global): Option[Seq[nir.Defn]]

  private[scalanative] def classesWithEntryPoints: Iterable[nir.Global.Top]

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
          val name = nir.Global.Top(io.packageNameFromPath(file))

          name -> file
        }
        .toMap

    private val cache =
      mutable.Map.empty[nir.Global, Option[Seq[nir.Defn]]]

    def contains(name: nir.Global) =
      files.contains(name.top)

    private def makeBufferName(directory: VirtualDirectory, file: Path) =
      directory.uri
        .resolve(new java.net.URI(file.getFileName().toString))
        .toString

    def load(name: nir.Global): Option[Seq[nir.Defn]] =
      cache.getOrElseUpdate(
        name, {
          files.get(name.top).map { file =>
            deserializeBinary(
              directory.read(file),
              makeBufferName(directory, file)
            )
          }
        }
      )

    lazy val classesWithEntryPoints: Iterable[nir.Global.Top] = {
      files.filter {
        case (_, file) =>
          val buffer = directory.read(file, len = NirPrelude.length)
          NirPrelude
            .readFrom(buffer, makeBufferName(directory, file))
            .hasEntryPoints
      }.keySet
    }
  }
}
