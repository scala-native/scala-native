package native
package compiler

import native.nir._
import native.nir.serialization._

sealed trait Loader {
  def resolve(name: Name): Option[Defn]
}

final class BinaryLoader(cp: ClasspathLoader, path: String) extends Loader {
  def deserializer: BinaryDeserializer = ???
  def resolve(name: Name) = ???
}

final class ClasspathLoader(paths: Seq[String]) extends Loader {
  def resolve(name: Name) = ???
}
