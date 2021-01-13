package scala.scalanative
package nscplugin

import java.io.FileOutputStream
import java.nio.file.{Path, Paths}
import scala.scalanative.nir.serialization.serializeBinary
import scala.tools.nsc.Global
import scala.tools.nsc.io.AbstractFile

trait NirGenFile[G <: Global with Singleton] { self: NirGenPhase[G] =>
  import global._

  def genPathFor(cunit: CompilationUnit, ownerName: nir.Global): Path = {
    val nir.Global.Top(id) = ownerName
    genPathFor(cunit, id)
  }

  def genPathFor(cunit: CompilationUnit, id: String): Path = {
    val baseDir: AbstractFile =
      settings.outputDirs.outputDirFor(cunit.source.file)

    val pathParts = id.split("[./]")
    val dir       = pathParts.init.foldLeft(baseDir)(_.subdirectoryNamed(_))

    val filename = pathParts.last
    val file     = dir fileNamed (filename + ".nir")

    Paths.get(file.file.getAbsolutePath)
  }

  def genIRFile(path: Path, defns: Seq[nir.Defn]): Unit = {
    val outStream = new FileOutputStream(path.toFile)
    try {
      serializeBinary(defns, outStream)
    } finally outStream.close()
  }
}
