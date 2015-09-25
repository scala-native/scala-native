package salty.tools
package compiler

import java.io._
import java.nio._
import java.nio.channels._
import java.util.Base64
import scala.tools.nsc._
import scala.tools.nsc.io.AbstractFile
import salty.ir, ir.Serializer
//import salty.ir.ShowDOT

trait GenIRFiles extends SubComponent  {
  import global._

  lazy val bb = ByteBuffer.allocateDirect(16 * 1024 * 1024) // 16M should be enough for everyone.

  def serialize(scope: ir.Scope): ByteBuffer = {
    bb.clear
    val serializer = new Serializer(bb)
    serializer.serialize(scope)
    bb.flip
    bb
  }

  def genIRFile(cunit: CompilationUnit, sym: Symbol, scope: ir.Scope): Unit = {
    val outfile = getFileFor(cunit, sym, ".salty").file
    val channel = (new FileOutputStream(outfile)).getChannel()
    try channel.write(serialize(scope))
    finally channel.close
  }

  /*
  def genDOTFile(cunit: CompilationUnit, sym: Symbol, scope: ir.Scope): Unit = {
    val outfile = getFileFor(cunit, sym, ".dot").file
    val channel = (new FileOutputStream(outfile)).getChannel()
    try channel.write(ByteBuffer.wrap((ShowDOT.showScope(scope).toString.getBytes)))
    finally channel.close
  }
  */

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
