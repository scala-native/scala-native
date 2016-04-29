package scala.scalanative
package nscplugin

import scala.tools.nsc._
import scala.tools.nsc.io.AbstractFile

trait NirFiles { self: NirCodeGen =>
  import global._

  private def getPathFor(
    cunit: CompilationUnit, sym: Symbol, suffix: String): String = {
    val baseDir: AbstractFile =
      settings.outputDirs.outputDirFor(cunit.source.file)

    val id        = genClassName(sym).id
    val pathParts = id.split("[./]")
    val dir       = (baseDir /: pathParts.init)(_.subdirectoryNamed(_))

    var filename = pathParts.last
    val file = dir fileNamed (filename + suffix)

    file.file.getAbsolutePath
  }

  def genIRFile(
    cunit: CompilationUnit, sym: Symbol, defns: Seq[nir.Defn]): Unit = {
    val kind =
      if (isModule(sym)) "value"
      else "type"
    nir.serialization.serializeBinaryFile(
      defns, getPathFor(cunit, sym, s".$kind.nir"))
    nir.serialization.serializeTextFile(
      defns, getPathFor(cunit, sym, s".$kind.hnir"))
  }
}
