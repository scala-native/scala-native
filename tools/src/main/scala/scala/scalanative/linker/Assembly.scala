package scala.scalanative
package linker

import java.io.File
import java.nio.file.Paths

import scala.scalanative.nir._
import scala.scalanative.nir.serialization._

final case class Assembly(base: File) {
  private val baseabs = base.getAbsolutePath()
  private val basePath = Paths.get(baseabs)
  private val classPath =
    if (baseabs.endsWith(".jar")) new JarClasspath(basePath)
    else new LocalClasspath(basePath)

  private val entries: Map[Global, BinaryDeserializer] = {
    classPath.contents
      .filter(_.relativePath.toString.endsWith(".nir"))
      .map { vfile =>
        val path = vfile.relativePath
        val fileabs = path.toAbsolutePath().toString
        val relpath = fileabs.replace(baseabs + "/", "")
        val (ctor, rel) =
          if (relpath.endsWith(".type.nir"))
            (Global.Type, relpath.replace(".type.nir", ""))
          else if (relpath.endsWith(".value.nir"))
            (Global.Val, relpath.replace(".value.nir", ""))
          else
            throw new LinkingError(s"can't recognized assembly file: $relpath")
        val parts = rel.split("/").toSeq
        val name = ctor(parts.filter(_ != "").mkString("."))
        (name -> new BinaryDeserializer(vfile.bytes))
      }
      .toMap
  }

  def contains(name: Global) =
    entries.contains(name.top)

  def load(name: Global): Option[(Seq[Dep], Defn)] =
    entries.get(name.top).flatMap { deserializer =>
      //println(s"deserializing $name")
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
