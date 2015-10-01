package salty.tools.linker

import java.io.File
import org.apache.commons.io.FileUtils
import org.apache.commons.io.filefilter.{DirectoryFileFilter, RegexFileFilter}
import salty.ir.Name

// TODO: rewrite using nio-only
// TODO: replace with something less naive in the future
class Classpath(val paths: Seq[String]) extends salty.ir.Classpath {
  lazy val resolve: Seq[(Name, String)] =
    paths.flatMap { path =>
      val base = new File(path)
      val baseabs = base.getAbsolutePath()
      val files =
        FileUtils.listFiles(
          base,
          new RegexFileFilter("(.*)\\.salty"),
          DirectoryFileFilter.DIRECTORY).toArray.toIterator
      files.map { case file: File =>
        val fileabs = file.getAbsolutePath()
        val relpath = fileabs.replace(baseabs + "/", "")
        val (nm, rel) =
          if (relpath.endsWith(".class.salty"))
            (Name.Class, relpath.replace(".class.salty", ""))
          else if (relpath.endsWith("$.module.salty"))
            (Name.Module, relpath.replace("$.module.salty", ""))
          else if (relpath.endsWith(".module.salty"))
            (Name.Module, relpath.replace(".module.salty", ""))
          else if (relpath.endsWith(".interface.salty"))
            (Name.Interface, relpath.replace(".interface.salty", ""))
          else
            throw new Exception(s"can't parse file kind $relpath")
        val parts = rel.split("/").toSeq
        val name = nm(parts.mkString("."))
        (name -> fileabs)
      }.toSeq
    }
}
