package scala.scalanative
package linker

import nir.{Global, Dep, Attr, Defn}
import nir.serialization.BinaryDeserializer
import java.nio.file.FileSystems
import scalanative.io.VirtualDirectory
import scalanative.util.Scope

sealed trait ClassPath {

  /** Check if given global is present in this classpath. */
  def contains(name: Global): Boolean

  /** Load given global and info about its dependencies. */
  def load(name: Global): Option[(Seq[Dep], Seq[Attr.Link], Seq[String], Defn)]

  /** Load all globals */
  def globals: Set[Global]
}

object ClassPath {

  /** Create classpath based on the virtual directory. */
  def apply(directory: VirtualDirectory): ClassPath =
    new Impl(directory)

  private final class Impl(directory: VirtualDirectory) extends ClassPath {
    private val entries: Map[Global, BinaryDeserializer] = {
      directory.files
        .filter(_.toString.endsWith(".nir"))
        .map { file =>
          val name = Global.Top(io.packageNameFromPath(file))

          (name -> new BinaryDeserializer(directory.read(file)))
        }
        .toMap
    }

    def contains(name: Global) =
      entries.contains(name.top)

    def load(
        name: Global): Option[(Seq[Dep], Seq[Attr.Link], Seq[String], Defn)] =
      entries.get(name.top).flatMap { deserializer =>
        deserializer.deserialize(name)
      }

    def globals: Set[Global] = entries.values.flatMap(_.globals).toSet
  }
}
