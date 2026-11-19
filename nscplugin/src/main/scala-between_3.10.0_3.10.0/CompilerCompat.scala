package scala.scalanative.nscplugin

import java.nio.file.{Path, Paths}

import dotty.tools.dotc.core.Contexts._
import dotty.tools.dotc.core.Symbols.Symbol
import dotty.tools.dotc.util.SourceFile
import dotty.tools.io.AbstractFile

object CompilerCompat {
  val Inlines = dotty.tools.dotc.inlines.Inlines
  val SymUtils = dotty.tools.dotc.core.Symbols
  val SymbolExtensions = dotty.tools.backend.jvm.SymbolUtils.symExtensions

  type ScalaPrimitives = dotty.tools.backend.ScalaPrimitives

  val LazyValHandleName = Option(
    dotty.tools.dotc.core.NameKinds.LazyVarHandleName
  )

  // `AbstractFile.bufferedOutput` is gone since 3.10.0, buffer it ourselves
  def abstractFileOutput(file: AbstractFile): java.io.OutputStream =
    new java.io.BufferedOutputStream(file.output)

  def sourceRootPath(using ctx: Context): Path = {
    val path =
      if !ctx.settings.sourcepath.isDefault then
        Paths.get(ctx.settings.sourcepath.value)
      else ctx.settings.sourceroot.value.jpath
    path.toAbsolutePath.normalize()
  }

  def absoluteSourcePath(source: SourceFile): Path =
    source.file.jpath.toAbsolutePath.normalize()

  def relativeSourcePath(source: SourceFile, relativeTo: Path): String = {
    val absSourcePath = absoluteSourcePath(source)
    val refPath = relativeTo.toAbsolutePath.normalize()
    if absSourcePath.startsWith(refPath) then
      refPath
        .relativize(absSourcePath)
        .toString
        .replace(java.io.File.separatorChar, '/')
    else source.file.path
  }

  abstract class NirPrimitivesBase(using ctx: Context)
      extends ScalaPrimitives(using ctx) {
    protected def nirPrimitiveCode(sym: Symbol): Option[Int]

    override def getPrimitive(sym: Symbol): Int =
      nirPrimitiveCode(sym).getOrElse(super.getPrimitive(sym))

    override def isPrimitive(sym: Symbol): Boolean =
      nirPrimitiveCode(sym).isDefined || super.isPrimitive(sym)
  }

  /** The alternatives of a catch case `case e: (A | B)`, which stays a catch
   *  case since Scala 3.10.2 (scala/scala3#27294); before, the pattern matcher
   *  handles such cases, so there are none.
   */
  def multiCatchTypes(cdef: dotty.tools.dotc.ast.tpd.CaseDef)(using
      dotty.tools.dotc.core.Contexts.Context
  ): Option[List[dotty.tools.dotc.core.Types.Type]] = None
}
