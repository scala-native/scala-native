package salty.tools
package compiler

import java.io._
import java.nio._
import java.nio.channels._
import scala.tools.nsc._
import scala.tools.nsc.io.AbstractFile
import salty.ir, ir.{GraphSerializer, BinarySerializer}
//import salty.ir.ShowDOT

trait GenIRFiles extends SubComponent  {
  import global._

  // 16M should be enough for everyone, right?
  private lazy val bb = ByteBuffer.allocateDirect(16 * 1024 * 1024)

  private def serialize(scope: ir.Scope)
                       (serializer: (ir.Scope, ByteBuffer) => Unit): ByteBuffer = {
    bb.clear
    serializer(scope, bb)
    bb.flip
    bb
  }

  private def genSerializedFile(cunit: CompilationUnit, sym: Symbol, scope: ir.Scope,
                                serializer: (ir.Scope, ByteBuffer) => Unit,
                                extension: String): Unit = {
    val outfile = getFileFor(cunit, sym, extension).file
    val channel = (new FileOutputStream(outfile)).getChannel()
    try channel.write(serialize(scope)(serializer))
    finally channel.close
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

  def genSaltyFile(cunit: CompilationUnit, sym: Symbol, scope: ir.Scope) =
    genSerializedFile(cunit, sym, scope, BinarySerializer, ".salty")

  def genGraphFile(cunit: CompilationUnit, sym: Symbol, scope: ir.Scope) =
    genSerializedFile(cunit, sym, scope, GraphSerializer, ".dot")
}
