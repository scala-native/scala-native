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

  private def getPathFor(cunit: CompilationUnit,
                         sym: Symbol,
                         suffix: String): Path = {
    val baseDir: AbstractFile =
      settings.outputDirs.outputDirFor(cunit.source.file)

    val id        = genTypeName(sym).id
    val pathParts = id.split("[./]")
    val dir       = (baseDir /: pathParts.init)(_.subdirectoryNamed(_))

    var filename = pathParts.last
    val file     = dir fileNamed (filename + suffix)

    Paths.get(file.file.getAbsolutePath)
  }

  def genIRFile(cunit: CompilationUnit,
                sym: Symbol,
                defns: Seq[nir.Defn]): Unit = {
    withScratchBuffer { buffer =>
      serializeBinary(defns, buffer)
      buffer.flip
      root.write(getPathFor(cunit, sym, s".nir"), buffer)
    }

    withScratchBuffer { buffer =>
      serializeText(defns, buffer)
      buffer.flip
      root.write(getPathFor(cunit, sym, s".hnir"), buffer)
    }
  }
}
