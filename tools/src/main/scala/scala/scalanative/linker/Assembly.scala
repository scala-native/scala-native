package scala.scalanative
package linker

import java.io.File
import org.apache.commons.io.FileUtils
import org.apache.commons.io.filefilter.{DirectoryFileFilter, RegexFileFilter}
import nir._
import nir.serialization._

sealed abstract class Assembly {
  def contains(entry: Global): Boolean
  def load(entry: Global): Option[(Seq[Global], Defn)]
}

object Assembly {
  final case class Dir private[Assembly] (val base: File) extends Assembly {
    private val entries: Map[Global, String] = {
      val baseabs = base.getAbsolutePath()
      val files =
        FileUtils.listFiles(
          base,
          new RegexFileFilter("(.*)\\.nir"),
          DirectoryFileFilter.DIRECTORY).toArray.toIterator
      files.map { case file: File =>
        val fileabs = file.getAbsolutePath()
        val relpath = fileabs.replace(baseabs + "/", "")
        val (isType, rel) =
          if (relpath.endsWith(".class.nir"))
            (true, relpath.replace(".class.nir", ""))
          else if (relpath.endsWith(".trait.nir"))
            (true, relpath.replace(".trait.nir", ""))
          else if (relpath.endsWith(".module.nir"))
            (false, relpath.replace(".module.nir", ""))
          else
            throw new LinkingError(s"can't recognized assembly file: $relpath")
        val parts = rel.split("/").toSeq
        val name = new Global(Seq(parts.mkString(".")), isType)
        (name -> fileabs)
      }.toMap
    }
    //println(s"discovered dir assembly $base")
    //println(s"entries: $entries")

    def contains(entry: Global) = entries.contains(entry)

    def load(entry: Global) = entries.get(entry).map { path =>
      //println(s"loaded $entry from $base")
      val (deps, defns) = deserializeBinaryFile(path)
      assert(defns.length == 1, "non-assembly nir files may contain only a single definition")
      (deps, defns.head)
    }
  }

  final case class Jar private[Assembly] (val base: File) extends Assembly {
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
