package scala.scalanative
package linker

import java.io.File
import java.nio.file.Paths

import scala.scalanative.nir._
import scala.scalanative.nir.serialization._

final case class Assembly(base: File) {
  private val baseabs  = base.getAbsolutePath()
  private val basePath = Paths.get(baseabs)
  private val classPath =
    if (baseabs.endsWith(".jar")) new JarClasspath(basePath)
    else new LocalClasspath(basePath)

  private val entries: Map[Global, BinaryDeserializer] = {
    classPath.contents
      .filter(_.relativePath.toString.endsWith(".nir"))
      .map { vfile =>
        val path    = vfile.relativePath
        val fileabs = path.toAbsolutePath().toString
        val relpath = fileabs.replace(baseabs + "/", "")
        val parts   = relpath.replace(".nir", "").split("/").toSeq
        val name    = Global.Top(parts.filter(_ != "").mkString("."))

        (name -> new BinaryDeserializer(vfile.bytes))
      }
      .toMap
  }

  def contains(name: Global) =
    entries.contains(name.top)

  def load(name: Global): Option[(Seq[Dep], Seq[Attr.Link], Defn)] =
    entries.get(name.top).flatMap { deserializer =>
      deserializer.deserialize(name)
    }

  def close = classPath.close()
}

object Assembly {
  def apply(path: String): Option[Assembly] = {
    val file = new File(path)
    if (!file.exists() || !file.canRead) None
    else if (file.isDirectory || file.getName.endsWith(".jar"))
      Some(Assembly(file))
    else throw new LinkingError(s"unrecognized classpath entry: $path")
  }
}
