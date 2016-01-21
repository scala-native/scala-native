package native
package nir
package plugin

import scala.tools.nsc._
import scala.tools.nsc.io.AbstractFile

trait GenIRFiles extends SubComponent with GenTypeKinds {
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

  def genIRFile(cunit: CompilationUnit, sym: Symbol, defns: Seq[nir.Defn]): Unit = {
    val kind =
      if (isModule(sym)) "module"
      else if (sym.isInterface) "interface"
      else "class"
    nir.serialization.serializeBinaryFile(defns, getPathFor(cunit, sym, s".$kind.nir"))
    nir.serialization.serializeTextFile(defns, getPathFor(cunit, sym, s".$kind.hnir"))
  }
}
