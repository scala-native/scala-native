package scala.scalanative
package linker

import java.nio.file.Path
import java.io.ByteArrayInputStream
import java.io.BufferedReader
import java.io.InputStreamReader

import scala.collection.mutable
import scala.scalanative.io.VirtualDirectory
import scala.scalanative.nir.serialization.deserializeBinary
import scala.scalanative.nir.serialization.{Prelude => NirPrelude}
import scala.scalanative.nir.serialization.NirDeserializationException
import scala.scalanative.build.Logger

sealed trait ClassPath {

  /** Check if given global is present in this classpath. */
  private[scalanative] def contains(name: nir.Global): Boolean

  /** Load given global and info about its dependencies. */
  private[scalanative] def load(name: nir.Global.Top): Option[Seq[nir.Defn]]

  private[scalanative] def classesWithEntryPoints: Iterable[nir.Global.Top]

  private[scalanative] def definedServicesProviders
      : Map[nir.Global.Top, Seq[nir.Global.Top]]
}

object ClassPath {

  /** Create classpath based on the virtual directory. */
  private[scalanative] def apply(directory: VirtualDirectory): ClassPath =
    new Impl(directory, Logger.nullLogger)

  /** Create classpath based on the virtual directory. */
  private[scalanative] def apply(
      directory: VirtualDirectory,
      log: Logger
  ): ClassPath =
    new Impl(directory, log)

  private final class Impl(directory: VirtualDirectory, log: Logger)
      extends ClassPath {
    val nirFiles = mutable.Map.empty[nir.Global.Top, Path]
    val serviceProviders = mutable.Map.empty[nir.Global.Top, Path]

    directory.files
      .foreach {
        case path if path.toString.endsWith(".nir") =>
          val name = nir.Global.Top(io.packageNameFromPath(path))
          nirFiles.update(name, path)

        // First variant for jars, seconds for local directories
        case path
            if (path.startsWith("/META-INF/services/") ||
              path.startsWith("META-INF/services/")) =>
          val serviceName = nir.Global.Top(path.getFileName().toString())
          serviceProviders.update(serviceName, path)

        case _ => ()
      }

    private val cache =
      mutable.Map.empty[nir.Global.Top, Option[Seq[nir.Defn]]]

    def contains(name: nir.Global) =
      nirFiles.contains(name.top)

    private def makeBufferName(directory: VirtualDirectory, file: Path) =
      directory.uri
        .resolve(new java.net.URI(file.getFileName().toString))
        .toString

    def load(name: nir.Global.Top): Option[Seq[nir.Defn]] =
      cache.getOrElseUpdate(
        name, {
          nirFiles.get(name.top).map { file =>
            deserializeBinary(directory, file)
          }
        }
      )

    lazy val classesWithEntryPoints: Iterable[nir.Global.Top] = {
      nirFiles.filter {
        case (_, file) =>
          def logUnreadableFile(err: NirDeserializationException): Unit =
            log.warn(
              s"Can't check if file contains entrypoints, ${file} would be ignored: ${err.getMessage()}."
            )
          val buffer = directory.read(file, len = NirPrelude.length)
          NirPrelude
            .tryReadFrom(buffer, makeBufferName(directory, file))
            .left
            .map(logUnreadableFile)
            .exists(_.hasEntryPoints)
      }.keySet
    }

    lazy val definedServicesProviders
        : Map[nir.Global.Top, Seq[nir.Global.Top]] = {
      serviceProviders.map {
        case (name, path) =>
          val b = Seq.newBuilder[nir.Global.Top]
          val reader = new BufferedReader(
            new InputStreamReader(
              new ByteArrayInputStream(directory.read(path).array())
            )
          )
          try
            reader
              .lines()
              .map[String](_.trim())
              .filter(_.nonEmpty)
              .forEach(b += nir.Global.Top(_))
          finally reader.close()
          name -> b.result()
      }.toMap
    }
  }
}
