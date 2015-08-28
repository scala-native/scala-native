package salty.tools
package compiler

import java.io._
import scala.tools.nsc._
import scala.tools.nsc.io.AbstractFile
import salty.ir

trait GenIRFiles extends SubComponent  {
  import global._

  def genIRFile(cunit: CompilationUnit, sym: Symbol, stat: ir.Stat): Unit = {
    val outfile = getFileFor(cunit, sym, ".salty")
    val output = outfile.bufferedOutput
    val serialized = stat.serialize
    try output.write(serialized)
    finally output.close()
  }

  private def getFileFor(cunit: CompilationUnit, sym: Symbol, suffix: String) = {
    val baseDir: AbstractFile =
      settings.outputDirs.outputDirFor(cunit.source.file)

    val pathParts = sym.fullName.split("[./]")
    val dir = (baseDir /: pathParts.init)(_.subdirectoryNamed(_))

    var filename = pathParts.last
    if (sym.isModuleClass && !sym.isImplClass)
      filename = filename + nme.MODULE_SUFFIX_STRING

    dir fileNamed (filename + suffix)
  }
}
