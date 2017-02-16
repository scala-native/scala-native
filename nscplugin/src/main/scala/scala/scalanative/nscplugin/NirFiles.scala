package scala.scalanative
package nscplugin

import java.nio.file.{Path, Paths}
import scala.tools.nsc._
import scala.tools.nsc.io.AbstractFile
import scalanative.nir.serialization.{serializeText, serializeBinary}
import scalanative.io.withScratchBuffer
import scalanative.io.VirtualDirectory.root

trait NirFiles { self: NirCodeGen =>
  import global._

  def getPathFor(cunit: CompilationUnit, sym: Symbol): Path = {
    val baseDir: AbstractFile =
      settings.outputDirs.outputDirFor(cunit.source.file)

    val id        = genTypeName(sym).id
    val pathParts = id.split("[./]")
    val dir       = (baseDir /: pathParts.init)(_.subdirectoryNamed(_))

    var filename = pathParts.last
    val file     = dir fileNamed (filename + ".nir")

    Paths.get(file.file.getAbsolutePath)
  }

  def genIRFiles(files: Seq[(Path, Seq[nir.Defn])]): Unit =
    files.foreach {
      case (path, defns) =>
        withScratchBuffer { buffer =>
          serializeBinary(defns, buffer)
          buffer.flip
          root.write(path, buffer)
        }
    }
}
