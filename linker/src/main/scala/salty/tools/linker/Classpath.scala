package salty.tools.linker

import java.io.File
import org.apache.commons.io.FileUtils
import org.apache.commons.io.filefilter.{DirectoryFileFilter, RegexFileFilter}
import salty.ir

// TODO: replace with something less naive in the future
class Classpath(val paths: Seq[String]) extends salty.ir.Classpath {
  def resolve: Seq[(ir.Name, String)] =
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
        val rel = fileabs.replace(baseabs + "/", "").replace(".salty", "")
        val parts = rel.split("/").toSeq
        val name = ir.Name.Global(parts.mkString("."))
        (name -> fileabs)
      }.toSeq
    }
}
