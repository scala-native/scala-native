package scala.scalanative
package linker

import java.io.File
import java.nio.file.{Files, Paths, Path, FileVisitOption}
import java.nio.file.attribute.BasicFileAttributes
import java.util.function.BiPredicate
import scala.collection.JavaConverters._
import nir._
import nir.serialization._

sealed abstract class Assembly {
  def contains(entry: Global): Boolean
  def load(entry: Global): Option[(Seq[Dep], Defn)]
}

object Assembly {
  final case class Dir private[Assembly](val base: File) extends Assembly {
    //println(s"discovered dir assembly $base")

    class NirMatcher extends BiPredicate[Path, BasicFileAttributes] {
      def test(path: Path, b: BasicFileAttributes) =
        path.toString.endsWith(".nir")
    }

    def matchFiles(f: String): Iterator[Path] =
      Files
        .find(Paths.get(f),
              Integer.MAX_VALUE,
              new NirMatcher(),
              FileVisitOption.FOLLOW_LINKS)
        .iterator
        .asScala

    private val entries: Map[Global, BinaryDeserializer] = {
      val baseabs = base.getAbsolutePath()

      matchFiles(baseabs).map { path: Path =>
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
        val name  = ctor(parts.mkString("."))
        (name -> deserializeBinaryFile(fileabs))
      }.toMap
    }

    def contains(name: Global) =
      entries.contains(name.top)

    def load(name: Global): Option[(Seq[Dep], Defn)] =
      entries.get(name.top).flatMap { deserializer =>
        //println(s"deserializing $name")
        deserializer.deserialize(name)
      }
  }

  final case class Jar private[Assembly](val base: File) extends Assembly {
    def contains(entry: Global) = false

    def load(entry: Global) = None
  }

  def apply(path: String): Option[Assembly] = {
    //println(s"assembly for $path")
    val file = new File(path)

    if (!file.exists) None
    else if (file.isDirectory) Some(new Dir(file))
    else if (path.endsWith(".jar")) Some(new Jar(file))
    else throw new LinkingError(s"unrecognized classpath entry: $path")
  }
}
