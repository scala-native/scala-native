package native
package plugin

import scala.tools.nsc._
import scala.tools.nsc.io.AbstractFile
import native.ir
import native.ir.serialization._

trait GenIRFiles extends SubComponent  {
  import global._

  private def getPathFor(cunit: CompilationUnit, sym: Symbol, suffix: String): String = {
    val baseDir: AbstractFile =
      settings.outputDirs.outputDirFor(cunit.source.file)

    val pathParts = sym.fullName.split("[./]")
    val dir = (baseDir /: pathParts.init)(_.subdirectoryNamed(_))

    var filename = pathParts.last
    if (sym.isModuleClass && !sym.isImplClass)
      filename = filename + nme.MODULE_SUFFIX_STRING
    val file = dir fileNamed (filename + suffix)

    file.file.getAbsolutePath
  }

  def genIRFile(cunit: CompilationUnit, sym: Symbol, scope: ir.Scope) = {
    val kind =
      if (sym.isModuleClass) "module"
      else if (sym.isInterface) "interface"
      else "class"
    serializeIRFile(scope, getPathFor(cunit, sym, s".$kind.nir"))
  }

  def genDotFile(cunit: CompilationUnit, sym: Symbol, scope: ir.Scope) =
    serializeDotFile(scope, getPathFor(cunit, sym, ".dot"))
}
