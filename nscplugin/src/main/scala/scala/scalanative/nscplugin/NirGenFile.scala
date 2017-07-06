package scala.scalanative
package nscplugin

import java.nio.file.{Path, Paths}
import scala.tools.nsc._
import scala.tools.nsc.io.AbstractFile
import scalanative.nir.serialization.{serializeText, serializeBinary}
import scalanative.io.withScratchBuffer
import scalanative.io.VirtualDirectory

trait NirGenFile { self: NirGenPhase =>
  import global._

  def genPathFor(cunit: CompilationUnit, sym: Symbol): Path = {
    val baseDir: AbstractFile =
      settings.outputDirs.outputDirFor(cunit.source.file)

    val id        = genTypeName(sym).id
    val pathParts = id.split("[./]")
    val dir       = (baseDir /: pathParts.init)(_.subdirectoryNamed(_))

    var filename = pathParts.last
    val file     = dir fileNamed (filename + ".nir")

    Paths.get(file.file.getAbsolutePath)
  }

  def genIRFile(path: Path, defns: Seq[nir.Defn]): Unit = {
    //println(s"--- serializing ${defns.size} defns to ${path}")
    withScratchBuffer { buffer =>
      serializeBinary(defns, buffer)
      buffer.flip
      VirtualDirectory.local(path.getParent.toFile).write(path, buffer)
    }
  }
}
